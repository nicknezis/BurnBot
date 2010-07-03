package com.nicknackhacks.dailyburn.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;

public class FoodSearchActivity extends Activity {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.food_search);    	
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
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
    
    public void onClickVoiceSearch(View v) {
    	startVoiceRecognitionActivity();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
    		// Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);    	
            BurnBot.LogD( "Matches: " + matches);
            if(matches.size() > 0) {
            	EditText textField = (EditText) findViewById(R.id.food_search);
            	textField.setText(matches.get(0));
            }
    	} else if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
        	IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        	if(result.getContents() == null)
        		return;
        	String contents = result.getContents();
        	String formatName = result.getFormatName();
    		Intent intent = new Intent("com.nicknackhacks.dailyburn.SEARCH_FOOD");
    		intent.putExtra("query", contents);
    		startActivity(intent);
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    }
    
	public void onSearchFoods(View v) {
		TextView txt = (TextView)findViewById(R.id.food_search);
		String param = txt.getText().toString();
		Intent intent = new Intent("com.nicknackhacks.dailyburn.SEARCH_FOOD");
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
	
	/**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the food name.");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

}
