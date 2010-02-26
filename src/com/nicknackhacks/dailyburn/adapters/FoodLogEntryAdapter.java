package com.nicknackhacks.dailyburn.adapters;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.activity.FoodLogEntriesActivity;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;

public class FoodLogEntryAdapter extends ArrayAdapter<FoodLogEntry> {

	private FoodLogEntriesActivity activity;

	public FoodLogEntryAdapter(FoodLogEntriesActivity activity, int textViewResourceId,
			List<FoodLogEntry> items) {
		super(activity, textViewResourceId, items);
		this.activity = activity;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		FoodLogEntryWrapper wrapper = null;

		if (row == null) {
			LayoutInflater inflater = activity.getLayoutInflater();

			row = inflater.inflate(R.layout.foodrow, null);
			wrapper = new FoodLogEntryWrapper(row);
			row.setTag(wrapper);
		} else {
			wrapper = (FoodLogEntryWrapper) row.getTag();
		}

		wrapper.populateFrom(getItem(position));

		return (row);
	}
}
