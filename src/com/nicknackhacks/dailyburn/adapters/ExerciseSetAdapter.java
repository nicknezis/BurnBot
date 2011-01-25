package com.nicknackhacks.dailyburn.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.model.ExerciseSet;

public class ExerciseSetAdapter extends ArrayAdapter<ExerciseSet> {

	private Activity activity;

	public ExerciseSetAdapter(Activity activity, int textViewResourceId,
			List<ExerciseSet> items) {
		super(activity, textViewResourceId, items);
		this.activity = activity;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ExerciseSetWrapper wrapper = null;

		if (row == null) {
			LayoutInflater inflater = activity.getLayoutInflater();

			row = inflater.inflate(R.layout.exercise_row, null);
			wrapper = new ExerciseSetWrapper(row);
			row.setTag(wrapper);
		} else {
			wrapper = (ExerciseSetWrapper) row.getTag();
		}

		wrapper.populateFrom(getItem(position));

		return (row);
	}
}
