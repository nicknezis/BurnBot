package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.AddBodyEntryDialog;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.api.UserDao;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.User;

public class BodyMetricsListActivity extends ListActivity {

	private static final int ADD_METRIC_DIALOG_ID = 0; 
	private ProgressDialog progressDialog = null;
	private State mState;
	private BodyDao bodyDao;
	private UserDao userDao;
	private Map<String, String> selectedMetric;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_metrics);

		BurnBot app = (BurnBot) getApplication();
		bodyDao = new BodyDao(app);
		userDao = new UserDao(app);
		
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
            mState = new State(this, bodyDao, userDao);         
    		mState.asyncTask.execute();
        }
		
		SimpleAdapter adapter = new SimpleAdapter(this,mState.mapping,android.R.layout.simple_list_item_1,
				new String[]{"Name"},new int[]{android.R.id.text1});
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        // Clear any strong references to this Activity, we'll reattach to
        // handle events on the other side.
        mState.asyncTask.detach();
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

	@Override
	protected void onResume() {
		super.onResume();
	}
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.body_metrics_context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_create_metric_entry:
			selectedMetric = (Map<String, String>) getListAdapter().getItem((int) info.id);
			FlurryAgent.onEvent("Click Create Body Metric Context Item",selectedMetric);
			showDialog(ADD_METRIC_DIALOG_ID);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
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
			((AddBodyEntryDialog)dialog).setMetricIdentifier(selectedMetric.get("Identifier"));
			((AddBodyEntryDialog)dialog).setMetricUnit(selectedMetric.get("Unit"));
		}
	}
	
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
     * State specific to {@link BodyMetricsListActivity} that is held between configuration
     * changes. Any strong {@link Activity} references <strong>must</strong> be
     * cleared before {@link #onRetainNonConfigurationInstance()}, and this
     * class should remain {@code static class}.
     */
    private static class State {
        public BodyMetricsAsyncTask asyncTask;
        public List<Map<String, String>> mapping;

        private State(BodyMetricsListActivity activity, BodyDao bodyDao, UserDao userDao) {
        	asyncTask = new BodyMetricsAsyncTask(activity, bodyDao, userDao);
        	mapping = new ArrayList<Map<String,String>>();
        }
    }

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Map<String, String> metric = (Map<String, String>)getListAdapter().getItem(arg2);
			LogHelper.LogD("Metric: " + metric.get("Name") + " selected.");
			Intent intent = new Intent(BodyMetricsListActivity.this, BodyEntryListActivity.class);
			intent.putExtra("body_metric_identifier", metric.get("Identifier"));
			intent.putExtra("body_metric_unit", metric.get("Unit"));
			FlurryAgent.onEvent("Click Body Metric List Item",metric);
			startActivity(intent);
		}
	};
	
	private static class BodyMetricsAsyncTask extends AsyncTask<Void, Void, List<Map<String, String>>> {

		private BodyMetricsListActivity activity;
		private BodyDao bodyDao;
		private UserDao userDao;

		public BodyMetricsAsyncTask(BodyMetricsListActivity activity, BodyDao bodyDao, UserDao userDao) {
			this.bodyDao = bodyDao;
			this.userDao = userDao;
			attach(activity);	
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.startProgressDialog();
		}

		@Override
		protected List<Map<String, String>> doInBackground(Void... unused) {
			User userInfo = userDao.getUserInfo();
			List<BodyMetric> metrics = bodyDao.getBodyMetrics();
			List<Map<String,String>> mapping = new ArrayList<Map<String,String>>();
			for(BodyMetric metric : metrics) {
				LogHelper.LogD("Metric " + metric.getName() + ", Pro: " + metric.isPro());
				if(!metric.isPro() || userInfo.isPro())
				{
					LogHelper.LogD("Adding " + metric.getName());
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("Name", metric.getName());
					map.put("Pro", String.valueOf(metric.isPro()));
					map.put("ID", String.valueOf(metric.getId()));
					map.put("Identifier", metric.getMetricIdentifier());
					map.put("Unit", metric.getUnit());
					mapping.add(map);
				}
			}
			return mapping;
		}

		@Override
		protected void onPostExecute(List<Map<String, String>> mapping) {
			super.onPostExecute(mapping);
			SimpleAdapter adapter = new SimpleAdapter(activity,mapping,android.R.layout.simple_list_item_1,
					new String[]{"Name"},new int[]{android.R.id.text1});
			activity.mState.mapping = mapping;
			activity.setListAdapter(adapter);
			adapter.notifyDataSetChanged();
			activity.stopProgressDialog();
		}
		
		void detach() {
			activity=null;
		}

		void attach(BodyMetricsListActivity activity) {
			this.activity=activity;
		}
	}		
}
