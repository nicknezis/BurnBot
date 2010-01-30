package org.nicknack.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.DrawableManager;
import org.nicknack.dailyburn.api.FoodDao;
import org.nicknack.dailyburn.model.Food;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FoodSearchResults extends ListActivity {

	private ProgressDialog progressDialog = null;
	private List<Food> foods = null;
	private FoodAdapter adapter;
	private FoodAsyncTask viewFoods;
	private SharedPreferences pref;
	private FoodDao foodDao;
	private DrawableManager dManager = new DrawableManager();

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
		
		this.getListView().setOnItemClickListener(itemClickListener);
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
			ImageView foodIcon = (ImageView)v.findViewById(R.id.foodrow_Icon);
			dManager.fetchDrawableOnThread("http://dailyburn.com"+f.getNormalUrl(), foodIcon);
			if (f != null) {
				final TextView nameRow = (TextView) v.findViewById(R.id.foodrow_Name);
				final TextView sizeRow = (TextView) v.findViewById(R.id.foodrow_Size);
				final TextView nutRow1 = (TextView) v.findViewById(R.id.foodrow_Nutrition1);
				final TextView nutRow2 = (TextView) v.findViewById(R.id.foodrow_Nutrition2);
				if (nameRow != null) {
					String txt = "Name: " + f.getName();
					if(f.getBrand() != null)
						txt = txt + " by " + f.getBrand();
					nameRow.setText(txt);
				}
				if (sizeRow != null) {
					sizeRow.setText(f.getServingSize());
				}
				if (nutRow1 != null) {
					String txt = "Cal: " + f.getCalories() + 
								 ", Fat: " + f.getTotalFat() + "g"; 
					nutRow1.setText(txt);
				}
				if (nutRow2 != null) {
					String txt = "Carbs: " + f.getTotalCarbs() + 
								 "g, Protein: " + f.getProtein() + "g";
					nutRow2.setText(txt);
				}
			}
			return v;
		}
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Food selectedFood = foods.get(arg2);
			DailyBurnDroid app = (DailyBurnDroid) FoodSearchResults.this.getApplication();
			Intent intent = new Intent("com.nicknack.dailyburn.FOOD_DETAIL");
			//Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(selectedFood));
			intent.putExtra("selectedFood", key);
			//Make key for selected food icon
//			key = System.nanoTime();
			//Object o = arg0.getItemAtPosition(arg2);
//			View view = arg0.getChildAt(arg2);
//			ImageView imageView = (ImageView) view.findViewById(R.id.icon);
//			Drawable icon = imageView.getDrawable();
//			app.objects.put(key, new WeakReference<Object>(icon));
//			intent.putExtra("selectedFoodImage", key);
			startActivity(intent);
		}		
	};
}
