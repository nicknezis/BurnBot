package com.nicknackhacks.dailyburn.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.foodlogdetail);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Long selectedEntryKey = extras.getLong("selectedEntry");
		app = (BurnBot) this.getApplication();
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
	
	public void onDeleteEntry(View v) {
		try {
		foodDao.deleteFoodLogEntry(detailFoodEntry.getId());
		} catch (Exception e) {
			Log.e(BurnBot.TAG, e.getMessage());
		}
	}
}
