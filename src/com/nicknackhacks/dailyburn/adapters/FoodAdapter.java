package com.nicknackhacks.dailyburn.adapters;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.activity.FoodListActivity;
import com.nicknackhacks.dailyburn.model.Food;

public class FoodAdapter extends ArrayAdapter<Food> {

	private FoodListActivity activity;

	public FoodAdapter(FoodListActivity activity, int textViewResourceId,
			List<Food> items) {
		super(activity, textViewResourceId, items);
		this.activity = activity;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		FoodWrapper wrapper = null;

		if (row == null) {
			LayoutInflater inflater = activity.getLayoutInflater();

			row = inflater.inflate(R.layout.foodrow, null);
			wrapper = new FoodWrapper(row);
			row.setTag(wrapper);
		} else {
			wrapper = (FoodWrapper) row.getTag();
		}

		wrapper.populateFrom(getItem(position));

		return (row);
	}
}
