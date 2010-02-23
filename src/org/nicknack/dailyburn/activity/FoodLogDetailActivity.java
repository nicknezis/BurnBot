package org.nicknack.dailyburn.activity;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.DrawableManager;
import org.nicknack.dailyburn.api.FoodDao;
import org.nicknack.dailyburn.model.FoodLogEntry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodLogDetailActivity extends Activity {
	private DailyBurnDroid app;
	private FoodDao foodDao;
	private FoodLogEntry detailFoodEntry;
	private SharedPreferences pref;
	private DrawableManager dManager = new DrawableManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.foodlogdetail);
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
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Long selectedEntryKey = extras.getLong("selectedEntry");
		app = (DailyBurnDroid) this.getApplication();
		detailFoodEntry = (FoodLogEntry) app.objects.get(selectedEntryKey).get();
		final TextView nameField = (TextView) findViewById(R.id.food_log_name);
		nameField.setText("Name: " + detailFoodEntry.getFoodName());
		final ImageView icon = (ImageView) findViewById(R.id.food_log_icon);
		Drawable foodImage = null;
			foodImage = dManager.fetchDrawable("http://dailyburn.com"
					+ detailFoodEntry.getFoodPictureUrl());
			icon.setImageDrawable(foodImage);
		final TextView servingsField = (TextView) findViewById(R.id.food_log_servings_eaten);
		servingsField.setText("Servings: " + detailFoodEntry.getServingsEaten());
		final TextView loggedOnField = (TextView) findViewById(R.id.food_log_logged_on);
		loggedOnField.setText("Logged on: " + detailFoodEntry.getLoggedOn());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		foodDao.shutdown();
	}
	
	public void onDeleteEntry(View v) {
		try {
		foodDao.deleteFoodLogEntry(detailFoodEntry.getId());
		} catch (Exception e) {
			Log.e(DailyBurnDroid.TAG, e.getMessage());
		}
	}
}
