package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;

import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;

public class FoodDetailActivity extends Activity {

	private static final int FOOD_ENTRY_RESULT_CODE = 0;
	//private BurnBot app;
	private FoodDao foodDao;
	private Food detailFood;
	private SharedPreferences pref;
	private DrawableManager dManager = new DrawableManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Long selectedFoodKey = extras.getLong("selectedFood");
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

		GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
		adSenseSpec.setKeywords(adSenseSpec.getKeywords() + ", " + detailFood.getBrand() +", " + detailFood.getName());
		googleAdView.showAds(adSenseSpec);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}

//	@Override
//	protected void onResume() {
//		super.onResume();
//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		int width = metrics.widthPixels;
//		Intent intent = getIntent();
//		Bundle extras = intent.getExtras();
//		Long selectedFoodKey = extras.getLong("selectedFood");
//		app = (BurnBot) this.getApplication();
//		detailFood = (Food) app.objects.get(selectedFoodKey).get();
//		final TextView tv = (TextView) findViewById(R.id.food_name);
//		tv.setText("Name: " + detailFood.getName());
//		final ImageView icon = (ImageView) findViewById(R.id.food_icon);
//		Drawable foodImage = null;
//		if (detailFood.getThumbUrl() != null) {
//			foodImage = dManager.fetchDrawable("http://dailyburn.com"
//					+ detailFood.getNormalUrl());
//			icon.setImageDrawable(foodImage);
//		}
//
//		final WebView nutrition = (WebView) findViewById(R.id.nutrition);
//		String html = foodDao.getNutritionLabel(detailFood.getId());
//		nutrition.loadData(html, "text/html", "UTF-8");
//		
////		AdView ad = (AdView)findViewById(R.id.ad);
////		String keywords = "health food " + detailFood.getBrand() + " " + detailFood.getName();
////		LogHelper.LogD("Setting keywords: " + keywords);
////		ad.setKeywords(keywords);
//		
//	}

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
	
}
