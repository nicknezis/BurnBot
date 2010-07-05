package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodAdapter;
import com.nicknackhacks.dailyburn.api.AddFoodLogEntryDialog;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;

public class FoodListActivity extends ListActivity {

	private static final int FOOD_ENTRY_DIALOG_ID = 0;
	private static final String FOOD_ID_KEY = "FOOD_ID_KEY";
	public static final String SEARCH_FOOD = "com.nicknackhacks.dailyburn.SEARCH_FOOD";
	public static final String LIST_FAVORITE = "com.nicknackhacks.dailyburn.LIST_FAVORITE_FOODS";
	private static final int[] IMAGE_IDS={R.id.foodrow_Icon};
	private ProgressDialog progressDialog = null;
	private List<Food> foods = null;
	private FoodAdapter adapter;
	private ThumbnailAdapter thumbs;
	private FoodAsyncTask viewFoods;
	private FoodDao foodDao;
	private String action = null;
	private String searchParam = null;
	private int pageNum = 1;
	private int foodId;
	private View toggledItem;
	protected boolean fetching;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodsearchresults);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
		
		this.foods = new ArrayList<Food>();
		this.adapter = new FoodAdapter(this, R.layout.foodrow, foods);
		//this.endless = new EndlessFoodAdapter(this, foodDao, adapter, action, searchParam);		
		this.thumbs = new ThumbnailAdapter(this, this.adapter, 
				((BurnBot)getApplication()).getCache(),IMAGE_IDS);
		setListAdapter(this.thumbs);

		viewFoods = new FoodAsyncTask();
		action = this.getIntent().getAction();
		if (action != null && action.contentEquals(SEARCH_FOOD)) {
			searchParam = getIntent().getStringExtra("query");
			BurnBot.LogD( "Food search : " + searchParam);
			viewFoods.execute("search",searchParam);
		} else if (action != null && action.contentEquals(LIST_FAVORITE)) {
			BurnBot.LogD( "Favorite Foods");
			viewFoods.execute("favorite");
		}
		
		progressDialog = ProgressDialog.show(FoodListActivity.this,
				"Please wait...", "Retrieving data ...", true);

		getListView().setOnItemClickListener(itemClickListener);
		getListView().setOnScrollListener(scrollListener);
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
		FlurryAgent.onEvent("FoodListActivity");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
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
			  FlurryAgent.onEvent("Click Add Favorite Context Item");
  			  food = adapter.getItem((int) info.id);
			  BurnBot.LogD("Add Info ID: " + info.id + ", Food ID: " + food.getId());
			  try {
				foodDao.addFavoriteFood(food.getId());
			} catch (Exception e) {
				BurnBot.LogE(e.getMessage(), e);
			} 
		    return true;
		  case R.id.menu_delete_favorite:
			  FlurryAgent.onEvent("Click Delete Favorite Context Item");
			  food = adapter.getItem((int) info.id);
			  BurnBot.LogD("Delete Info ID: " + info.id + ", Food ID: " + food.getId());
			  try {
				foodDao.deleteFavoriteFood(food.getId());
			} catch (Exception e) {
				BurnBot.LogE(e.getMessage(), e);
			}
		    return true;
		  case R.id.menu_ate_this:
			  FlurryAgent.onEvent("Click Ate This Context Item");
			  food = adapter.getItem((int) info.id);
//			  Bundle bundle = new Bundle();
//			  bundle.putInt(FOOD_ID_KEY, food.getId());
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
//			((AddFoodLogEntryDialog)dialog).setFoodId(args.getInt(FOOD_ID_KEY));
			((AddFoodLogEntryDialog)dialog).setFoodId(foodId);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	private class FoodAsyncTask extends AsyncTask<String, Void, List<Food>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fetching = true;
//			progressDialog = ProgressDialog.show(FoodSearchResults.this,
//					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected List<Food> doInBackground(String... params) {
			List<Food> result = null;
			int count = params.length;
			if (count > 0) {
				if (params[0].contentEquals("search")) {
					if(count == 2)
						result = foodDao.search(params[1]);
					else if(count == 3)
						result = foodDao.search(params[1],params[2]);
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
				pageNum++;
				if(toggledItem != null) {
					toggledItem.findViewById(R.id.itemContent).setVisibility(View.VISIBLE);
					toggledItem.findViewById(R.id.itemLoading).setVisibility(View.GONE);
				}
				thumbs.notifyDataSetChanged();
				//endless.notifyDataSetChanged();
				//adapter.notifyDataSetChanged();
				for (int i = 0; i < foods.size(); i++) {
					adapter.add(foods.get(i));
					//adapter.add(foods.get(i));
				}
				//adapter.notifyDataSetChanged();
			}
			thumbs.notifyDataSetChanged();
			//endless.notifyDataSetChanged();
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			fetching = false;
		}
	}
		
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Food selectedFood = adapter.getItem(arg2);
			//Food selectedFood = foods.get(arg2);
			BurnBot app = (BurnBot) FoodListActivity.this
					.getApplication();
			Intent intent = new Intent("com.nicknackhacks.dailyburn.FOOD_DETAIL");
			// Make key for selected Food item
			Long key = System.nanoTime();
			app.objects.put(key, new WeakReference<Object>(selectedFood));
			intent.putExtra("selectedFood", key);
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("Name", selectedFood.getName());
			params.put("Brand", selectedFood.getBrand());
			FlurryAgent.onEvent("Click Food List Item",params);
			startActivity(intent);
		}
	};
	

	private OnScrollListener scrollListener = new OnScrollListener() {

		private int priorFirst = -1;
		private int visible = 0;
		
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// detect if last item is visible
			if (action != null && action.contentEquals(SEARCH_FOOD)
					&& visibleItemCount < totalItemCount
					&& (firstVisibleItem + visibleItemCount == totalItemCount)) {
				// see if we have more results
				if (!fetching && firstVisibleItem != priorFirst) {
					priorFirst = firstVisibleItem;
					onLastListItemDisplayed(totalItemCount, visibleItemCount);
				}
			}
		}

		protected void onLastListItemDisplayed(int totalItemCount, int visibleItemCount) {
			if (totalItemCount < 100) {
			// find last item in the list
			View item = getListView().getChildAt(visibleItemCount - 1);
			toggledItem = item;
			item.findViewById(R.id.itemContent).setVisibility(View.GONE);
			item.findViewById(R.id.itemLoading).setVisibility(View.VISIBLE);
			viewFoods = new FoodAsyncTask();
			viewFoods.execute("search",searchParam,String.valueOf(pageNum));
			}
			}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
