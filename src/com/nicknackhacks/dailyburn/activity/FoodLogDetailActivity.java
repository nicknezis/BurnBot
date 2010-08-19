package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.admob.android.ads.AdView;
import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;

public class FoodLogDetailActivity extends Activity {
	private BurnBot app;
	private FoodDao foodDao;
	private FoodLogEntry detailFoodEntry;
	private DrawableManager dManager = new DrawableManager();
	private long selectedEntryKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.foodlogdetail);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
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

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		selectedEntryKey = extras.getLong("selectedEntry");
		app = (BurnBot) this.getApplication();
		detailFoodEntry = (FoodLogEntry) app.objects.get(selectedEntryKey).get();
		final TextView nameField = (TextView) findViewById(R.id.food_log_name);
		nameField.setText("Name: " + detailFoodEntry.getFoodName());
		final ImageView icon = (ImageView) findViewById(R.id.food_log_icon);
		dManager.fetchDrawableOnThread("http://dailyburn.com"
					+ detailFoodEntry.getFoodPictureUrl(), icon);
		final TextView servingsField = (TextView) findViewById(R.id.food_log_servings_eaten);
		servingsField.setText("Servings: " + detailFoodEntry.getServingsEaten());
		final TextView loggedOnField = (TextView) findViewById(R.id.food_log_logged_on);
		loggedOnField.setText("Logged on: " + detailFoodEntry.getLoggedOn());
		
		AdView ad = (AdView)findViewById(R.id.ad);
		ad.setVisibility(View.VISIBLE);
		String keywords = "health food " + detailFoodEntry.getFoodName();
		BurnBot.LogD("Setting keywords: " + keywords);
		ad.setKeywords(keywords);
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
			BurnBot.LogE(e.getMessage(), e);
		}
	}
}
