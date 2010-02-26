package com.nicknackhacks.dailyburn.adapters;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.activity.FoodListActivity;
import com.nicknackhacks.dailyburn.api.DrawableManager;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.Food;

public class EndlessFoodAdapter extends EndlessAdapter {
	
	private FoodListActivity activity;
	private FoodDao foodDao;
	private AtomicInteger pageNum = new AtomicInteger(1);
	private String searchParam;
	private String action;
	private List<Food> result;

	public EndlessFoodAdapter(FoodListActivity activity, FoodDao foodDao, 
									ListAdapter wrapped, String action, String searchParam) {
		super(wrapped);
		
		this.activity = activity;
		this.foodDao = foodDao;
		this.searchParam = searchParam;
		this.action = action;
//		rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
//				0.5f, Animation.RELATIVE_TO_SELF,
//				0.5f);
//rotate.setDuration(600);
//rotate.setRepeatMode(Animation.RESTART);
//rotate.setRepeatCount(Animation.INFINITE);
	}

	private RotateAnimation rotate=null;
	
//	DemoAdapter(ArrayList<String> list) {
//		super(new ArrayAdapter<String>(EndlessAdapterDemo.this,
//																		R.layout.row,
//																		android.R.id.text1,
//																		list));
//		
//		rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
//																0.5f, Animation.RELATIVE_TO_SELF,
//																0.5f);
//		rotate.setDuration(600);
//		rotate.setRepeatMode(Animation.RESTART);
//		rotate.setRepeatCount(Animation.INFINITE);
//	}
	
	protected View getPendingView(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) activity
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View row=inflater.inflate(R.layout.foodrow, null);
		
		View child=row.findViewById(R.id.itemContent);
		child.setVisibility(View.GONE);
		
		child=row.findViewById(R.id.itemLoading);
		child.setVisibility(View.VISIBLE);
		
		
		//child.startAnimation(rotate);
		
		return(row);
	}
	
	protected void rebindPendingView(int position, View row) {
		View child=row.findViewById(R.id.itemContent);
		child.setVisibility(View.VISIBLE);
		
		FoodWrapper wrapper = new FoodWrapper(row);
		row.setTag(wrapper);
		wrapper.populateFrom((Food)getWrappedAdapter().getItem(position));
		DrawableManager dm = new DrawableManager();
		dm.fetchDrawableOnThread(wrapper.getIcon().getTag().toString(), wrapper.getIcon());
		//this.notifyDataSetChanged();
		
		child=row.findViewById(R.id.itemLoading);
		child.setVisibility(View.GONE);
		//child.clearAnimation();
	}
	
	protected boolean cacheInBackground() {
		if(action.contentEquals(FoodListActivity.SEARCH_FOODS)) {
			result = foodDao.search(searchParam,pageNum.toString());
		} else if(action.contentEquals(FoodListActivity.LIST_FAVORITE) && pageNum.get() == 1) {
			result = foodDao.getFavoriteFoods();
		}
		boolean shouldAppend = (result != null && result.size() > 0); 
		return(shouldAppend);
	}
	
	protected void appendCachedData() {
		FoodAdapter adapter = (FoodAdapter)getWrappedAdapter();
		if(result == null)
			return;
		
		for (Food food : result) {
			adapter.add(food);
		}
		result = null;
		pageNum.getAndIncrement();
	}
}