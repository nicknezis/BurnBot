package com.nicknackhacks.dailyburn.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.api.FoodDao;
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
		
		Intent intent = getIntent();
		foodId = intent.getIntExtra("foodId", 0);
		Spinner mealNames = (Spinner) findViewById(R.id.meals_spinner);
		
		Cursor mealNameCursor = managedQuery(MealNameContract.CONTENT_URI, null, null, null, null);
		SimpleCursorAdapter namesAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_spinner_dropdown_item,
				mealNameCursor,new String[] {MealNameContract.MEALNAME_NAME},new int[] {android.R.id.text1});

		mealNames.setAdapter(namesAdapter);
		
		String foodName = intent.getStringExtra("foodName");
		TextView foodNameLabel = (TextView) findViewById(R.id.food_name_label);
		foodNameLabel.setText(foodNameLabel.getText() + " " + foodName); 
		
		String servingSize = intent.getStringExtra("servingSize");
		TextView servingsLabel = (TextView) findViewById(R.id.servings_eaten_label);
		servingsLabel.setText(servingsLabel.getText() + " " + servingSize);
		
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
			//Spinner servings = (Spinner) findViewById(R.id.servings_spinner);
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
				LogHelper.LogE(e.getMessage(), e);
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