package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodLogEntryAdapter;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;

public class FoodLogEntriesActivity extends ListActivity {

	public static final int DETAIL_REQUEST_CODE = 1;
	private static final int[] IMAGE_IDS={R.id.foodrow_Icon};
	private static final int DATE_DIALOG_ID = 0;
	private ProgressDialog progressDialog = null;
	private FoodLogAsyncTask viewFoodLogs;
	private List<FoodLogEntry> entries;
	private FoodDao foodDao;
	Map<Integer,String> mealNameMap = null;
	private MergeAdapter mergeAdapter = new MergeAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodsearchresults);

		View v = findViewById(R.id.change_date_button);
		v.setVisibility(View.VISIBLE);
		
		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);

		mealNameMap = app.getMealNameMap();
		
		setListAdapter(mergeAdapter);

		viewFoodLogs = new FoodLogAsyncTask();
		viewFoodLogs.execute();
		
		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
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
	public void onDestroy() {
		super.onDestroy();	
//		mergeAdapter.close();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.foodentry_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_delete_foodentry:
			FlurryAgent.onEvent("Click Delete Food Context Item");
			FoodLogEntry entry = (FoodLogEntry) mergeAdapter.getItem(info.position);
			try {
				foodDao.deleteFoodLogEntry(entry.getId());
				entries.remove(entry);
				updateAdapter(entries);
			} catch (Exception e) {
				LogHelper.LogE(e.getMessage(), e);
			} 
			return true;
		default:
			return super.onContextItemSelected(item);
		}
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
        	FlurryAgent.onEvent("Click Change Date Options Item");
        	showDialog(DATE_DIALOG_ID);
        	return true;
        }
		return false;
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
			mergeAdapter = new MergeAdapter();
			FoodLogEntriesActivity.this.setListAdapter(mergeAdapter);
			viewFoodLogs = new FoodLogAsyncTask();
			viewFoodLogs.execute(year,monthOfYear,dayOfMonth);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			if(extras.getBoolean("itemDeleted")) {
				Long selectedEntryKey = extras.getLong("selectedEntry");
				BurnBot app = (BurnBot) this.getApplication();
				FoodLogEntry detailFoodEntry = (FoodLogEntry) app.objects.get(selectedEntryKey).get();
				entries.remove(detailFoodEntry);
				updateAdapter(entries);
				Toast toast = Toast.makeText(getApplicationContext(), detailFoodEntry.getFoodName() + " deleted.", Toast.LENGTH_SHORT);
				toast.show();
			}			
		}
	};

	private void updateAdapter(List<FoodLogEntry> result) {
		if (result != null && result.size() > 0) {
			entries = result;
			MergeAdapter tmpAdapter = new MergeAdapter();
			
			Map<Integer, List<FoodLogEntry>> foods = partitionByMeal(result);

			for (Entry<Integer, List<FoodLogEntry>> entry : foods.entrySet()) {
				FoodLogEntryAdapter adapter = new FoodLogEntryAdapter(this, R.layout.foodrow, entry.getValue());
				ThumbnailAdapter thumbs = new ThumbnailAdapter(this, adapter, 
						((BurnBot)getApplication()).getCache(),IMAGE_IDS);
				TextView tv = (TextView) getLayoutInflater().inflate(R.layout.header, null);
				tv.setText(mealNameMap.get(entry.getKey()));
				tmpAdapter.addView(tv);
				tmpAdapter.addAdapter(thumbs);
			}
			mergeAdapter = tmpAdapter;
			setListAdapter(mergeAdapter);
			//mergeAdapter.notifyDataSetChanged();
		}
	}

	private Map<Integer, List<FoodLogEntry>> partitionByMeal(
			List<FoodLogEntry> foods) {
		Map<Integer, List<FoodLogEntry>> result = new HashMap<Integer,
														List<FoodLogEntry>>();
		for (FoodLogEntry entry : foods) {
			int mealId = entry.getMealId();
			if(result.containsKey(mealId)) {
				List<FoodLogEntry> list = result.get(mealId);
				list.add(entry);
			} else {
				List<FoodLogEntry> list = new ArrayList<FoodLogEntry>();
				list.add(entry);
				result.put(mealId, list);
			}
		}
		return result;
	}
	
	private class FoodLogAsyncTask extends AsyncTask<Integer, Void, List<FoodLogEntry>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(FoodLogEntriesActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected List<FoodLogEntry> doInBackground(Integer... params) {
			List<FoodLogEntry> result = null;
			if (params.length == 0) {
				result = foodDao.getFoodLogEntries();
			} else if (params.length == 3) {
				result = foodDao.getFoodLogEntries(params[0].intValue(),
						params[1].intValue(), params[2].intValue());
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<FoodLogEntry> result) {
			super.onPostExecute(result);
			updateAdapter(result);
			progressDialog.dismiss();
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			FoodLogEntry selectedEntry = (FoodLogEntry) mergeAdapter.getItem(arg2);
			BurnBot app = (BurnBot) FoodLogEntriesActivity.this
					.getApplication();
			//Intent intent = new Intent("com.nicknackhacks.dailyburn.FOOD_LOG_DETAIL");
			Intent intent = new Intent(FoodLogEntriesActivity.this, FoodLogDetailActivity.class);
			// Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(selectedEntry));
			intent.putExtra("selectedEntry", key);
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("FoodName", selectedEntry.getFoodName());
			FlurryAgent.onEvent("Click Log List Item",params);
			(FoodLogEntriesActivity.this).startActivityForResult(intent, DETAIL_REQUEST_CODE);
//			startActivity(intent);
		}
	};
}
