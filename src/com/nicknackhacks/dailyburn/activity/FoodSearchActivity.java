package com.nicknackhacks.dailyburn.activity;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nicknackhacks.dailyburn.ActionBarHandler;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.DietDao;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.DietGoal;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;

public class FoodSearchActivity extends Activity {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private FoodDao foodDao;
	private DietDao dietDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.food_search);    	
        
		if (getIntent().hasExtra(ActionBarHandler.BARCODE))
			initiateBarcodeScan();
		
		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
		dietDao = new DietDao(app);
		
		List<DietGoal> goals = dietDao.getDietGoals();
		List<FoodLogEntry> foodEntries = foodDao.getFoodLogEntries();
		HashMap<String, DietGoal> goalMap = new HashMap<String, DietGoal>();
		for (DietGoal d : goals) {
			goalMap.put(d.getGoalType(), d);
		}
		DietGoal calorieGoal = goalMap.get("CalorieDietGoal");
		DietGoal fatGoal = goalMap.get("TotalFatDietGoal");
		DietGoal carbGoal = goalMap.get("CarbDietGoal");
		DietGoal proteinGoal = goalMap.get("ProteinDietGoal");
		
		float curCalories = 0;
		float curFat = 0;
		float curCarb = 0;
		float curProtein = 0;
		for (FoodLogEntry entry : foodEntries) {
			curCalories += entry.getCaloriesEaten();
			curFat += entry.getTotalFatEaten();
			curCarb += entry.getTotalCarbsEaten();
			curProtein += entry.getProteinEaten();
		}
		
		String tmp = "Calories: " + String.valueOf(curCalories) 
		+ " of (" + calorieGoal.getLowerBound() 
		+ " - " + calorieGoal.getUpperBound() 
		+ ")";
		((TextView)findViewById(R.id.calorie_goal)).setText(tmp);
		LogHelper.LogD(tmp);
		tmp = "Carbs: " + String.valueOf(curCarb) 
		+ " of (" + carbGoal.getLowerBound() 
		+ " - " + carbGoal.getUpperBound() 
		+ ")";
		((TextView)findViewById(R.id.carb_goal)).setText(tmp);
		LogHelper.LogD(tmp);
		tmp = "Protein: " + String.valueOf(curProtein) 
		+ " of (" + proteinGoal.getLowerBound() 
		+ " - " + proteinGoal.getUpperBound() 
		+ ")";
		((TextView)findViewById(R.id.protein_goal)).setText(tmp);
		LogHelper.LogD(tmp);
		tmp = "Fat: " + String.valueOf(curFat) 
		+ " of (" + fatGoal.getLowerBound() 
		+ " - " + fatGoal.getUpperBound() 
		+ ")";
		((TextView)findViewById(R.id.fat_goal)).setText(tmp);
		LogHelper.LogD(tmp);
	}
		
	@Override
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onPageView();
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
        	FlurryAgent.onEvent("Click Barcode Options Item");
        	initiateBarcodeScan();
        	return true;
        }
		return false;
    }

    public void initiateBarcodeScan() {
    	IntentIntegrator.initiateScan(this);
    }
    
    public void onClickBarcodeScan(View v) {
    	FlurryAgent.onEvent("Click Barcode Button");
    	initiateBarcodeScan();
    }
    
    public void onClickVoiceSearch(View v) {
    	FlurryAgent.onEvent("Click Voice Search Button");
    	startVoiceRecognitionActivity();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
//    		// Fill the list view with the strings the recognizer thought it could have heard
//            ArrayList<String> matches = data.getStringArrayListExtra(
//                    RecognizerIntent.EXTRA_RESULTS);
//            LogHelper.LogD( "Matches: " + matches);
//            if(matches.size() > 0) {
//            	HashMap<String,String> params = new HashMap<String,String>();
//            	params.put("match", matches.get(0));
//                FlurryAgent.onEvent("Voice Result",params);
//            	EditText textField = (EditText) findViewById(R.id.food_search);
//            	textField.setText(matches.get(0));
//            } else {
//            	FlurryAgent.onEvent("No Voice Result");
//            }
//    	} else 
    		if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
        	IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        	if(result.getContents() == null)
        		return;
        	String contents = result.getContents();
        	String formatName = result.getFormatName();
    		Intent intent = new Intent("com.nicknackhacks.dailyburn.SEARCH_FOOD");
    		intent.putExtra("query", contents);
    		HashMap<String,String> params = new HashMap<String,String>();
    		params.put("Result Contents", contents);
    		params.put("Result FormatName", formatName);
    		FlurryAgent.onEvent("Barcode Result", params);
    		startActivity(intent);
    	}
    }
    
	public void onSearchFoods(View v) {
		onSearchRequested();
//		TextView txt = (TextView)findViewById(R.id.food_search);
//		String param = txt.getText().toString();
//		Intent intent = new Intent("com.nicknackhacks.dailyburn.SEARCH_FOOD");
//		intent.putExtra("query", param);
//		HashMap<String, String> fParams = new HashMap<String,String>();
//		fParams.put("query", param);
//		FlurryAgent.onEvent("Click Food Search Button", fParams);
//		startActivity(intent);
//		return;
	}
	
	public void onListFavoriteFoods(View v) {
		FlurryAgent.onEvent("Click Favorite Foods Button");
		Intent intent = new Intent("com.nicknackhacks.dailyburn.LIST_FAVORITE_FOODS");
		startActivity(intent);
		return;
	}
	
	public void onViewFoodLogs(View v) {
		FlurryAgent.onEvent("Click View Food Logs Button");
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
