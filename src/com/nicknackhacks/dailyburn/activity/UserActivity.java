package com.nicknackhacks.dailyburn.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.UserDao;
import com.nicknackhacks.dailyburn.model.User;
import com.nicknackhacks.dailyburn.provider.BurnBotContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserContract;
import com.nicknackhacks.dailyburn.provider.BurnBotProvider;

public class UserActivity extends Activity {

	private UserDao userDao;
	private DrawableManager dManager = new DrawableManager();
	private UserInfoAsyncTask userAsyncTask = new UserInfoAsyncTask();
	private UserContentObserver observer;
	private Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.userdetail);

		BurnBot app = (BurnBot) getApplication();
		userDao = new UserDao(app);

		cursor = getContentResolver().query(UserContract.CONTENT_URI, null,
				null, null, null);
		startManagingCursor(cursor);
		userAsyncTask.execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
		FlurryAgent.onEvent("UserActivity");
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
		cursor.registerContentObserver(observer);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// cursor.unregisterContentObserver(observer);
	}

	private class UserContentObserver extends ContentObserver {

		public UserContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			cursor = getContentResolver().query(UserContract.CONTENT_URI, null,
					null, null, null);
			if (cursor.moveToFirst()) {
				User user = new User(cursor);
				BurnBot.LogD(user.getUsername() + ", " + user.getTimeZone());
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
					dManager.fetchDrawableOnThread("http://dailyburn.com"
							+ user.getPictureUrl(), icon);
				}
			}
		}
	}

	private class UserInfoAsyncTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(UserActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected Void doInBackground(Void... unused) {
			userDao.getUserAndApply(getContentResolver());
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			super.onPostExecute(unused);

			progressDialog.dismiss();
		}
	}

}
