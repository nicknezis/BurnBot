package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
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
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodAdapter;
import com.nicknackhacks.dailyburn.adapters.FoodCursorAdapter;
import com.nicknackhacks.dailyburn.api.AddFoodLogEntryDialog;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.provider.BurnBotContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodContract;

public class FoodFavoritesListActivity extends ListActivity {

	private static final int FOOD_ENTRY_DIALOG_ID = 0;
	private static final String FOOD_ID_KEY = "FOOD_ID_KEY";
	public static final String SEARCH_FOOD = "com.nicknackhacks.dailyburn.SEARCH_FOOD";
	public static final String LIST_FAVORITE = "com.nicknackhacks.dailyburn.LIST_FAVORITE_FOODS";
	private static final int[] IMAGE_IDS = { R.id.foodrow_Icon };
	private ProgressDialog progressDialog = null;
	private FoodAdapter adapter;
	private FoodDao foodDao;
	private String action = null;
	private String searchParam = null;
	private int foodId;
	private View toggledItem;
	private State mState;
	private SharedPreferences pref;
	private FoodCursorAdapter cursorAdapter;
	private Cursor cursor;
//	private FoodContentObserver observer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodsearchresults);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);

		setFoodDaoPreferences();

		mState = (State) getLastNonConfigurationInstance();
        final boolean previousState = mState != null;
//        
        if(previousState) {
//        	adapter = new FoodAdapter(this, R.layout.foodrow, mState.foods);
    		cursor = mState.cursor;
        	if(mState.asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
        		startProgressDialog();
        		mState.asyncTask.attach(this);
        	}
        } else {
        	mState = new State(this, foodDao);
//        	adapter = new FoodAdapter(this, R.layout.foodrow, new ArrayList<Food>());
//        	action = this.getIntent().getAction();
//    		if (action != null && action.contentEquals(SEARCH_FOOD)) {
//    			searchParam = getIntent().getStringExtra("query");
//    			BurnBot.LogD("Food search : " + searchParam);
//    			mState.asyncTask.execute("search", searchParam, String.valueOf(mState.pageNum));
//    		} else if (action != null && action.contentEquals(LIST_FAVORITE)) {
    			BurnBot.LogD("Favorite Foods");
    			cursor = managedQuery(FoodContract.FAVORITES_URI, null, null, null, null);
//    			cursorAdapter = new FoodCursorAdapter(this, R.layout.foodrow, cursor);
    			mState.asyncTask.execute("favorite");
//    		}	
        }
        cursorAdapter = new FoodCursorAdapter(this, R.layout.foodrow, cursor);
		ThumbnailAdapter thumbs = new ThumbnailAdapter(this, this.cursorAdapter,
				((BurnBot) getApplication()).getCache(), IMAGE_IDS);
		setListAdapter(thumbs);

		getListView().setOnItemClickListener(itemClickListener);
//		getListView().setOnScrollListener(scrollListener);
		registerForContextMenu(getListView());
	}

