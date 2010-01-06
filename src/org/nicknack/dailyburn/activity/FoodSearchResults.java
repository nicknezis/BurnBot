package org.nicknack.dailyburn.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;

public class FoodSearchResults extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		String param = getIntent().getStringExtra("query");
		Log.d("dailyburndroid","query from resume: "+param);
	}
}
