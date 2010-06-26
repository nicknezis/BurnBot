package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Spinner;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.BodyMetricsAdaptor;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.model.BodyMetric;

public class BodyEntryAddActivity extends Activity {
	protected ArrayList<BodyMetric> mMetricTypes;
	protected BodyMetricsAdaptor bodyMetricsAdapter;
	protected BodyDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_body_entry);
		
		SharedPreferences pref = this.getSharedPreferences("dbdroid", 0);
		
		// Check if the user is authenticated.
		if(pref.getBoolean("isAuthed", false)) {
			// Set the contents of the Metrics spinner.
			Spinner metricSpinner = (Spinner) findViewById(R.id.metric_types);
			dao = new BodyDao((BurnBot) getApplication());
			mMetricTypes = dao.getBodyMetrics(this.getApplicationContext());
			
			bodyMetricsAdapter = new BodyMetricsAdaptor(this, android.R.layout.simple_spinner_item, mMetricTypes);
			bodyMetricsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			metricSpinner.setAdapter(bodyMetricsAdapter);			
		}
		
	}
}
