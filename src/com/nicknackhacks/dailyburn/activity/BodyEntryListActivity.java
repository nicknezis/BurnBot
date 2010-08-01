package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
	private State mState;
	private BodyDao bodyDao;
	private String metricIdentifier;
	private String metricUnit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_entries);

		BurnBot app = (BurnBot) getApplication();
		bodyDao = new BodyDao(app);
		
		metricIdentifier = getIntent().getStringExtra("body_metric_identifier");
		metricUnit = getIntent().getStringExtra("body_metric_unit");

		mState = (State) getLastNonConfigurationInstance();
        final boolean previousState = mState != null;

        if (previousState) {
        	if(mState.asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
        		startProgressDialog();
        		mState.asyncTask.attach(this);
        	} else if(mState.asyncTask.getStatus() == AsyncTask.Status.PENDING) {
        		mState.asyncTask.execute(metricIdentifier);
        	}
        } else {
            mState = new State(this, bodyDao);         
    		mState.asyncTask.execute(metricIdentifier);
        }
    	adapter = new BodyEntryAdapter(this, R.layout.body_entry_row, mState.entries);
    	setListAdapter(adapter);
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        // Clear any strong references to this Activity, we'll reattach to
        // handle events on the other side.
        mState.asyncTask.detach();
        int count = adapter.getCount();
        mState.entries = new ArrayList<BodyLogEntry>(count);
        for(int position = 0; position < count; position++) {
        	mState.entries.add(adapter.getItem(position));
        }
        return mState;
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

	private void startProgressDialog() {
		progressDialog = ProgressDialog.show(BodyEntryListActivity.this,
				"Please wait...", "Retrieving data ...", true);
	}
	
	private void stopProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
    /**
     * State specific to {@link BodyEntryListActivity} that is held between configuration
     * changes. Any strong {@link Activity} references <strong>must</strong> be
     * cleared before {@link #onRetainNonConfigurationInstance()}, and this
     * class should remain {@code static class}.
     */
    private static class State {
        public BodyEntryAsyncTask asyncTask;
        public List<BodyLogEntry> entries;

        private State(BodyEntryListActivity activity, BodyDao bodyDao) {
        	asyncTask = new BodyEntryAsyncTask(activity, bodyDao);
        	entries = new ArrayList<BodyLogEntry>();
        }
    }

	private static class BodyEntryAsyncTask extends AsyncTask<String, Integer, List<BodyLogEntry>> {

		private BodyEntryListActivity activity;
		private BodyDao bodyDao;
		
		public BodyEntryAsyncTask(BodyEntryListActivity activity, BodyDao bodyDao) {
			this.bodyDao = bodyDao;
			attach(activity);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.startProgressDialog();
		}

		@Override
		protected List<BodyLogEntry> doInBackground(String... arg0) {
			return bodyDao.getBodyLogEntries(arg0[0]);
		}

		@Override
		protected void onPostExecute(List<BodyLogEntry> result) {
			super.onPostExecute(result);
			BodyEntryAdapter adapter = (BodyEntryAdapter) activity.getListAdapter();
			if (result != null && result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					adapter.add(result.get(i));
				}
			}
			adapter.notifyDataSetChanged();
			activity.stopProgressDialog();
		}

		void detach() {
			activity=null;
		}

		void attach(BodyEntryListActivity activity) {
			this.activity=activity;
		}
	}		
}
