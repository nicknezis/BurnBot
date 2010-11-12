package com.nicknackhacks.dailyburn.adapters;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.activity.BodyEntryListActivity;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;

public class BodyEntryAdapter extends ArrayAdapter<BodyLogEntry> {

	private BodyEntryListActivity activity;

	public BodyEntryAdapter(BodyEntryListActivity activity, int textViewResourceId,
			List<BodyLogEntry> items) {
		super(activity, textViewResourceId, items);
		this.activity = activity;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		BodyEntryWrapper wrapper = null;

		if (row == null) {
			LayoutInflater inflater = activity.getLayoutInflater();

			row = inflater.inflate(R.layout.body_entry_row, null);
			wrapper = new BodyEntryWrapper(row);
			row.setTag(wrapper);
		} else {
			wrapper = (BodyEntryWrapper) row.getTag();
		}
		
		int count = activity.getListAdapter().getCount();
		if(position == (count - 1))
			wrapper.populateFrom(getItem(position));
		else
			wrapper.populateWithDiff(getItem(position), getItem(position+1));

		return (row);
	}
}
