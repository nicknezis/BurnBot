package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.admob.android.ads.AdView;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;

public class FoodLogDetailActivity extends Activity {
	private BurnBot app;
	private FoodDao foodDao;
	private FoodLogEntry detailFoodEntry;
	private long selectedEntryKey;
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.foodlogdetail);

		app = (BurnBot) getApplication();
		cache = app.getCache();
		LogHelper.LogD("Bus Key: %s", getBusKey());
		cache.getBus().register(getBusKey(), onCache);
		foodDao = new FoodDao(app);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		selectedEntryKey = extras.getLong("selectedEntry");

		detailFoodEntry = (FoodLogEntry) app.objects.get(selectedEntryKey)
				.get();
		final TextView nameField = (TextView) findViewById(R.id.food_log_name);
		nameField.setText("Name: " + detailFoodEntry.getFoodName());
		final ImageView icon = (ImageView) findViewById(R.id.food_log_icon);
		icon.setTag(detailFoodEntry.getFoodPictureUrl());
		ThumbnailMessage msg = cache.getBus().createMessage(getBusKey());
		msg.setImageView(icon);
		msg.setUrl(detailFoodEntry.getFoodPictureUrl());
		try {
			cache.notify(msg.getUrl(), msg);
		} catch (Throwable t) {
			LogHelper.LogE("Exception trying to fetch image", t);
		}
		final TextView servingsField = (TextView) findViewById(R.id.food_log_servings_eaten);
		servingsField
				.setText("Servings: " + detailFoodEntry.getServingsEaten());
		final TextView loggedOnField = (TextView) findViewById(R.id.food_log_logged_on);
		loggedOnField.setText("Logged on: " + detailFoodEntry.getLoggedOn());

//		GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
//		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
//		adSenseSpec.setKeywords(adSenseSpec.getKeywords() + ","
//				+ detailFoodEntry.getFoodName());
//		googleAdView.showAds(adSenseSpec);
		 AdView ad = (AdView)findViewById(R.id.ad);
		 ad.setVisibility(View.VISIBLE);
		 ad.setKeywords(ad.getKeywords() + " " + detailFoodEntry.getFoodName());
		 LogHelper.LogD("Setting keywords: %s", ad.getKeywords());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cache.getBus().unregister(onCache);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (BurnBot.DoFlurry) {
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
			FlurryAgent.onPageView();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void onDeleteEntry(View v) {
		try {
			foodDao.deleteFoodLogEntry(detailFoodEntry.getId());
			Intent data = new Intent();
			// Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(detailFoodEntry));
			data.putExtra("selectedEntry", key);
			data.putExtra("itemDeleted", true);
			setResult(RESULT_OK, data);
			finish();
		} catch (Exception e) {
			LogHelper.LogE(e.getMessage(), e);
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
