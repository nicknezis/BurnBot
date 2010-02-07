package org.nicknack.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.DrawableManager;
import org.nicknack.dailyburn.api.FoodDao;
import org.nicknack.dailyburn.model.FoodLogEntry;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FoodLogEntries extends ListActivity {

	private ProgressDialog progressDialog = null;
	private List<FoodLogEntry> entries = null;
	private FoodLogAdapter adapter;
	private FoodLogAsyncTask viewFoodLogs;
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
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		foodDao = new FoodDao(new DefaultHttpClient(), consumer);

		this.entries = new ArrayList<FoodLogEntry>();
		this.adapter = new FoodLogAdapter(this, R.layout.foodrow, entries);
		setListAdapter(this.adapter);

		viewFoodLogs = new FoodLogAsyncTask();
		viewFoodLogs.execute(null);
		
		//		String action = this.getIntent().getAction();
//		if (action != null
//				&& action.contentEquals("com.nicknack.dailyburn.SEARCH_FOODS")) {
//			String param = getIntent().getStringExtra("query");
//			Log.d("dailyburndroid", "Food search : " + param);
//			viewFoods.execute("search", param);
//		} else if (action != null
//				&& action.contentEquals("com.nicknack.dailyburn.LIST_FAVORITE_FOODS")) {
//			Log.d("dailyburndroid", "Favorite Foods");
//			viewFoods.execute("favorite");
//		}

		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
	}

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v,
//			ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.foods_context_menu, menu);
//        if(getIntent().getAction().contentEquals("search")) {
//        	final MenuItem item = menu.findItem(R.id.menu_delete_favorite);
//        	item.setEnabled(false);
//        	item.setVisible(false);
//        }
//        if(getIntent().getAction().contentEquals("favorite")) {
//        	final MenuItem item = menu.findItem(R.id.menu_add_favorite);
//        	item.setEnabled(false);
//        	item.setVisible(false);
//        }
//	}
	
//	public boolean onContextItemSelected(MenuItem item) {
//		  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//		  FoodLogEntry entry = null;
//		  switch (item.getItemId()) {
//		    return true;
//		  case R.id.menu_delete_favorite:
//		    return true;
//		  default:
//		    return super.onContextItemSelected(item);
//		  }
//		}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class FoodLogAsyncTask extends AsyncTask<String, Integer, List<FoodLogEntry>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(FoodLogEntries.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected List<FoodLogEntry> doInBackground(String... params) {
			List<FoodLogEntry> result = null;
			//int count = params.length;
			result = foodDao.getFoodLogEntries();			
			return result;
		}

		@Override
		protected void onPostExecute(List<FoodLogEntry> result) {
			super.onPostExecute(result);
			entries = result;
			if (entries != null && entries.size() > 0) {
				adapter.notifyDataSetChanged();
				for (int i = 0; i < entries.size(); i++)
					adapter.add(entries.get(i));
			}
			progressDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
	}

	private class FoodLogAdapter extends ArrayAdapter<FoodLogEntry> {

		private class ViewHolder {
			TextView name;
			TextView size;
			TextView nutrition1;
			TextView nutrition2;
			ImageView icon;
		}

		private List<FoodLogEntry> items;

		public FoodLogAdapter(Context context, int textViewResourceId,
				List<FoodLogEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.foodrow, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) v.findViewById(R.id.foodrow_Icon);
				holder.name = (TextView) v.findViewById(R.id.foodrow_Name);
				holder.size = (TextView) v.findViewById(R.id.foodrow_Size);
				holder.nutrition1 = (TextView) v
						.findViewById(R.id.foodrow_Nutrition1);
				holder.nutrition2 = (TextView) v
						.findViewById(R.id.foodrow_Nutrition2);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}
			FoodLogEntry f = items.get(position);
			if (f.getFoodPictureUrl() != null) {
				ImageView foodIcon = (ImageView) holder.icon;
				dManager.fetchDrawableOnThread("http://dailyburn.com"
						+ f.getFoodPictureUrl(), foodIcon);
			}
			if (f != null) {
				final TextView nameRow = (TextView) holder.name;
				final TextView sizeRow = (TextView) holder.size;
				final TextView nutRow1 = (TextView) holder.nutrition1;
				final TextView nutRow2 = (TextView) holder.nutrition2;
				if (nameRow != null) {
					String txt = "Name: " + f.getFoodName();
					nameRow.setText(txt);
				}
				if (sizeRow != null) {
					sizeRow.setText(String.valueOf(f.getServingsEaten()));
				}
				if (nutRow1 != null) {
					String txt = "Cals: " + f.getCaloriesEaten() + ", Fat: "
							+ f.getTotalFatEaten() + "g";
					nutRow1.setText(txt);
				}
				if (nutRow2 != null) {
					String txt = "Carbs: " + f.getTotalCarbsEaten() + "g, Protein: "
							+ f.getProteinEaten() + "g";
					nutRow2.setText(txt);
				}
			}
			return v;
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			FoodLogEntry selectedEntry = adapter.getItem(arg2);
			DailyBurnDroid app = (DailyBurnDroid) FoodLogEntries.this
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
