package com.nicknackhacks.dailyburn.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.model.Food;

public class FoodWrapper {
	TextView name;
	TextView size;
	TextView nutrition1;
	TextView nutrition2;
	ImageView icon;
	View row;

	public FoodWrapper(View row) {
		this.row = row;
	}

	public void populateFrom(Food f) {
		getName().setText(f.getName());
		getSize().setText(f.getServingSize());
		String tmp = "Cal: " + f.getCalories() + ", Fat: "
				+ f.getTotalFat() + "g";
		getNutrition1().setText(tmp);
		tmp = "Carbs: " + f.getTotalCarbs() + "g, Protein: "
				+ f.getProtein() + "g";
		getNutrition2().setText(tmp);
		if (f.getThumbUrl() != null) {
			getIcon().setImageResource(R.drawable.icon);
//			if(f.getThumbUrl().contentEquals("/images/default_food_thumb.gif")) {
//				getIcon().setTag("http://dailyburn.com" + f.getThumbUrl());
//			} else {
				getIcon().setTag(f.getThumbUrl());
//			}
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
}
