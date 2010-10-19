package com.nicknackhacks.dailyburn.activity;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.google.ads.AdSenseSpec.ExpandDirection;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.UserDao;
import com.nicknackhacks.dailyburn.model.User;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserContract;

public class UserActivity extends Activity {

	private UserDao userDao;
	private DrawableManager dManager = new DrawableManager();
	private UserInfoAsyncTask userAsyncTask = new UserInfoAsyncTask();
	private UserContentObserver observer;
	private Cursor cursor;
	private boolean mSyncing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.userdetail);

		BurnBot app = (BurnBot) getApplication();
		userDao = new UserDao(app);

		cursor = getContentResolver().query(UserContract.CONTENT_URI, null,
				null, null, null);
		updateActivityFromCursor(cursor);
		startManagingCursor(cursor);
		userAsyncTask.execute();
		
		GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
		adSenseSpec.setExpandDirection(ExpandDirection.TOP);
		googleAdView.showAds(adSenseSpec);
	}

	public void onRefresh(View v) {
		switch (userAsyncTask.getStatus()) {
		case PENDING:
			userAsyncTask.execute();
			return;
		case FINISHED:
			userAsyncTask = new UserInfoAsyncTask();
			userAsyncTask.execute();
			return;
		case RUNNING:
			return;
		}
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

	@Override
	protected void onResume() {
		super.onResume();
		observer = new UserContentObserver(new Handler());
		LogHelper.LogD("Registering " + observer);
		getContentResolver().registerContentObserver(UserContract.CONTENT_URI,
				true, observer);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogHelper.LogD("UnRegistering " + observer);
		getContentResolver().unregisterContentObserver(observer);
	}

	void updateActivityFromCursor(Cursor cursor) {
		if (cursor.moveToFirst()) {
			User user = new User(cursor);
			LogHelper.LogD(user.getUsername() + ", " + user.getPictureUrl());
			String text = "Username: " + user.getUsername();
			((TextView) findViewById(R.id.user_name)).setText(text);
			text = "Current Weight: " + user.getBodyWeight();
			((TextView) findViewById(R.id.current_weight)).setText(text);
			text = "Goal Weight: " + user.getBodyWeightGoal();
			((TextView) findViewById(R.id.goal_weight)).setText(text);
			text = "Calories Eaten: " + user.getCaloriesConsumed();
			((TextView) findViewById(R.id.calories_eaten)).setText(text);
			text = "Calories Burned: " + user.getCaloriesBurned();
			((TextView) findViewById(R.id.calories_burned)).setText(text);
			text = "Exercise Status: " + user.getDaysExercisedInPastWeek();
			((TextView) findViewById(R.id.exercise_status)).setText(text);
			text = "Nutrition Status: " + user.getCalGoalsMetInPastWeek();
			((TextView) findViewById(R.id.nutrition_status)).setText(text);
			if (user.getPictureUrl() != null) {
				final ImageView icon = (ImageView) findViewById(R.id.user_icon);
				dManager.fetchDrawableOnThread(user.getPictureUrl(), icon);
			}
		}
	}

	private class UserContentObserver extends ContentObserver {

		public UserContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			cursor.requery();
			updateActivityFromCursor(cursor);
		}
	}

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mSyncing ? View.VISIBLE : View.GONE);
	}

	private class UserInfoAsyncTask extends AsyncTask<Void, Void, User> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mSyncing = true;
			updateRefreshStatus();
		}

		@Override
		protected User doInBackground(Void... unused) {
			User user = userDao.getUserInfo();
			if (null != user) {
				userDao.getUserAndApply(getContentResolver(), user);
			}
			return user;
		}

		@Override
		protected void onPostExecute(User user) {
			super.onPostExecute(user);
			mSyncing = false;
			updateRefreshStatus();
		}
	}

}
