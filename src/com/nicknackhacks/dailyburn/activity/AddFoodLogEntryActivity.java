package com.nicknackhacks.dailyburn.activity;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.MealName;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.MealNameContract;

public class AddFoodLogEntryActivity extends Activity {

	public FoodDao foodDao;
	public int foodId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_foodlogentry);

		BurnBot app = (BurnBot) getApplication();
		foodDao = new FoodDao(app);
		
		foodId = getIntent().getIntExtra("foodId", 0);
		Spinner mealNames = (Spinner) findViewById(R.id.meals_spinner);
		
		Cursor mealNameCursor = managedQuery(MealNameContract.CONTENT_URI, null, null, null, null);
		SimpleCursorAdapter namesAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_spinner_dropdown_item,
				mealNameCursor,new String[] {MealNameContract.MEALNAME_NAME},new int[] {android.R.id.text1});

		mealNames.setAdapter(namesAdapter);
		
		DatePicker datePicker = (DatePicker) findViewById(R.id.DatePicker);
		Calendar cal = Calendar.getInstance();
    	int cYear = cal.get(Calendar.YEAR);
    	int cMonth = cal.get(Calendar.MONTH);
    	int cDay = cal.get(Calendar.DAY_OF_MONTH);
		datePicker.init(cYear,cMonth,cDay, null);
		//this.setCancelable(true);
		
		((Button)findViewById(R.id.dialog_ok)).setOnClickListener(okClickListener);
		((Button)findViewById(R.id.dialog_cancel)).setOnClickListener(cancelClickListener);
	}	

	private Button.OnClickListener okClickListener = new Button.OnClickListener() {

		public void onClick(View v) {
			ProgressDialog progressDialog = ProgressDialog.show(AddFoodLogEntryActivity.this, 
															"Food Entry", "Adding Food Entry");

			String servings_eaten = ((EditText) findViewById(R.id.servings_eaten)).getText().toString();
			DatePicker datePicker = (DatePicker) findViewById(R.id.DatePicker);
			Spinner mealNames = (Spinner) findViewById(R.id.meals_spinner);
			Cursor mealNameCursor = (Cursor) mealNames.getSelectedItem();
			int mealNameId = mealNameCursor.getInt(mealNameCursor.getColumnIndex(MealNameContract.MEALNAME_ID));
			try {
				foodDao.addFoodLogEntry(foodId, servings_eaten, 
										datePicker.getYear(), 
										datePicker.getMonth(), 
										datePicker.getDayOfMonth(),
										mealNameId);
			} catch (Exception e) {
				BurnBot.LogE(e.getMessage(), e);
			} finally {
				progressDialog.cancel();
				setResult(RESULT_OK);
				finish();
			}
		}
	};
	
	private Button.OnClickListener cancelClickListener = new Button.OnClickListener() {
		
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
	};
}