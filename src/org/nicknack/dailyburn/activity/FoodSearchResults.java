package org.nicknack.dailyburn.activity;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.FoodDao;
import org.nicknack.dailyburn.model.Food;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FoodSearchResults extends ListActivity {

	private ProgressDialog progressDialog = null;
	private List<Food> foods = null;
	private FoodAdapter adapter;
	private FoodAsyncTask viewFoods;
	private SharedPreferences pref;
	private FoodDao foodDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodsearchresults);
		pref = this.getSharedPreferences("dbdroid", 0);
		// boolean isAuthenticated = pref.getBoolean("isAuthed", false);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		DefaultOAuthConsumer consumer = new DefaultOAuthConsumer(
				// "1YHdpiXLKmueriS5v7oS2w",
				// "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k",
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		foodDao = new FoodDao(new DefaultHttpClient(), consumer);

		this.foods = new ArrayList<Food>();
		this.adapter = new FoodAdapter(this, R.layout.foodrow, foods);
		setListAdapter(this.adapter);

		String param = getIntent().getStringExtra("query");
		Log.d("dailyburndroid", "Food search : " + param);
		
		viewFoods = new FoodAsyncTask();
		viewFoods.execute(param);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class FoodAsyncTask extends AsyncTask<String,Integer,List<Food>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(FoodSearchResults.this,
					"Please wait...", "Retrieving data ...", true);
		}
		
		@Override
		protected List<Food> doInBackground(String... params) {
			List<Food> result = null;
			int count = params.length;
			if(count > 0)
				result = foodDao.search(params[0]);
			return result;
		}
		
		@Override
		protected void onPostExecute(List<Food> result) {
			super.onPostExecute(result);
			foods = result;
			if(foods != null && foods.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<foods.size();i++)
                adapter.add(foods.get(i));
            }
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
		}
	}
	
	private class FoodAdapter extends ArrayAdapter<Food> {

		private List<Food> items;

		public FoodAdapter(Context context, int textViewResourceId,
				List<Food> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.foodrow, null);
			}
			Food f = items.get(position);
			if (f != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText("Name: " + f.getName());
				}
				if (bt != null) {
					bt.setText("Brand: " + f.getBrand());
				}
			}
			return v;
		}
	}
}
