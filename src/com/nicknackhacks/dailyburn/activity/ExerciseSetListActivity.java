package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.ExerciseSetAdapter;
import com.nicknackhacks.dailyburn.api.ExerciseDao;
import com.nicknackhacks.dailyburn.model.ExerciseSet;

public class ExerciseSetListActivity extends ListActivity {

	private static final int ADD_METRIC_DIALOG_ID = 0;
	private ProgressDialog progressDialog = null;
	private ExerciseSetAdapter adapter;
	private State mState;
	private ExerciseDao exerciseDao;
	private String metricIdentifier;
	private String metricUnit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_entries);

		BurnBot app = (BurnBot) getApplication();
		exerciseDao = new ExerciseDao(app);
		
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
            mState = new State(this, exerciseDao);         
    		mState.asyncTask.execute(metricIdentifier);
        }
    	adapter = new ExerciseSetAdapter(this, R.layout.exercise_row, mState.entries);
    	setListAdapter(adapter);
    	
    	GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
		googleAdView.showAds(adSenseSpec);
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        // Clear any strong references to this Activity, we'll reattach to
        // handle events on the other side.
        mState.asyncTask.detach();
        int count = adapter.getCount();
        mState.entries = new ArrayList<ExerciseSet>(count);
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
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}
	
//	/* Creates the menu items */
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.body_metrics_context_menu, menu);
//		return true;
//	}
//
//	/* Handles item selections */
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.menu_create_metric_entry:
//			FlurryAgent.onEvent("Click Create Metric Options Item");
//			showDialog(ADD_METRIC_DIALOG_ID);
//			return true;
//		}
//		return false;
//	}

//	@Override
//    protected Dialog onCreateDialog(int id) {
//    	switch(id) {
//    	case ADD_METRIC_DIALOG_ID:
//    		AddBodyEntryDialog dialog = new AddBodyEntryDialog(this,exerciseDao);
//    		return dialog;
//    	}
//    	return null;
//    }
	
//	@Override
//	protected void onPrepareDialog(int id, Dialog dialog) {
//		super.onPrepareDialog(id, dialog);
//		switch (id) {
//		case ADD_METRIC_DIALOG_ID:
//			((AddBodyEntryDialog)dialog).setMetricIdentifier(metricIdentifier);
//			((AddBodyEntryDialog)dialog).setMetricUnit(metricUnit);
//		}
//	}

	private void startProgressDialog() {
		progressDialog = ProgressDialog.show(this,
				"Please wait...", "Retrieving data ...", true);
	}
	
	private void stopProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
    /**
     * State specific to {@link ExerciseSetListActivity} that is held between configuration
     * changes. Any strong {@link Activity} references <strong>must</strong> be
     * cleared before {@link #onRetainNonConfigurationInstance()}, and this
     * class should remain {@code static class}.
     */
    private static class State {
        public ExerciseSetAsyncTask asyncTask;
        public List<ExerciseSet> entries;

        private State(ExerciseSetListActivity activity, ExerciseDao exerciseDao) {
        	asyncTask = new ExerciseSetAsyncTask(activity, exerciseDao);
        	entries = new ArrayList<ExerciseSet>();
        }
    }

	private static class ExerciseSetAsyncTask extends AsyncTask<String, Integer, List<ExerciseSet>> {

		private ExerciseSetListActivity activity;
		private ExerciseDao exerciseDao;
		
		public ExerciseSetAsyncTask(ExerciseSetListActivity activity, ExerciseDao exerciseDao) {
			this.exerciseDao = exerciseDao;
			attach(activity);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.startProgressDialog();
		}

		@Override
		protected List<ExerciseSet> doInBackground(String... arg0) {
			return exerciseDao.getExerciseSets();//getBodyLogEntries(arg0[0]);
		}

		@Override
		protected void onPostExecute(List<ExerciseSet> result) {
			super.onPostExecute(result);
			ExerciseSetAdapter adapter = (ExerciseSetAdapter) activity.getListAdapter();
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

		void attach(ExerciseSetListActivity activity) {
			this.activity=activity;
		}
	}		
}
