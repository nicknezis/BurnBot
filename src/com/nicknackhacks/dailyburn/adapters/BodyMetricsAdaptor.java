package com.nicknackhacks.dailyburn.adapters;

import java.util.List;

import com.nicknackhacks.dailyburn.model.BodyMetric;

import android.content.Context;
import android.widget.ArrayAdapter;

public class BodyMetricsAdaptor extends ArrayAdapter<BodyMetric> {

	public BodyMetricsAdaptor(Context context, int textViewResourceId, List<BodyMetric> items) {
		super(context, textViewResourceId, items);
	}

}
