package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;

import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.AddFoodLogEntryDialog;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
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

	public void onAddFavorite(View v) {
		try {
			foodDao.addFavoriteFood(this.detailFood.getId());
		} catch (OAuthMessageSignerException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthNotAuthorizedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (ClientProtocolException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogE(e.getMessage(), e);
		}
	}

	public void onAddLogEntry(View v) {
		showDialog(DATE_DIALOG_ID);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			final AddFoodLogEntryDialog dialog = new AddFoodLogEntryDialog(
					this, foodDao);
			return dialog;
		}
		return null;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DATE_DIALOG_ID:
			((AddFoodLogEntryDialog)dialog).setFoodId(detailFood.getId());
		}
	}
}
