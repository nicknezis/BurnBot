package org.nicknack.dailyburn.adapters;

import java.util.List;

import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.activity.FoodLogEntriesActivity;
import org.nicknack.dailyburn.model.FoodLogEntry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
