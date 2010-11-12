package com.nicknackhacks.dailyburn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodContract;

public class FoodCursorAdapter extends CursorAdapter {
	
//	private MyDataSetObserver observer;
	private int layout;
	
	public FoodCursorAdapter(Context context, int layout, Cursor c) {
		super(context, c);
		this.layout = layout;
//		observer = new MyDataSetObserver();
//		c.registerDataSetObserver(observer);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		FoodWrapper wrapper = (FoodWrapper) view.getTag();
		Food food = FoodContract.getFoodFromCursor(cursor);
		wrapper.populateFrom(food);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		final LayoutInflater inflater = LayoutInflater.from(context);

		View row = inflater.inflate(layout, parent, false);
		FoodWrapper wrapper = new FoodWrapper(row);
		row.setTag(wrapper);

		Food food = FoodContract.getFoodFromCursor(cursor);
		wrapper.populateFrom(food);
		
		return row;
	}
	
	@Override
		public Object getItem(int position) {
			Food food = FoodContract.getFoodFromCursor((Cursor) super.getItem(position));
			return food;
		}
	
//	private Food getFoodFromCursor(Cursor c) {
//		Food food = new Food();
//		food.setId(c.getInt(c.getColumnIndex(FoodContract.FOOD_ID)));
//		food.setName(c.getString(c.getColumnIndex(FoodContract.FOOD_NAME)));
//		food.setBrand(c.getString(c.getColumnIndex(FoodContract.FOOD_BRAND)));
//		food.setCalories(c.getInt(c.getColumnIndex(FoodContract.FOOD_CALORIES)));
//		food.setProtein(c.getFloat(c.getColumnIndex(FoodContract.FOOD_PROTEIN)));
//		food.setServingSize(c.getString(c.getColumnIndex(FoodContract.FOOD_SERVING_SIZE)));
//		food.setTotalCarbs(c.getFloat(c.getColumnIndex(FoodContract.FOOD_TOTAL_CARBS)));
//		food.setTotalFat(c.getFloat(c.getColumnIndex(FoodContract.FOOD_TOTAL_FAT)));
//		food.setUserId(c.getInt(c.getColumnIndex(FoodContract.FOOD_USER_ID)));
//		food.setThumbUrl(c.getString(c.getColumnIndex(FoodContract.FOOD_THUMB_URL)));
//		food.setUsda(c.getInt(c.getColumnIndex(FoodContract.FOOD_USDA))==1?true:false);
//		return food;
//	}
	
//	private class MyDataSetObserver extends DataSetObserver {
//	    public void onChanged(){
//	        LogHelper.LogD("CHANGED CURSOR!");
//	    }
//	    public void onInvalidated(){
//	    	getCursor().requery();
//	        LogHelper.LogD("INVALIDATED CURSOR!");
//	    }
//	}
}
