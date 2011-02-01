package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.ExerciseSetAdapter;
import com.nicknackhacks.dailyburn.api.ExerciseDao;
import com.nicknackhacks.dailyburn.model.ExerciseSet;

public class ExerciseSetListActivity extends ListActivity {

	private static final int DATE_DIALOG_ID = 0;
	private ProgressDialog progressDialog = null;
	private ExerciseSetAdapter adapter;
	private State mState;
	private ExerciseDao exerciseDao;
//	private String metricIdentifier;
//	private String metricUnit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exercisesets_list);

		BurnBot app = (BurnBot) getApplication();
		exerciseDao = new ExerciseDao(app);
		
		mState = (State) getLastNonConfigurationInstance();
        final boolean previousState = mState != null;

        if (previousState) {
        	if(mState.asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
        		startProgressDialog();
        		mState.asyncTask.attach(this);
        	} else if(mState.asyncTask.getStatus() == AsyncTask.Status.PENDING) {
        		mState.asyncTask.execute();
        	}
        } else {
            mState = new State(this, exerciseDao);         
    		mState.asyncTask.execute();
        }
    	adapter = new ExerciseSetAdapter(this, R.layout.exercise_row, mState.entries);
    	setListAdapter(adapter);
    	
//    	GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
//		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
//		googleAdView.showAds(adSenseSpec);
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
	
    public void onClickChangeDate(View v) {
    	FlurryAgent.onEvent("Click Change Date Button");
    	showDialog(DATE_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	Calendar cal = Calendar.getInstance();
    	int cYear = cal.get(Calendar.YEAR);
    	int cMonth = cal.get(Calendar.MONTH);
    	int cDay = cal.get(Calendar.DAY_OF_MONTH);
    	switch(id) {
    	case DATE_DIALOG_ID:
    		return new DatePickerDialog(this, dateSetListener, cYear, cMonth, cDay);
    	}
    	return null;
    }

	private OnDateSetListener dateSetListener = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
//			mergeAdapter = new MergeAdapter();
//			adapter = new ExerciseSetAdapter(activity, R.id.exerciserow, items)
			ExerciseSetListActivity activity = ExerciseSetListActivity.this;
//			activity.setListAdapter(mergeAdapter);
			mState.asyncTask.cancel(false);
			mState.asyncTask = new ExerciseSetAsyncTask(activity,activity.exerciseDao);
			mState.asyncTask.execute(year,monthOfYear,dayOfMonth);
		}
	};


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

	private static class ExerciseSetAsyncTask extends AsyncTask<Integer, Void, List<ExerciseSet>> {

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
		protected List<ExerciseSet> doInBackground(Integer... params) {
			List<ExerciseSet> result = null;
			if (params.length == 0) {
				result = exerciseDao.getExerciseSets();
			} else if (params.length == 3) {
				result = exerciseDao.getExerciseSets(params[0].intValue(),
						params[1].intValue(), params[2].intValue());
			} else {
				LogHelper.LogE("Wrong number of parameters passed to ExerciseSetAsyncTask");
			}
			return result;
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
