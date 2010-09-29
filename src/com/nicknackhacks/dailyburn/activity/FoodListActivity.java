package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.flurry.android.FlurryAgent;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.google.ads.AdSenseSpec.ExpandDirection;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodAdapter;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.provider.FoodSuggestionProvider;

public class FoodListActivity extends ListActivity {

	private static final int FOOD_ENTRY_RESULT_CODE = 0;
	private static final String FOOD_ID_KEY = "FOOD_ID_KEY";
	private static final int[] IMAGE_IDS = { R.id.foodrow_Icon };
	private FoodAdapter adapter;
	private FoodDao foodDao;
	private String searchParam = null;
	private View toggledItem;
	private State mState;
	private SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodsearchresults);

		View v = findViewById(R.id.btn_title_refresh);
		v.setEnabled(false);
		v.setVisibility(View.INVISIBLE);
		
		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);

		setFoodDaoPreferences();

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			adapter = new FoodAdapter(this, R.layout.foodrow, mState.foods);
			if (mState.asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
				updateRefreshStatus();
				mState.asyncTask.attach(this);
			}
		} else {
			mState = new State(this, foodDao);
			adapter = new FoodAdapter(this, R.layout.foodrow,
					new ArrayList<Food>());
			if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
				searchParam = getIntent().getStringExtra(SearchManager.QUERY);
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		                FoodSuggestionProvider.AUTHORITY, FoodSuggestionProvider.MODE);
		        suggestions.saveRecentQuery(searchParam, null);
				LogHelper.LogD("Query: %s", searchParam);
			} else {
				searchParam = getIntent().getStringExtra("query");
				LogHelper.LogD("Food search : " + searchParam);
			}
			mState.asyncTask.execute(searchParam,
					String.valueOf(mState.pageNum));
		}
		ThumbnailAdapter thumbs = new ThumbnailAdapter(this, adapter,
				((BurnBot) getApplication()).getCache(), IMAGE_IDS);
		
		setListAdapter(thumbs);

		getListView().setOnItemClickListener(itemClickListener);
		getListView().setOnScrollListener(scrollListener);
		registerForContextMenu(getListView());
		
		GoogleAdView googleAdView = (GoogleAdView) findViewById(R.id.adview);
		AdSenseSpec adSenseSpec = BurnBot.getAdSpec();
		adSenseSpec.setExpandDirection(ExpandDirection.TOP);
		googleAdView.showAds(adSenseSpec);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.asyncTask.detach();
		return mState;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.foods_context_menu, menu);
		final MenuItem item = menu.findItem(R.id.menu_delete_favorite);
		item.setEnabled(false);
		item.setVisible(false);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Food food = null;
		switch (item.getItemId()) {
		case R.id.menu_add_favorite:
			FlurryAgent.onEvent("Click Add Favorite Context Item");
			food = adapter.getItem((int) info.id);
			LogHelper.LogD("Add Info ID: " + info.id + ", Food ID: "
					+ food.getId());
			try {
				foodDao.addFavoriteFood(food.getId());
			} catch (Exception e) {
				LogHelper.LogE(e.getMessage(), e);
			}
			return true;
		case R.id.menu_delete_favorite:
			FlurryAgent.onEvent("Click Delete Favorite Context Item");
			food = adapter.getItem((int) info.id);
			LogHelper.LogD("Delete Info ID: " + info.id + ", Food ID: "
					+ food.getId());
			try {
				foodDao.deleteFavoriteFood(food.getId());
			} catch (Exception e) {
				LogHelper.LogE(e.getMessage(), e);
			}
			return true;
		case R.id.menu_ate_this:
			FlurryAgent.onEvent("Click Ate This Context Item");
			food = adapter.getItem((int) info.id);
			Intent intent = new Intent(this, AddFoodLogEntryActivity.class);
			intent.putExtra("foodId", food.getId());
			intent.putExtra("servingSize", food.getServingSize());
			intent.putExtra("foodName", food.getName());
			startActivityForResult(intent, FOOD_ENTRY_RESULT_CODE);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	void setFoodDaoPreferences() {
		pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Boolean reverse = pref.getBoolean("food.search.reverse", false);
		String sortBy = pref.getString("food.search.sort_by", "best_match");
		String perPage = pref.getString("food.search.per_page", "10");
		foodDao.setPerPage(perPage);
		foodDao.setSortBy(sortBy);
		foodDao.setReverse(reverse.toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFoodDaoPreferences();
	}

	private void updateRefreshStatus() {
//		findViewById(R.id.btn_title_refresh).setVisibility(
//				mState.mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mState.mSyncing ? View.VISIBLE : View.GONE);
	}

	private static class State {
		public FoodAsyncTask asyncTask;
		public boolean mSyncing = false;
		public List<Food> foods;
		public int pageNum = 1;

		private State(FoodListActivity activity, FoodDao foodDao) {
			LogHelper.LogD("mSyncing = " + mSyncing + " (new State)");
			asyncTask = new FoodAsyncTask(activity, foodDao);
			foods = new ArrayList<Food>();
		}
	}

	private static class FoodAsyncTask extends
			AsyncTask<String, Void, List<Food>> {

		private FoodListActivity activity;
		private FoodDao foodDao;

		public FoodAsyncTask(FoodListActivity activity, FoodDao foodDao) {
			attach(activity);
			this.foodDao = foodDao;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.mState.mSyncing = true;
			LogHelper.LogD("mSyncing = " + activity.mState.mSyncing + " (onPreExecute)");
			activity.updateRefreshStatus();
		}

		@Override
		protected List<Food> doInBackground(String... params) {
			List<Food> result = null;
			int count = params.length;
			result = foodDao.search(params[0], params[1]);
			return result;
		}

		@Override
		protected void onPostExecute(List<Food> result) {
			super.onPostExecute(result);
			if (activity.toggledItem != null) {
				activity.toggledItem.findViewById(R.id.itemContent)
						.setVisibility(View.VISIBLE);
				activity.toggledItem.findViewById(R.id.itemLoading)
						.setVisibility(View.GONE);
			}
			if (result != null && result.size() > 0) {
				activity.mState.foods = result;
				activity.mState.pageNum++;
//				if (activity.toggledItem != null) {
//					activity.toggledItem.findViewById(R.id.itemContent)
//							.setVisibility(View.VISIBLE);
//					activity.toggledItem.findViewById(R.id.itemLoading)
//							.setVisibility(View.GONE);
//				}

				for (int i = 0; i < result.size(); i++) {
					LogHelper.LogD("Adding: " + result.get(i));
					activity.adapter.add(result.get(i));
				}
			}
			LogHelper.LogD("mSyncing = " + activity.mState.mSyncing + " (onPostExecute)");
			((BaseAdapter) activity.getListAdapter()).notifyDataSetChanged();
			activity.mState.mSyncing = false;
			activity.updateRefreshStatus();
		}

		void detach() {
			activity = null;
		}

		void attach(FoodListActivity activity) {
			this.activity = activity;
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Food selectedFood = adapter.getItem(arg2);
			// Food selectedFood = foods.get(arg2);
			BurnBot app = (BurnBot) FoodListActivity.this.getApplication();
			Intent intent = new Intent(
					"com.nicknackhacks.dailyburn.FOOD_DETAIL");
			// Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(selectedFood));
			intent.putExtra("selectedFood", key);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("Name", selectedFood.getName());
			params.put("Brand", selectedFood.getBrand());
			FlurryAgent.onEvent("Click Food List Item", params);
			startActivity(intent);
		}
	};

	private OnScrollListener scrollListener = new OnScrollListener() {

		private int priorFirst = -1;
		private int visible = 0;
		private int prevTotalItemCount = 0;

		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			LogHelper.LogD("onScroll: firstVisible=" + firstVisibleItem + ", visibleCount=" + visibleItemCount + ", totalCount=" + totalItemCount);
			// detect if last item is visible
			// if (action != null && action.contentEquals(SEARCH_FOOD)
			if (visibleItemCount < totalItemCount
					&& (firstVisibleItem + visibleItemCount >= totalItemCount)) {
				LogHelper.LogD("Passed the scroll check");
				// see if we have more results
				LogHelper.LogD("mSyncing = %s  (onScroll)", mState.mSyncing);
				LogHelper.LogD("TotalCount = %s , PreviousTotal: %s", totalItemCount, prevTotalItemCount);
				if (!mState.mSyncing && totalItemCount != prevTotalItemCount ) {
					prevTotalItemCount = totalItemCount;
						//firstVisibleItem != priorFirst) {
					LogHelper.LogD("Passed the second scroll check");
					LogHelper.LogD("firstVisibleItem = " + firstVisibleItem + ", priorFirst = " + priorFirst);
					priorFirst = firstVisibleItem;
					
					onLastListItemDisplayed(totalItemCount, visibleItemCount);
				}
			}
		}

		protected void onLastListItemDisplayed(int totalItemCount,
				int visibleItemCount) {
			LogHelper.LogD("onLastListItem: total: " + totalItemCount + ", visible: " + visibleItemCount);
			if (totalItemCount < 100) {
				// find last item in the list
				View item = getListView().getChildAt(visibleItemCount - 1);
				toggledItem = item;
				
				item.findViewById(R.id.itemContent).setVisibility(View.GONE);
				item.findViewById(R.id.itemLoading).setVisibility(View.VISIBLE);
				mState.asyncTask = new FoodAsyncTask(FoodListActivity.this,
						foodDao);
				mState.asyncTask.execute(searchParam,
						String.valueOf(mState.pageNum));
			}
		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

	};
}
