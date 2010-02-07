package org.nicknack.dailyburn.activity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.ClientProtocolException;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		foodDao = new FoodDao(new DefaultHttpClient(), consumer);

		this.foods = new ArrayList<Food>();
		this.adapter = new FoodAdapter(this, R.layout.foodrow, foods);
		setListAdapter(this.adapter);

		viewFoods = new FoodAsyncTask();
		String action = this.getIntent().getAction();
		if (action != null
				&& action.contentEquals("com.nicknack.dailyburn.SEARCH_FOODS")) {
			String param = getIntent().getStringExtra("query");
			Log.d("dailyburndroid", "Food search : " + param);
			viewFoods.execute("search", param);
		} else if (action != null
				&& action.contentEquals("com.nicknack.dailyburn.LIST_FAVORITE_FOODS")) {
			Log.d("dailyburndroid", "Favorite Foods");
			viewFoods.execute("favorite");
		}

		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
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
	
	public boolean onContextItemSelected(MenuItem item) {
		  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  Food food = null;
		  switch (item.getItemId()) {
		  case R.id.menu_add_favorite:
			  food = this.adapter.getItem((int) info.id);
			  //food = foods.get((int) info.id);
			  Log.d("dailyburndroid","Add Info ID: " + info.id + ", Food ID: " + food.getId());
			  try {
				foodDao.addFavoriteFood(food.getId());
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return true;
		  case R.id.menu_delete_favorite:
			  food = foods.get((int) info.id);
			  Log.d("dailyburndroid","Delete Info ID: " + info.id + ", Food ID: " + food.getId());
			  try {
				foodDao.deleteFavoriteFood(food.getId());
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return true;
		  default:
		    return super.onContextItemSelected(item);
		  }
		}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class FoodAsyncTask extends AsyncTask<String, Integer, List<Food>> {

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
			if (count > 0) {
				if (params[0].contentEquals("search")) {
					result = foodDao.search(params[1]);
				} else if (params[0].contentEquals("favorite")) {
					result = foodDao.getFavoriteFoods();
				}
			}
//			result = foodDao.search(params[0]);
			return result;
		}

		@Override
		protected void onPostExecute(List<Food> result) {
			super.onPostExecute(result);
			foods = result;
			if (foods != null && foods.size() > 0) {
				adapter.notifyDataSetChanged();
				for (int i = 0; i < foods.size(); i++)
					adapter.add(foods.get(i));
			}
			progressDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
	}

	private class FoodAdapter extends ArrayAdapter<Food> {

		private class ViewHolder {
			TextView name;
			TextView size;
			TextView nutrition1;
			TextView nutrition2;
			ImageView icon;
		}

		private List<Food> items;

		public FoodAdapter(Context context, int textViewResourceId,
				List<Food> items) {
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
			Food f = items.get(position);
			if (f.getThumbUrl() != null) {
				ImageView foodIcon = (ImageView) holder.icon;
				dManager.fetchDrawableOnThread("http://dailyburn.com"
						+ f.getThumbUrl(), foodIcon);
			}
			if (f != null) {
				final TextView nameRow = (TextView) holder.name;
				final TextView sizeRow = (TextView) holder.size;
				final TextView nutRow1 = (TextView) holder.nutrition1;
				final TextView nutRow2 = (TextView) holder.nutrition2;
				if (nameRow != null) {
					String txt = "Name: " + f.getName();
					if (f.getBrand() != null)
						txt = txt + " by " + f.getBrand();
					nameRow.setText(txt);
				}
				if (sizeRow != null) {
					sizeRow.setText(f.getServingSize());
				}
				if (nutRow1 != null) {
					String txt = "Cal: " + f.getCalories() + ", Fat: "
							+ f.getTotalFat() + "g";
					nutRow1.setText(txt);
				}
				if (nutRow2 != null) {
					String txt = "Carbs: " + f.getTotalCarbs() + "g, Protein: "
							+ f.getProtein() + "g";
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
			DailyBurnDroid app = (DailyBurnDroid) FoodSearchResults.this
					.getApplication();
			Intent intent = new Intent("com.nicknack.dailyburn.FOOD_DETAIL");
			// Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(selectedFood));
			intent.putExtra("selectedFood", key);
			startActivity(intent);
		}
	};
}
