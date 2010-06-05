package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.api.UserDao;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.model.User;

public class BodyMetricsListActivity extends ListActivity {

	private static final int ADD_METRIC_DIALOG_ID = 0; 
	private ProgressDialog progressDialog = null;
	private SharedPreferences pref;
	private BodyDao bodyDao;
	private UserDao userDao;
	private HashMap<String,String> selectedMetric;
	private SimpleAdapter adapter;
	protected boolean fetching;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_metrics);
		pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		bodyDao = new BodyDao(new DefaultHttpClient(), consumer);
		userDao = new UserDao(new DefaultHttpClient(), consumer);
		
		User userInfo = userDao.getUserInfo();
		List<BodyMetric> metrics = bodyDao.getBodyMetrics();
		List<Map<String,String>> mapping = new ArrayList<Map<String,String>>();
		for(BodyMetric metric : metrics) {
			Log.d(BurnBot.TAG,"Metric " + metric.getName() + ", Pro: " + metric.isPro());
			if(!metric.isPro() || userInfo.isPro())
			{
				Log.d(BurnBot.TAG,"Adding " + metric.getName());
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("Name", metric.getName());
				map.put("Pro", String.valueOf(metric.isPro()));
				map.put("ID", String.valueOf(metric.getId()));
				map.put("Identifier", metric.getMetricIdentifier());
				map.put("Unit", metric.getUnit());
				mapping.add(map);
			}
		}
		adapter = new SimpleAdapter(this,mapping,android.R.layout.simple_list_item_1,
				new String[]{"Name"},new int[]{android.R.id.text1}); 
		setListAdapter(this.adapter);
		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
		userDao.shutdown();
		bodyDao.shutdown();
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
			selectedMetric = (HashMap<String, String>) adapter.getItem((int) info.id);
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
        	Calendar cal = Calendar.getInstance();
        	int cYear = cal.get(Calendar.YEAR);
        	int cMonth = cal.get(Calendar.MONTH);
        	int cDay = cal.get(Calendar.DAY_OF_MONTH);

    		final Dialog dialog = new Dialog(this);

    		dialog.setContentView(R.layout.add_body_entry);
    		dialog.setTitle("Entry:");

    		DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.DatePicker);
    		datePicker.init(cYear,cMonth,cDay, null);
    		dialog.setCancelable(true);
    		((Button)dialog.findViewById(R.id.dialog_ok)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
//					Log.d(DailyBurnDroid.TAG,"OK: " + FoodDetailActivity.this.mYear + "-" + 
//							FoodDetailActivity.this.mMonthOfYear + ", Serv: " + 
//							((EditText)dialog.findViewById(R.id.servings_eaten)).getText());
					String value = ((EditText)dialog.findViewById(R.id.body_entry)).getText().toString();
					DatePicker datePicker = (DatePicker)dialog.findViewById(R.id.DatePicker);
					try {
						bodyDao.addBodyLogEntry(selectedMetric.get("Identifier"),
												value,
												selectedMetric.get("Unit"));
//						foodDao.addFoodLogEntry(detailFood.getId(), servings_eaten, 
//												datePicker.getYear(), 
//												datePicker.getMonth(), 
//												datePicker.getDayOfMonth());
					} catch (Exception e) {
						Log.e(BurnBot.TAG, e.getMessage());
						e.printStackTrace();
					} 
				}
			});
    		((Button)dialog.findViewById(R.id.dialog_cancel)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
				}
			});
    		return dialog;
    	}
    	return null;
    }
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			HashMap<String, String> metric = (HashMap<String, String>)adapter.getItem(arg2);
			Log.d(BurnBot.TAG,"Metric: " + metric.get("Name") + " selected.");
			Intent intent = new Intent(BodyMetricsListActivity.this, BodyEntryListActivity.class);
			intent.putExtra("body_metric_identifier", metric.get("Identifier"));
			startActivity(intent);
		}
	};	
}
