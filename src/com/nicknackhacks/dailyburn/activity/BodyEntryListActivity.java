package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.BodyEntryAdapter;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;

public class BodyEntryListActivity extends ListActivity {

	private ProgressDialog progressDialog = null;
	private BodyEntryAdapter adapter;
	private BodyEntryAsyncTask viewEntries;
	private SharedPreferences pref;
	private BodyDao bodyDao;
	private View toggledItem;
	protected boolean fetching;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_entries);
		pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		bodyDao = new BodyDao(new DefaultHttpClient(), consumer);
		
		List<BodyLogEntry> entries = new ArrayList<BodyLogEntry>();
		this.adapter = new BodyEntryAdapter(this, R.layout.body_entry_row, entries);
		setListAdapter(this.adapter);

		viewEntries = new BodyEntryAsyncTask();
		String bodyMetricIdentifier = getIntent().getStringExtra("body_metric_identifier");
		viewEntries.execute(bodyMetricIdentifier);		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
		bodyDao.shutdown();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.foods_context_menu, menu);
        if(getIntent().getAction().contentEquals("search")) {
        	final MenuItem item = menu.findItem(R.id.menu_delete_favorite);
        	item.setEnabled(false);
        	item.setVisible(false);
        }
        if(getIntent().getAction().contentEquals("favorite")) {
        	final MenuItem item = menu.findItem(R.id.menu_add_favorite);
        	item.setEnabled(false);
        	item.setVisible(false);
        }
	}	

	@Override
	protected void onResume() {
		super.onResume();
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
