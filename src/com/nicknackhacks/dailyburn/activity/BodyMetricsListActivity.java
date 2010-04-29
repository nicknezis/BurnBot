package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.nicknackhacks.dailyburn.DailyBurnDroid;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.api.UserDao;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.User;

public class BodyMetricsListActivity extends ListActivity {

	private ProgressDialog progressDialog = null;
	private SharedPreferences pref;
	private BodyDao bodyDao;
	private UserDao userDao;
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
			Log.d(DailyBurnDroid.TAG,"Metric " + metric.getName() + ", Pro: " + metric.isPro());
			if(!metric.isPro() || userInfo.isPro())
			{
				Log.d(DailyBurnDroid.TAG,"Adding " + metric.getName());
				Map<String, String> map = new HashMap<String, String>();
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
		
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			HashMap<String, String> metric = (HashMap<String, String>)adapter.getItem(arg2);
			Log.d(DailyBurnDroid.TAG,"Metric: " + metric.get("Name") + " selected.");
			Intent intent = new Intent(BodyMetricsListActivity.this, BodyEntryListActivity.class);
			intent.putExtra("body_metric_identifier", metric.get("Identifier"));
			startActivity(intent);
		}
	};	
}
