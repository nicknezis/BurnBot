package com.nicknackhacks.dailyburn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.nicknackhacks.dailyburn.model.FoodLogEntry;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodLogContract;

public class FoodLogEntryCursorAdapter extends CursorAdapter {

	private int layout;
	
	public FoodLogEntryCursorAdapter(Context context, int layout, Cursor c) {
		super(context, c);
		this.layout = layout;
	}

	@Override
		public void bindView(View view, Context context, Cursor cursor) {
			FoodLogEntryWrapper wrapper = (FoodLogEntryWrapper) view.getTag();
			FoodLogEntry entry = getFoodLogEntryFromCursor(cursor);
			wrapper.populateFrom(entry);
		}
	
	@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			
			View row = inflater.inflate(layout, parent, false);
			FoodLogEntryWrapper wrapper = new FoodLogEntryWrapper(row);
			row.setTag(wrapper);
			
			FoodLogEntry entry = getFoodLogEntryFromCursor(cursor);
			wrapper.populateFrom(entry);
			
			return row;
		}
	
	private FoodLogEntry getFoodLogEntryFromCursor(Cursor c) {
		FoodLogEntry entry = new FoodLogEntry();
		entry.setCreatedAt(c.getString(c.getColumnIndex(FoodLogContract.FOODLOG_CREATED_AT)));
		entry.setFoodId(c.getInt(c.getColumnIndex(FoodLogContract.FOODLOG_FOOD_ID)));
		entry.setId(c.getInt(c.getColumnIndex(FoodLogContract.FOODLOG_ID)));
		entry.setMealId(c.getInt(c.getColumnIndex(FoodLogContract.FOODLOG_MEAL_ID)));
		entry.setLoggedOn(c.getString(c.getColumnIndex(FoodLogContract.FOODLOG_LOGGED_ON)));
		entry.setServingsEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_SERVINGS_EATEN)));
		entry.setUserId(c.getInt(c.getColumnIndex(FoodLogContract.FOODLOG_USER_ID)));
		entry.setCaloriesEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_CALORIES_EATEN)));
		entry.setTotalFatEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_FAT_EATEN)));
		entry.setTotalCarbsEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_CARBS_EATEN)));
		entry.setProteinEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_PROTEIN_EATEN)));
		entry.setFoodName(c.getString(c.getColumnIndex(FoodLogContract.FOODLOG_FOOD_NAME)));
		entry.setFoodPictureUrl(c.getString(c.getColumnIndex(FoodLogContract.FOODLOG_PIC_URL)));
		entry.setFiberEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_FIBER_EATEN)));
		entry.setSodiumEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_SODIUM_EATEN)));
		entry.setCholesterolEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_CHOLESTEROL_EATEN)));
		entry.setPotassiumEaten(c.getFloat(c.getColumnIndex(FoodLogContract.FOODLOG_POTASSIUM_EATEN)));
		return entry;
	}
}
