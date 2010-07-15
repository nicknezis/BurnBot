package com.nicknackhacks.dailyburn;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.nicknackhacks.dailyburn.activity.FoodSearchActivity;

public class ActionBarHandler implements OnClickListener{
	private Activity activity;
	public static final String BARCODE = "barcode";
	
	public ActionBarHandler(Activity a){
		activity = a;
	}
	
	public void onClick(View abAction) {
		Intent i = new Intent(activity, FoodSearchActivity.class);
		if (abAction.getId() == R.id.ab_barcode){
			i.putExtra(BARCODE, true);
    		activity.startActivity(i);
    	}else if (abAction.getId() == R.id.ab_search){
    		activity.startActivity(i);
    	}
	}

}