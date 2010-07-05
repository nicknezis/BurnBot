package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.BodyEntryAdapter;
import com.nicknackhacks.dailyburn.api.AddBodyEntryDialog;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;

public class BodyEntryListActivity extends ListActivity {

	private static final int ADD_METRIC_DIALOG_ID = 0;
	private ProgressDialog progressDialog = null;
	private BodyEntryAdapter adapter;
	private BodyEntryAsyncTask viewEntries = new BodyEntryAsyncTask();
	private BodyDao bodyDao;
	protected boolean fetching;
	private String metricIdentifier;
	private String metricUnit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_entries);

		BurnBot app = (BurnBot) getApplication();
		bodyDao = new BodyDao(app);
		
		List<BodyLogEntry> entries = new ArrayList<BodyLogEntry>();
		this.adapter = new BodyEntryAdapter(this, R.layout.body_entry_row, entries);
		setListAdapter(this.adapter);

		metricIdentifier = getIntent().getStringExtra("body_metric_identifier");
		metricUnit = getIntent().getStringExtra("body_metric_unit");
		viewEntries.execute(metricIdentifier);		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
		FlurryAgent.onEvent("BodyEntryListActivity");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.body_metrics_context_menu, menu);
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create_metric_entry:
			FlurryAgent.onEvent("Click Create Metric Options Item");
			showDialog(ADD_METRIC_DIALOG_ID);
			return true;
		}
		return false;
	}

	@Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    	case ADD_METRIC_DIALOG_ID:
    		AddBodyEntryDialog dialog = new AddBodyEntryDialog(this,bodyDao);
    		return dialog;
    	}
    	return null;
    }
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case ADD_METRIC_DIALOG_ID:
			((AddBodyEntryDialog)dialog).setMetricIdentifier(metricIdentifier);
			((AddBodyEntryDialog)dialog).setMetricUnit(metricUnit);
		}
	}

	private class BodyEntryAsyncTask extends AsyncTask<String, Integer, List<BodyLogEntry>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fetching = true;
			progressDialog = ProgressDialog.show(BodyEntryListActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected List<BodyLogEntry> doInBackground(String... arg0) {
			return bodyDao.getBodyLogEntries(arg0[0]);
		}

		@Override
		protected void onPostExecute(List<BodyLogEntry> result) {
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					adapter.add(result.get(i));
				}
			}
			adapter.notifyDataSetChanged();
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		}

	}		
}
