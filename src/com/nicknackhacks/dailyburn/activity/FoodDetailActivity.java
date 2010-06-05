package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;
import java.util.Calendar;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;

public class FoodDetailActivity extends Activity {
	
	private static final int DATE_DIALOG_ID = 0; 
	private BurnBot app;
	private FoodDao foodDao;
	private Food detailFood;
	private SharedPreferences pref;
	private DrawableManager dManager = new DrawableManager();
//	protected int mYear;
//	protected int mMonthOfYear;
//	protected int mDayOfMonth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);
		pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		foodDao = new FoodDao(new DefaultHttpClient(), consumer);
	}

	@Override
	protected void onResume() {
		super.onResume();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Long selectedFoodKey = extras.getLong("selectedFood");
		app = (BurnBot) this.getApplication();
		detailFood = (Food) app.objects.get(selectedFoodKey).get();
		final TextView tv = (TextView) findViewById(R.id.food_name);
		tv.setText("Name: " + detailFood.getName());
		final ImageView icon = (ImageView) findViewById(R.id.food_icon);
		Drawable foodImage = null;
		if (detailFood.getThumbUrl() != null) {
			foodImage = dManager.fetchDrawable("http://dailyburn.com"
					+ detailFood.getNormalUrl());
			icon.setImageDrawable(foodImage);
		}

		final WebView nutrition = (WebView) findViewById(R.id.nutrition);
		String html = foodDao.getNutritionLabel(detailFood.getId());
		nutrition.loadData(html, "text/html", "UTF-8");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		foodDao.shutdown();
	}
	
	public void onAddFavorite(View v) {
		try {
			foodDao.addFavoriteFood(this.detailFood.getId());
		} catch (OAuthMessageSignerException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void onAddLogEntry(View v) {
		showDialog(DATE_DIALOG_ID);		
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	Calendar cal = Calendar.getInstance();
    	int cYear = cal.get(Calendar.YEAR);
    	int cMonth = cal.get(Calendar.MONTH);
    	int cDay = cal.get(Calendar.DAY_OF_MONTH);
    	switch(id) {
    	case DATE_DIALOG_ID:
    		final Dialog dialog = new Dialog(this);

    		dialog.setContentView(R.layout.add_foodlogentry);
    		dialog.setTitle("I Ate This");

    		DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.DatePicker);
    		datePicker.init(cYear,cMonth,cDay, null);
    		dialog.setCancelable(true);
    		((Button)dialog.findViewById(R.id.dialog_ok)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
//					Log.d(DailyBurnDroid.TAG,"OK: " + FoodDetailActivity.this.mYear + "-" + 
//							FoodDetailActivity.this.mMonthOfYear + ", Serv: " + 
//							((EditText)dialog.findViewById(R.id.servings_eaten)).getText());
					String servings_eaten = ((EditText)dialog.findViewById(R.id.servings_eaten)).getText().toString();
					DatePicker datePicker = (DatePicker)dialog.findViewById(R.id.DatePicker);
					try {
						foodDao.addFoodLogEntry(detailFood.getId(), servings_eaten, 
												datePicker.getYear(), 
												datePicker.getMonth(), 
												datePicker.getDayOfMonth());
					} catch (Exception e) {
						Log.e(BurnBot.TAG, e.getMessage());
						e.printStackTrace();
					} 
				}
			});
    		((Button)dialog.findViewById(R.id.dialog_cancel)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
				}
			});
    		return dialog;
    	}
    	return null;
    }

//    private OnDateChangedListener dateChangedListener = new OnDateChangedListener() {
//		
//		public void onDateChanged(DatePicker view, int year, int monthOfYear,
//				int dayOfMonth) {
//			switch(view.getId()) {
//			case R.id.DatePicker:
//				mYear = year;
//				mMonthOfYear = monthOfYear;
//				mDayOfMonth = dayOfMonth;
//			}			
//		}
//	};
}
