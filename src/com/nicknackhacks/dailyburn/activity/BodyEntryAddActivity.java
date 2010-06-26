package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.BodyMetricsAdaptor;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;
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
			
			// Set up the metric spinner units drop down.
			metricSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				
				public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long Id) {
					// Get the units for this metric from the cache and update the next spinner.
					TextView item = (TextView) selectedItemView;
					BodyMetric metric = dao.getMetricDetailsByName((String) item.getText());
					
					// Split the string to get an array of all the units.
					ArrayList<String> unitsList = new ArrayList<String>(Arrays.asList(metric.getUnit().split("\\|"))); 
					
					Spinner unitSpinner = (Spinner) findViewById(R.id.metric_unit);
					ArrayAdapter<String> unitAdapter = new ArrayAdapter<String>(parent.getContext(), android.R.layout.simple_spinner_item, unitsList);
					unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					
					unitSpinner.setAdapter(unitAdapter);
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			
			// Set up what happens when the user clicks the button.
			Button okButton = (Button) findViewById(R.id.dialog_ok);
			okButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					BodyLogEntry logEntry = new BodyLogEntry();

					Spinner metric = (Spinner) findViewById(R.id.metric_types);
					String name = metric.getSelectedItem().toString();
					logEntry.setMetricIdentifier(dao.getMetricDetailsByName(name).getMetricIdentifier());
					
					Spinner unit = (Spinner) findViewById(R.id.metric_unit);
					logEntry.setUnit(unit.getSelectedItem().toString());
					
					EditText entry = (EditText) findViewById(R.id.body_entry);
					logEntry.setValue(Float.valueOf(entry.getText().toString()));
					
					// Send the entry.
					try {
						dao.addBodyLogEntry(logEntry);
					} catch (OAuthMessageSignerException e) {
						Log.e("BurnBot", e.getMessage());
					} catch (OAuthExpectationFailedException e) {
						Log.e("BurnBot", e.getMessage());
					} catch (ClientProtocolException e) {
						Log.e("BurnBot", e.getMessage());
					} catch (OAuthNotAuthorizedException e) {
						Log.e("BurnBot", e.getMessage());
					} catch (IOException e) {
						Log.e("BurnBot", e.getMessage());
					}
					
					// Notify the user.
					Toast toast = Toast.makeText(getApplicationContext(), "Progress Recorded", Toast.LENGTH_SHORT);
					toast.show();
					
				}
			});
		}
		
	}
	
}
