package com.nicknackhacks.dailyburn.activity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.nicknackhacks.dailyburn.DailyBurnDroid;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.BodyDao;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.Food;

public class BodyMetricsListActivity extends ListActivity {

//	public static final String SEARCH_FOODS = "com.nicknackhacks.dailyburn.SEARCH_FOODS";
//	public static final String LIST_FAVORITE = "com.nicknackhacks.dailyburn.LIST_FAVORITE_FOODS";
	//private static final int[] IMAGE_IDS={R.id.foodrow_Icon};
	private ProgressDialog progressDialog = null;
	//private List<BodyMetric> metrics = null;
	//private FoodAdapter adapter;
	//private ThumbnailAdapter thumbs;
	//private MetricsAsyncTask viewMetrics;
	private SharedPreferences pref;
	private BodyDao bodyDao;
	private SimpleAdapter adapter;
	//private String action = null;
	//private String searchParam = null;
	//private int pageNum = 1;
	//private View toggledItem;
	protected boolean fetching;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_metrics);
		pref = this.getSharedPreferences("dbdroid", 0);
		// boolean isAuthenticated = pref.getBoolean("isAuthed", false);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		bodyDao = new BodyDao(new DefaultHttpClient(), consumer);
		
		//this.foods = new ArrayList<Food>();
		//this.adapter = new FoodAdapter(this, R.layout.foodrow, foods);
		//this.endless = new EndlessFoodAdapter(this, foodDao, adapter, action, searchParam);		
//		this.thumbs = new ThumbnailAdapter(this, this.adapter, 
//				((DailyBurnDroid)getApplication()).getCache(),IMAGE_IDS);
		List<BodyMetric> metrics = bodyDao.getBodyMetrics();
		List<Map<String,String>> mapping = new ArrayList<Map<String,String>>();
		for(BodyMetric metric : metrics) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("Name", metric.getName());
			map.put("Pro", String.valueOf(metric.isPro()));
			map.put("ID", String.valueOf(metric.getId()));
			map.put("identifier", metric.getMetricIdentifier());
			map.put("Unit", metric.getUnit());
			mapping.add(map);
		}
		adapter = new SimpleAdapter(this,mapping,android.R.layout.simple_list_item_1,
				new String[]{"Name","Unit"},new int[]{android.R.id.text1}); 
		setListAdapter(this.adapter);

//		viewFoods = new FoodAsyncTask();
//		action = this.getIntent().getAction();
//		if (action != null && action.contentEquals(SEARCH_FOODS)) {
//			searchParam = getIntent().getStringExtra("query");
//			Log.d(DailyBurnDroid.TAG, "Food search : " + searchParam);
//			viewFoods.execute("search",searchParam);
//		} else if (action != null && action.contentEquals(LIST_FAVORITE)) {
//			Log.d("dailyburndroid", "Favorite Foods");
//			viewFoods.execute("favorite");
//		}
		
//		progressDialog = ProgressDialog.show(BodyMetricsListActivity.this,
//				"Please wait...", "Retrieving data ...", true);
//
//		getListView().setOnItemClickListener(itemClickListener);
//		getListView().setOnScrollListener(scrollListener);
//		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
		//thumbs.close();
		bodyDao.shutdown();
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
//		  Food food = null;
//		  switch (item.getItemId()) {
//		  case R.id.menu_add_favorite:
//  			  food = adapter.getItem((int) info.id);
//			  Log.d(DailyBurnDroid.TAG,"Add Info ID: " + info.id + ", Food ID: " + food.getId());
//			  try {
//				foodDao.addFavoriteFood(food.getId());
//			} catch (OAuthMessageSignerException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OAuthExpectationFailedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OAuthNotAuthorizedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClientProtocolException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		    return true;
//		  case R.id.menu_delete_favorite:
//			  food = adapter.getItem((int) info.id);
//			  Log.d("dailyburndroid","Delete Info ID: " + info.id + ", Food ID: " + food.getId());
//			  try {
//				foodDao.deleteFavoriteFood(food.getId());
//			} catch (OAuthMessageSignerException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OAuthExpectationFailedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OAuthNotAuthorizedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClientProtocolException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		    return true;
//		  default:
//		    return super.onContextItemSelected(item);
//		  }
//		}

	@Override
	protected void onResume() {
		super.onResume();
	}

