package com.nicknackhacks.dailyburn.activity;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.api.DietDao;
import com.nicknackhacks.dailyburn.model.DietGoal;

public class DietGoalsActivity extends Activity {

	private SharedPreferences pref;
	private DietDao dietDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dietgoals);

		BurnBot app = (BurnBot) getApplication();
		BodyDao bodyDao = new BodyDao(app);
		
//		bodyDao.getBodyMetrics();
//		bodyDao.getBodyLogEntries();
		
		List<DietGoal> goals = dietDao.getDietGoals();
		LogHelper.LogD( "goals.size " + goals.size());
		for (DietGoal goal : goals) {
			String goalType = goal.getGoalType();
			if (goalType.contains("CalorieDietGoal")) {
				TextView tv = (TextView) (this.findViewById(R.id.CaloriesGoal));
				tv.setText("Calories: " + goal.getLowerBound() + ", "
						+ goal.getUpperBound());
//				ProgressBar bar = (ProgressBar) (this.findViewById(R.id.progress_calories));
//				bar.setProgress(goal.getUpperBound())
			}
		}
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

}
