package com.nicknackhacks.dailyburn.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.nicknackhacks.dailyburn.provider.DailyBurnContract;
import com.nicknackhacks.dailyburn.provider.DailyBurnContract.UserContract;
import com.nicknackhacks.dailyburn.provider.DailyBurnProvider;

public class UserActivity extends Activity {

	private UserDao userDao;
	private DrawableManager dManager = new DrawableManager();
	private UserInfoAsyncTask userAsyncTask = new UserInfoAsyncTask();
	private DailyBurnProvider provider = new DailyBurnProvider();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.userdetail);

		BurnBot app = (BurnBot) getApplication();
		userDao = new UserDao(app);
		
		userAsyncTask.execute();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
		FlurryAgent.onEvent("UserActivity");
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
		getContentResolver().registerContentObserver(DailyBurnContract.UserContract.CONTENT_URI, 
				false, new UserContentObserver(new Handler()));
	}
	
	private class UserContentObserver extends ContentObserver {

		public UserContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Cursor cursor = provider.query(UserContract.CONTENT_URI, null, null, null, null);
			String username = cursor.getString(cursor.getColumnIndex(UserContract.USER_NAME));
			String timezone = cursor.getString(cursor.getColumnIndex(UserContract.USER_TIMEZONE));
			BurnBot.LogD(username + ", " + timezone);
			String text = "Username: " + username.toUpperCase();
			((TextView) findViewById(R.id.user_name)).setText(text);
		}
	}
	
	private class UserInfoAsyncTask extends AsyncTask<Void, Void, User> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(UserActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected User doInBackground(Void... unused) {
			User user = userDao.getUserInfo();
			return user;
		}

		@Override
		protected void onPostExecute(User user) {
			super.onPostExecute(user);
			if (user == null)
				return;
			if (user.getPictureUrl() != null) {
				final ImageView icon = (ImageView) findViewById(R.id.user_icon);
				dManager.fetchDrawableOnThread("http://dailyburn.com"
						+ user.getPictureUrl(), icon);
			}
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

			progressDialog.dismiss();
		}
	}

}