//	private class MetricsAsyncTask extends AsyncTask<String, Integer, List<Food>> {
//
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			fetching = true;
////			progressDialog = ProgressDialog.show(FoodSearchResults.this,
////					"Please wait...", "Retrieving data ...", true);
//		}
//
//		@Override
//		protected List<Food> doInBackground(String... params) {
//			List<Food> result = null;
//			int count = params.length;
//			if (count > 0) {
//				if (params[0].contentEquals("search")) {
//					if(count == 2)
//						result = foodDao.search(params[1]);
//					else if(count == 3)
//						result = foodDao.search(params[1],params[2]);
//				} else if (params[0].contentEquals("favorite")) {
//					result = foodDao.getFavoriteFoods();
//				}
//			}
////			result = foodDao.search(params[0]);
//			return result;
//		}
//
//		@Override
//		protected void onPostExecute(List<Food> result) {
//			super.onPostExecute(result);
//			foods = result;
//			if (foods != null && foods.size() > 0) {
//				pageNum++;
//				if(toggledItem != null) {
//					toggledItem.findViewById(R.id.itemContent).setVisibility(View.VISIBLE);
//					toggledItem.findViewById(R.id.itemLoading).setVisibility(View.GONE);
//				}
//				thumbs.notifyDataSetChanged();
//				//endless.notifyDataSetChanged();
//				//adapter.notifyDataSetChanged();
//				for (int i = 0; i < foods.size(); i++) {
//					adapter.add(foods.get(i));
//					//adapter.add(foods.get(i));
//				}
//				//adapter.notifyDataSetChanged();
//			}
//			thumbs.notifyDataSetChanged();
//			//endless.notifyDataSetChanged();
//			if(progressDialog != null) {
//				progressDialog.dismiss();
//				progressDialog = null;
//			}
//			fetching = false;
//		}
//	}
		
//	private OnItemClickListener itemClickListener = new OnItemClickListener() {
//		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//				long arg3) {
//			Food selectedFood = adapter.getItem(arg2);
//			//Food selectedFood = foods.get(arg2);
//			DailyBurnDroid app = (DailyBurnDroid) BodyMetricsListActivity.this
//					.getApplication();
//			Intent intent = new Intent("com.nicknackhacks.dailyburn.FOOD_DETAIL");
//			// Make key for selected Food item
//			Long key = System.nanoTime();
//			app.objects.put(key, new WeakReference<Object>(selectedFood));
//			intent.putExtra("selectedFood", key);
//			startActivity(intent);
//		}
//	};
	

//	private OnScrollListener scrollListener = new OnScrollListener() {
//
//		private int priorFirst = -1;
//		private int visible = 0;
//		
//		public void onScroll(AbsListView view, int firstVisibleItem,
//				int visibleItemCount, int totalItemCount) {
//			// detect if last item is visible
//			if (action != null && action.contentEquals(SEARCH_FOODS)
//					&& visibleItemCount < totalItemCount
//					&& (firstVisibleItem + visibleItemCount == totalItemCount)) {
//				// see if we have more results
//				if (!fetching && firstVisibleItem != priorFirst) {
//					priorFirst = firstVisibleItem;
//					onLastListItemDisplayed(totalItemCount, visibleItemCount);
//				}
//			}
//		}

//		protected void onLastListItemDisplayed(int totalItemCount, int visibleItemCount) {
//			if (totalItemCount < 100) {
//			// find last item in the list
//			View item = getListView().getChildAt(visibleItemCount - 1);
//			toggledItem = item;
//			item.findViewById(R.id.itemContent).setVisibility(View.GONE);
//			item.findViewById(R.id.itemLoading).setVisibility(View.VISIBLE);
//			viewFoods = new FoodAsyncTask();
//			viewFoods.execute("search",searchParam,String.valueOf(pageNum));
//			}
//			}
//
//		public void onScrollStateChanged(AbsListView view, int scrollState) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	};
}
