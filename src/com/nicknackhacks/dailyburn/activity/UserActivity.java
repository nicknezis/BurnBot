package com.nicknackhacks.dailyburn.activity;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.UserDao;
import com.nicknackhacks.dailyburn.model.User;

public class UserActivity extends Activity {

	private UserDao userDao;
	private SharedPreferences pref;
	private DrawableManager dManager = new DrawableManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.userdetail);

		BurnBot app = (BurnBot) getApplication();
		userDao = new UserDao(app);
		
		User user = userDao.getUserInfo();
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
		
	}
}
