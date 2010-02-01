package org.nicknack.dailyburn.activity;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.DrawableManager;
import org.nicknack.dailyburn.api.FoodDao;
import org.nicknack.dailyburn.model.Food;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodDetail extends Activity {
	private DailyBurnDroid app;
	private FoodDao foodDao;
	private SharedPreferences pref;
	private DrawableManager dManager = new DrawableManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);
		pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		DefaultOAuthConsumer consumer = new DefaultOAuthConsumer(
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
		app = (DailyBurnDroid) this.getApplication();
		Food detailFood = (Food) app.objects.get(selectedFoodKey).get();
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
}
