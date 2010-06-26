package com.nicknackhacks.dailyburn.activity;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.nicknackhacks.dailyburn.api.BodyDao;

public class BodyEntryAddActivity extends Activity {
	protected ArrayAdapter<CharSequence> mMetricTypes;
	protected BodyDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_body_entry);
		
		// Set the contents of the Metrics spinner.
		Spinner metricSpinner = (Spinner) findViewById(R.id.metric_types);
		dao = new BodyDao((BurnBot) getApplication());
		mMetricTypes = (ArrayAdapter<CharSequence>) dao.getBodyMetrics();
		
		metricSpinner.setAdapter(mMetricTypes);
	}
	
	
}