//	private class FoodContentObserver extends ContentObserver {
//
//		public FoodContentObserver(Handler handler) {
//			super(handler);
//		}
//
//		@Override
//		public void onChange(boolean selfChange) {
//			super.onChange(selfChange);
//			cursor.requery();
//			//updateActivityFromCursor(cursor);
//		}
//	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        // Clear any strong references to this Activity, we'll reattach to
        // handle events on the other side.
        mState.asyncTask.detach();
        mState.cursor = cursorAdapter.getCursor();
        return mState;
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		if (BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
		FlurryAgent.onEvent("FoodListActivity");
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
//		if (getIntent().getAction().contentEquals("search")) {
//			final MenuItem item = menu.findItem(R.id.menu_delete_favorite);
//			item.setEnabled(false);
//			item.setVisible(false);
//		}
//		if (getIntent().getAction().contentEquals("favorite")) {
			final MenuItem item = menu.findItem(R.id.menu_add_favorite);
			item.setEnabled(false);
			item.setVisible(false);
//		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Food food = null;
		switch (item.getItemId()) {
		case R.id.menu_add_favorite:
			FlurryAgent.onEvent("Click Add Favorite Context Item");
			food = adapter.getItem((int) info.id);
			BurnBot.LogD("Add Info ID: " + info.id + ", Food ID: "
					+ food.getId());
			try {
				foodDao.addFavoriteFood(food.getId());
			} catch (Exception e) {
				BurnBot.LogE(e.getMessage(), e);
			}
			return true;
		case R.id.menu_delete_favorite:
			FlurryAgent.onEvent("Click Delete Favorite Context Item");
			food = adapter.getItem((int) info.id);
			BurnBot.LogD("Delete Info ID: " + info.id + ", Food ID: "
					+ food.getId());
			try {
				foodDao.deleteFavoriteFood(food.getId());
			} catch (Exception e) {
				BurnBot.LogE(e.getMessage(), e);
			}
			return true;
		case R.id.menu_ate_this:
			FlurryAgent.onEvent("Click Ate This Context Item");
			food = adapter.getItem((int) info.id);
			// Bundle bundle = new Bundle();
			// bundle.putInt(FOOD_ID_KEY, food.getId());
			this.foodId = food.getId();
			showDialog(FOOD_ENTRY_DIALOG_ID);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FOOD_ENTRY_DIALOG_ID:
			final AddFoodLogEntryDialog dialog = new AddFoodLogEntryDialog(
					this, foodDao);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case FOOD_ENTRY_DIALOG_ID:
			// ((AddFoodLogEntryDialog)dialog).setFoodId(args.getInt(FOOD_ID_KEY));
			((AddFoodLogEntryDialog) dialog).setFoodId(foodId);
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
//		observer = new FoodContentObserver(new Handler());
//		BurnBot.LogD("Registering " + observer);
//		getContentResolver().registerContentObserver(FoodContract.FAVORITES_URI, true, observer);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		getContentResolver().unregisterContentObserver(observer);
	}

	private void startProgressDialog() {
		progressDialog = ProgressDialog.show(this, "Please wait...",
				"Retrieving data ...", true);
	}

	private void stopProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private static class State {
		public FoodAsyncTask asyncTask;
		public Cursor cursor;
		public boolean fetching = false;
//		public List<Food> foods;
//		public int pageNum = 1;

		private State(FoodFavoritesListActivity activity, FoodDao foodDao) {
			asyncTask = new FoodAsyncTask(activity, foodDao);
//			foods = new ArrayList<Food>();
		}
	}

	private static class FoodAsyncTask extends
			AsyncTask<String, Void, List<Food>> {

		private FoodFavoritesListActivity activity;
		private FoodDao foodDao;

		public FoodAsyncTask(FoodFavoritesListActivity activity, FoodDao foodDao) {
			attach(activity);
			this.foodDao = foodDao;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.mState.fetching = true;
			activity.startProgressDialog();
		}

		@Override
		protected List<Food> doInBackground(String... params) {
			List<Food> result = null;
			int count = params.length;
			if (count > 0) {
				if (params[0].contentEquals("search")) {
					if (count == 3)
						result = foodDao.search(params[1], params[2]);
				} else if (params[0].contentEquals("favorite")) {
					result = foodDao.getFavoriteFoods();
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<Food> result) {
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				try {
					activity.getContentResolver().applyBatch(
							BurnBotContract.CONTENT_AUTHORITY, 
							foodDao.getFavoriteFoodsOps(result));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				activity.mState.foods = result;
//				activity.mState.pageNum++;
//				if (activity.toggledItem != null) {
//					activity.toggledItem.findViewById(R.id.itemContent)
//							.setVisibility(View.VISIBLE);
//					activity.toggledItem.findViewById(R.id.itemLoading)
//							.setVisibility(View.GONE);
//				}

//				for (int i = 0; i < result.size(); i++) {
//					activity.adapter.add(result.get(i));
//				}
			}
			activity.mState.fetching = false;
			activity.cursor.requery();
			((BaseAdapter) activity.getListAdapter()).notifyDataSetChanged();
			activity.stopProgressDialog();
		}

		void detach() {
			activity = null;
		}

		void attach(FoodFavoritesListActivity activity) {
			this.activity = activity;
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Food selectedFood = adapter.getItem(arg2);
			// Food selectedFood = foods.get(arg2);
			BurnBot app = (BurnBot) FoodFavoritesListActivity.this.getApplication();
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

//	private OnScrollListener scrollListener = new OnScrollListener() {
//
//		private int priorFirst = -1;
//		private int visible = 0;
//
//		public void onScroll(AbsListView view, int firstVisibleItem,
//				int visibleItemCount, int totalItemCount) {
//			// detect if last item is visible
//			if (action != null && action.contentEquals(SEARCH_FOOD)
//					&& visibleItemCount < totalItemCount
//					&& (firstVisibleItem + visibleItemCount == totalItemCount)) {
//				// see if we have more results
//				if (!mState.fetching && firstVisibleItem != priorFirst) {
//					priorFirst = firstVisibleItem;
//					onLastListItemDisplayed(totalItemCount, visibleItemCount);
//				}
//			}
//		}
//
//		protected void onLastListItemDisplayed(int totalItemCount,
//				int visibleItemCount) {
//			if (totalItemCount < 100) {
//				// find last item in the list
//				View item = getListView().getChildAt(visibleItemCount - 1);
//				toggledItem = item;
//				item.findViewById(R.id.itemContent).setVisibility(View.GONE);
//				item.findViewById(R.id.itemLoading).setVisibility(View.VISIBLE);
//				mState.asyncTask = new FoodAsyncTask(FoodFavoritesListActivity.this,
//						foodDao);
//				mState.asyncTask.execute("search", searchParam,
//						String.valueOf(mState.pageNum));
//			}
//		}
//
//		public void onScrollStateChanged(AbsListView view, int scrollState) {
//			// TODO Auto-generated method stub
//
//		}

//	};
}
