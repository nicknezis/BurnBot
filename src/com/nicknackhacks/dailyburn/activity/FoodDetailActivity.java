package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;

import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.flurry.android.FlurryAgent;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.provider.BurnBotContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodLabelContract;

public class FoodDetailActivity extends Activity {

	private static final int FOOD_ENTRY_RESULT_CODE = 0;
	private FoodDao foodDao;
	private Food detailFood;
	private SharedPreferences pref;
	private Cursor foodCursor;
	private Cursor labelCursor;
	private FoodDetailContentObserver foodObserver;
	private FoodLabelContentObserver labelObserver;
	private int selectedFoodKey;
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);

		BurnBot app = (BurnBot) getApplication();
		cache = app.getCache();
		LogHelper.LogD("Bus Key: %s", getBusKey());
		cache.getBus().register(getBusKey(), onCache);
		foodDao = new FoodDao(app);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		selectedFoodKey = extras.getInt("selectedFood");

		//TODO: Move this to async task followed by updateActivityFromCursor()
		String html = foodDao.getNutritionLabel(selectedFoodKey);
		try {
			getContentResolver().applyBatch(
					BurnBotContract.CONTENT_AUTHORITY,
					foodDao.getNutritionLabelOps(selectedFoodKey,html));
		} catch (RemoteException e) {
			LogHelper.LogE(
					"RemoteException while applying operations to the ContentResolver",
					e);
		} catch (OperationApplicationException e) {
			LogHelper.LogE("ContentProviderOperation failed.", e);
		}

		foodCursor = managedQuery(
				FoodContract.buildFoodUri(String.valueOf(selectedFoodKey)),
				null, null, null, null);

		labelCursor = managedQuery(FoodLabelContract.buildFoodLabelUri(String.valueOf(selectedFoodKey)),null,null,null,null);

		updateActivityFromCursors();
		
		GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
		adSenseSpec.setKeywords(adSenseSpec.getKeywords() + ", "
				+ detailFood.getBrand() + ", " + detailFood.getName());
		googleAdView.showAds(adSenseSpec);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cache.getBus().unregister(onCache);
	}

	private void updateActivityFromCursors() {
		if (foodCursor.moveToFirst()) {
			detailFood = FoodContract.getFoodFromCursor(foodCursor);

			// detailFood = (Food) app.objects.get(selectedFoodKey).get();
			final TextView tv = (TextView) findViewById(R.id.food_name);
			tv.setText("Name: " + detailFood.getName());
			final ImageView icon = (ImageView) findViewById(R.id.food_icon);
			Drawable foodImage = null;
			if (detailFood.getThumbUrl() != null) {
//				String thumbUrl = "http://dailyburn.com/images/default_food_thumb.gif";
//				if(detailFood.getThumbUrl().contains("http")) {
//					thumbUrl = detailFood.getThumbUrl();
//				}
//				if(f.getThumbUrl().contentEquals("/images/default_food_thumb.gif")) {
//					getIcon().setTag("http://dailyburn.com" + f.getThumbUrl());
//				} else {
//					getIcon().setTag(f.getThumbUrl());
//				}
				icon.setTag(detailFood.getNormalUrl());
				ThumbnailMessage msg = cache.getBus().createMessage(getBusKey());
				msg.setImageView(icon);
				msg.setUrl(detailFood.getNormalUrl());
				try {
					cache.notify(msg.getUrl(), msg);
				} catch (Throwable t) {
					LogHelper.LogE("Exception trying to fetch image", t);
				}
			}

			if(labelCursor.moveToFirst()) {
				final WebView nutrition = (WebView) findViewById(R.id.nutrition);
				String label = FoodLabelContract.getFoodLabelFromCursor(labelCursor);
				nutrition.loadData(label, "text/html", "UTF-8");
			}
//				else {
//				String html = foodDao.getNutritionLabel(detailFood.getId());
//				try {
//					getContentResolver().applyBatch(
//							BurnBotContract.CONTENT_AUTHORITY,
//							foodDao.getNutritionLabelOps(detailFood.getId(),html));
//				} catch (RemoteException e) {
//					LogHelper.LogE(
//							"RemoteException while applying operations to the ContentResolver",
//							e);
//				} catch (OperationApplicationException e) {
//					LogHelper.LogE("ContentProviderOperation failed.", e);
//				}
//			}			
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		foodObserver = new FoodDetailContentObserver(new Handler());
		labelObserver = new FoodLabelContentObserver(new Handler());
		Uri labelUri = FoodLabelContract.buildFoodLabelUri(String.valueOf(selectedFoodKey));
		Uri foodUri = FoodContract.buildFoodUri(String.valueOf(selectedFoodKey));
		LogHelper.LogD("Registering foodObserver: %s, uri: %s", labelObserver, labelUri);
		LogHelper.LogD("Registering foodObserver: %s, uri: %s", foodObserver, foodUri);
		getContentResolver().registerContentObserver(
				labelUri,
				true, labelObserver);
		getContentResolver().registerContentObserver(
				foodUri, 
				true, foodObserver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogHelper.LogD("UnRegistering %s", foodObserver);
		getContentResolver().unregisterContentObserver(foodObserver);
		LogHelper.LogD("UnRegistering %s", labelObserver);
		getContentResolver().unregisterContentObserver(labelObserver);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}

	public void onAddFavorite(View v) {
		FlurryAgent.onEvent("Click Add Favorite Button");
		try {
			foodDao.addFavoriteFood(this.detailFood.getId());
		} catch (OAuthMessageSignerException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (OAuthNotAuthorizedException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (ClientProtocolException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (IOException e) {
			LogHelper.LogE(e.getMessage(), e);
		}
	}

	public void onAddLogEntry(View v) {
		FlurryAgent.onEvent("Click Add Log Entry Button");
		Intent intent = new Intent(this, AddFoodLogEntryActivity.class);
		intent.putExtra("foodId", detailFood.getId());
		intent.putExtra("servingSize", detailFood.getServingSize());
		intent.putExtra("foodName", detailFood.getName());
		startActivityForResult(intent, FOOD_ENTRY_RESULT_CODE);
	}

	private class FoodDetailContentObserver extends ContentObserver {

		public FoodDetailContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			//labelCursor.requery();
			foodCursor.requery();
			updateActivityFromCursors();
		}
	}
	
	private class FoodLabelContentObserver extends ContentObserver {

		public FoodLabelContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			labelCursor.requery();
			//foodCursor.requery();
			updateActivityFromCursors();
		}
	}
	
	private String getBusKey() {
		return (toString());
	}

	private ThumbnailBus.Receiver<ThumbnailMessage> onCache = new ThumbnailBus.Receiver<ThumbnailMessage>() {
		public void onReceive(final ThumbnailMessage message) {
			final ImageView image = message.getImageView();

			runOnUiThread(new Runnable() {
				public void run() {
					if (image.getTag() != null
							&& image.getTag().toString()
									.equals(message.getUrl())) {
						image.setImageDrawable(cache.get(message.getUrl()));
					}
				}
			});
		}
	};
}
