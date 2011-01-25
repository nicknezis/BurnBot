package com.nicknackhacks.dailyburn.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;

public class FoodLogEntryWrapper {
	private TextView name;
	private TextView size;
	private TextView nutrition1;
	private TextView nutrition2;
	private ImageView icon;
	private View row;

	public FoodLogEntryWrapper(View row) {
		this.row = row;
	}

	public void populateFrom(String s) {
		((TextView) row).setText(s);
	}
	
	public void populateFrom(FoodLogEntry f) {
		getName().setText(f.getFoodName());
		getSize().setText(String.valueOf(f.getServingsEaten()));
		String tmp = "Cal: " + f.getCaloriesEaten() + ", Fat: "
				+ f.getTotalFatEaten() + "g";
		getNutrition1().setText(tmp);
		tmp = "Carbs: " + f.getTotalCarbsEaten() + "g, Protein: "
				+ f.getProteinEaten() + "g";
		getNutrition2().setText(tmp);
		if (f.getFoodPictureUrl() != null) {
			getIcon().setImageResource(R.drawable.icon);
			getIcon().setTag(f.getFoodPictureUrl());
		}
	}

	public TextView getName() {
		if (name == null) {
			name = (TextView) row.findViewById(R.id.foodrow_Name);
		}
		return name;
	}

	public TextView getSize() {
		if (size == null) {
			size = (TextView) row.findViewById(R.id.foodrow_Size);
		}
		return size;
	}

	public TextView getNutrition1() {
		if (nutrition1 == null) {
			nutrition1 = (TextView) row
					.findViewById(R.id.foodrow_Nutrition1);
		}
		return nutrition1;
	}

	public TextView getNutrition2() {
		if (nutrition2 == null) {
			nutrition2 = (TextView) row
					.findViewById(R.id.foodrow_Nutrition2);
		}
		return nutrition2;
	}

	public ImageView getIcon() {
		if (icon == null) {
			icon = (ImageView) row.findViewById(R.id.foodrow_Icon);
		}
		return (icon);
	}
	
	public View getRow() {
		return row;
	}
}
