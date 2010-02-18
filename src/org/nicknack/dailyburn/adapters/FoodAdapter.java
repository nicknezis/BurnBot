package org.nicknack.dailyburn.adapters;

import java.util.List;

import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.activity.FoodListActivity;
import org.nicknack.dailyburn.model.Food;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
