package org.nicknack.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.adapters.FoodLogEntryAdapter;
import org.nicknack.dailyburn.api.FoodDao;
import org.nicknack.dailyburn.model.FoodLogEntry;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

public class FoodLogEntriesActivity extends ListActivity {

	private static final int[] IMAGE_IDS={R.id.foodrow_Icon};
	private static final int DATE_DIALOG_ID = 0;
	private ProgressDialog progressDialog = null;
	private FoodLogEntryAdapter adapter;
	private FoodLogAsyncTask viewFoodLogs;
	private SharedPreferences pref;
	private FoodDao foodDao;
	private ThumbnailAdapter thumbs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodsearchresults);
		pref = this.getSharedPreferences("dbdroid", 0);
		// boolean isAuthenticated = pref.getBoolean("isAuthed", false);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret));
		consumer.setTokenWithSecret(token, secret);
		foodDao = new FoodDao(new DefaultHttpClient(), consumer);

		ArrayList<FoodLogEntry> entries = new ArrayList<FoodLogEntry>();
		this.adapter = new FoodLogEntryAdapter(this, R.layout.foodrow, entries);
		this.thumbs = new ThumbnailAdapter(this, this.adapter, 
				((DailyBurnDroid)getApplication()).getCache(),IMAGE_IDS);
		setListAdapter(this.thumbs);

		viewFoodLogs = new FoodLogAsyncTask();
		viewFoodLogs.execute(null);
		
		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
		thumbs.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.foodlogentries, menu);
		return true;
	}
	
	 /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.foodlog_changedate:
        	showDialog(DATE_DIALOG_ID);
        	return true;
        }
		return false;
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.foodentry_context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  FoodLogEntry entry = this.adapter.getItem((int) info.id);
		  switch (item.getItemId()) {
		  case R.id.menu_delete_foodentry:
			  try {
				foodDao.deleteFoodLogEntry(entry.getId());
			} catch (Exception e) {
				Log.e(DailyBurnDroid.TAG,e.getMessage());
				e.printStackTrace();
			} 	
			return true;
		  default:
		    return super.onContextItemSelected(item);
		  }
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
			ProgressDialog progressDialog = ProgressDialog.show(
					FoodLogEntriesActivity.this, "Please wait...",
					"Retrieving data ...", true);
			List<FoodLogEntry> results = foodDao.getFoodLogEntries(year,
					monthOfYear, dayOfMonth);
			progressDialog.dismiss();
			adapter.clear();
			if (results != null && results.size() > 0) {
				for (FoodLogEntry entry : results) {
					adapter.add(entry);
				}
			}
			thumbs.notifyDataSetChanged();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class FoodLogAsyncTask extends AsyncTask<Integer, Integer, List<FoodLogEntry>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(FoodLogEntriesActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected List<FoodLogEntry> doInBackground(Integer... params) {
			List<FoodLogEntry> result = null;
			if (params == null) {
				result = foodDao.getFoodLogEntries();
			} else if (params.length == 3) {
				result = foodDao.getFoodLogEntries(params[0].intValue(),
						params[1].intValue(), params[2].intValue());
			}
			// result = foodDao.getFoodLogEntries();
			return result;
		}

		@Override
		protected void onPostExecute(List<FoodLogEntry> result) {
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				thumbs.notifyDataSetChanged();
				for (FoodLogEntry entry : result) {
					adapter.add(entry);
				}
			}
			thumbs.notifyDataSetChanged();
			progressDialog.dismiss();
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			FoodLogEntry selectedEntry = adapter.getItem(arg2);
			DailyBurnDroid app = (DailyBurnDroid) FoodLogEntriesActivity.this
					.getApplication();
			Intent intent = new Intent("com.nicknack.dailyburn.FOOD_LOG_DETAIL");
			// Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(selectedEntry));
			intent.putExtra("selectedEntry", key);
			startActivity(intent);
		}
	};
}
