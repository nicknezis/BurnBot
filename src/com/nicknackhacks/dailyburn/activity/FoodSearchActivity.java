package com.nicknackhacks.dailyburn.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nicknackhacks.dailyburn.R;

public class FoodSearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.food_search);    	
	}
	
	/* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.food_search_menu, menu);
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_barcode:
        	initiateBarcodeScan();
        	return true;
        }
		return false;
    }

    public void initiateBarcodeScan() {
    	IntentIntegrator.initiateScan(this);
    }
    
    public void onClickBarcodeScan(View v) {
    	initiateBarcodeScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    	if(result.getContents() == null)
    		return;
    	String contents = result.getContents();
    	String formatName = result.getFormatName();
		Intent intent = new Intent("com.nicknackhacks.dailyburn.SEARCH_FOOD");
		intent.putExtra("query", contents);
		startActivity(intent);
    }
    
	public void onSearchFoods(View v) {
		TextView txt = (TextView)findViewById(R.id.food_search);
		String param = txt.getText().toString();
		Intent intent = new Intent("com.nicknackhacks.dailyburn.SEARCH_FOODS");
		intent.putExtra("query", param);
		startActivity(intent);
		return;
	}
	
	public void onListFavoriteFoods(View v) {
		Intent intent = new Intent("com.nicknackhacks.dailyburn.LIST_FAVORITE_FOODS");
		startActivity(intent);
		return;
	}
	
	public void onViewFoodLogs(View v) {
		Intent intent = new Intent("com.nicknackhacks.dailyburn.LIST_FOOD_LOGS");
		startActivity(intent);
		return;
	}
}
