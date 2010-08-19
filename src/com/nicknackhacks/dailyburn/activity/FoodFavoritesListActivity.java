package com.nicknackhacks.dailyburn.activity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodCursorAdapter;
import com.nicknackhacks.dailyburn.api.AddFoodLogEntryDialog;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.provider.BurnBotContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodContract;

public class FoodFavoritesListActivity extends ListActivity {

	private static final int FOOD_ENTRY_DIALOG_ID = 0;
	// private static final String FOOD_ID_KEY = "FOOD_ID_KEY";
	private static final int[] IMAGE_IDS = { R.id.foodrow_Icon };
	private FoodDao foodDao;
	private int foodId;
	private State mState;
	private SharedPreferences pref;
	private FoodCursorAdapter cursorAdapter;
	private Cursor cursor;

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
		if (previousState) {
			if (mState.cursor == null) {
				cursor = getContentResolver().query(FoodContract.FAVORITES_URI,
						null, null, null, null);
			} else {
				cursor = mState.cursor;
				cursor.requery();
			}

			if (mState.asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
				mState.asyncTask.attach(this);
			}
		} else {
			mState = new State(this, foodDao);
			BurnBot.LogD("Favorite Foods");
			cursor = getContentResolver().query(FoodContract.FAVORITES_URI,
					null, null, null, null);
			mState.asyncTask.execute("favorite");
		}
		startManagingCursor(cursor);
		cursorAdapter = new FoodCursorAdapter(this, R.layout.foodrow, cursor);
		ThumbnailAdapter thumbs = new ThumbnailAdapter(this, cursorAdapter,
				((BurnBot) getApplication()).getCache(), IMAGE_IDS);
		setListAdapter(thumbs);

		getListView().setOnItemClickListener(itemClickListener);
		registerForContextMenu(getListView());
		updateRefreshStatus();
	}

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				mState.mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mState.mSyncing ? View.VISIBLE : View.GONE);
	}

	public void onRefresh(View v) {
		switch (mState.asyncTask.getStatus()) {
		case PENDING:
			mState.asyncTask.execute();
			return;
		case FINISHED:
			mState.asyncTask = new FoodAsyncTask(this, foodDao);
			mState.asyncTask.execute();
			return;
		case RUNNING:
			return;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.asyncTask.detach();
		mState.cursor = cursorAdapter.getCursor();
		stopManagingCursor(mState.cursor);
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

		final MenuItem item = menu.findItem(R.id.menu_add_favorite);
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
			food = (Food) cursorAdapter.getItem((int) info.id);
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
			food = (Food) cursorAdapter.getItem((int) info.id);
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
			food = (Food) cursorAdapter.getItem((int) info.id);
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
	}

	private static class State {
		public FoodAsyncTask asyncTask;
		public Cursor cursor;
		public boolean mSyncing = false;

		private State(FoodFavoritesListActivity activity, FoodDao foodDao) {
			asyncTask = new FoodAsyncTask(activity, foodDao);
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
			activity.mState.mSyncing = true;
			activity.updateRefreshStatus();
			// activity.startProgressDialog();
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
					BurnBot.LogE(
							"RemoteException while applying operations to the ContentResolver",
							e);
				} catch (OperationApplicationException e) {
					BurnBot.LogE("ContentProviderOperation failed.", e);
				}
			}
			activity.cursorAdapter.getCursor().requery();
			((BaseAdapter) activity.getListAdapter()).notifyDataSetChanged();
			activity.mState.mSyncing = false;
			activity.updateRefreshStatus();
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
			Food selectedFood = (Food) cursorAdapter.getItem(arg2);
			// Food selectedFood = foods.get(arg2);
			BurnBot app = (BurnBot) FoodFavoritesListActivity.this
					.getApplication();
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

}
