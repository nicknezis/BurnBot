package com.nicknackhacks.dailyburn.api;

import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.model.MealName;

public class AddFoodLogEntryDialog extends Dialog {

	public FoodDao foodDao;
	public int foodId;
	
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
	
	public AddFoodLogEntryDialog(Context context, FoodDao foodDao) {
		super(context);
		
		this.foodDao = foodDao;
		
		setContentView(R.layout.add_foodlogentry);
		setTitle("I Ate This");

		Spinner mealNames = (Spinner) findViewById(R.id.meals_spinner);
		List<MealName> names = foodDao.getMealNames();
		ArrayAdapter<MealName> namesAdapter = new ArrayAdapter<MealName>(getContext(), 
						android.R.layout.simple_spinner_dropdown_item, names);
		mealNames.setAdapter(namesAdapter);
		
		DatePicker datePicker = (DatePicker) findViewById(R.id.DatePicker);
		Calendar cal = Calendar.getInstance();
    	int cYear = cal.get(Calendar.YEAR);
    	int cMonth = cal.get(Calendar.MONTH);
    	int cDay = cal.get(Calendar.DAY_OF_MONTH);
		datePicker.init(cYear,cMonth,cDay, null);
		this.setCancelable(true);
		
		((Button)findViewById(R.id.dialog_ok)).setOnClickListener(okClickListener);
		((Button)findViewById(R.id.dialog_cancel)).setOnClickListener(cancelClickListener);
	}

	private Button.OnClickListener okClickListener = new Button.OnClickListener() {

		public void onClick(View v) {
			int foodId = AddFoodLogEntryDialog.this.foodId;
			AddFoodLogEntryDialog.this.cancel();
			ProgressDialog progressDialog = ProgressDialog.show(AddFoodLogEntryDialog.this.getContext(), 
															"Food Entry", "Adding Food Entry");

			String servings_eaten = ((EditText) findViewById(R.id.servings_eaten)).getText().toString();
			DatePicker datePicker = (DatePicker) findViewById(R.id.DatePicker);
			Spinner mealNames = (Spinner) findViewById(R.id.meals_spinner);
			MealName mealName = (MealName) mealNames.getSelectedItem();
			try {
				foodDao.addFoodLogEntry(foodId, servings_eaten, 
										datePicker.getYear(), 
										datePicker.getMonth(), 
										datePicker.getDayOfMonth(),
										mealName.getId());
			} catch (Exception e) {
				BurnBot.LogE(e.getMessage(), e);
			} finally {
				progressDialog.cancel();
			}
		}
	};
	
	private Button.OnClickListener cancelClickListener = new Button.OnClickListener() {
		
		public void onClick(View v) {
			AddFoodLogEntryDialog.this.cancel();
		}
	};
}