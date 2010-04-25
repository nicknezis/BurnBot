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
	//private List<Food> foods = null;
	private BodyEntryAdapter adapter;
	private BodyEntryAsyncTask viewEntries;
	private SharedPreferences pref;
	private BodyDao bodyDao;
//	private String action = null;
//	private String searchParam = null;
	private View toggledItem;
	protected boolean fetching;
	
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
		bodyDao = new BodyDao(new DefaultHttpClient(), consumer);
		
		List<BodyLogEntry> entries = new ArrayList<BodyLogEntry>();
		this.adapter = new BodyEntryAdapter(this, R.layout.body_entry_row, entries);
		setListAdapter(this.adapter);

		viewEntries = new BodyEntryAsyncTask();
		viewEntries.execute();
		
		//getListView().setOnItemClickListener(itemClickListener);
		//getListView().setOnScrollListener(scrollListener);
		//registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
		//thumbs.close();
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

	private class BodyEntryAsyncTask extends AsyncTask<Integer, Integer, List<BodyLogEntry>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fetching = true;
			progressDialog = ProgressDialog.show(BodyEntryListActivity.this,
					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected List<BodyLogEntry> doInBackground(Integer... arg0) {
			return bodyDao.getBodyLogEntries();
		}

		@Override
		protected void onPostExecute(List<BodyLogEntry> result) {
			super.onPostExecute(result);
			//foods = result;
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
		
//	private OnItemClickListener itemClickListener = new OnItemClickListener() {
//		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//				long arg3) {
//			Food selectedFood = adapter.getItem(arg2);
//			//Food selectedFood = foods.get(arg2);
//			DailyBurnDroid app = (DailyBurnDroid) BodyEntryListActivity.this
//					.getApplication();
//			Intent intent = new Intent("com.nicknackhacks.dailyburn.FOOD_DETAIL");
//			// Make key for selected Food item
//			Long key = System.nanoTime();
//			app.objects.put(key, new WeakReference<Object>(selectedFood));
//			intent.putExtra("selectedFood", key);
//			startActivity(intent);
//		}
//	};
//	
//
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
//
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
