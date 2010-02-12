package org.nicknack.dailyburn.activity;

import java.util.List;

import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.model.Food;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class FoodAdapter extends ArrayAdapter<Food> {

	private FoodSearchResults activity;

	private class FoodWrapper {
		TextView name;
		TextView size;
		TextView nutrition1;
		TextView nutrition2;
		ImageView icon;
		View row;

		FoodWrapper(View row) {
			this.row = row;
		}

		void populateFrom(Food f) {
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
				getIcon().setTag("http://dailyburn.com" + f.getThumbUrl());
			}
		}

		TextView getName() {
			if (name == null) {
				name = (TextView) row.findViewById(R.id.foodrow_Name);
			}
			return name;
		}

		TextView getSize() {
			if (size == null) {
				size = (TextView) row.findViewById(R.id.foodrow_Size);
			}
			return size;
		}

		TextView getNutrition1() {
			if (nutrition1 == null) {
				nutrition1 = (TextView) row
						.findViewById(R.id.foodrow_Nutrition1);
			}
			return nutrition1;
		}

		TextView getNutrition2() {
			if (nutrition2 == null) {
				nutrition2 = (TextView) row
						.findViewById(R.id.foodrow_Nutrition2);
			}
			return nutrition2;
		}

		ImageView getIcon() {
			if (icon == null) {
				icon = (ImageView) row.findViewById(R.id.foodrow_Icon);
			}
			return (icon);
		}
	}

	// private List<Food> items;

	public FoodAdapter(FoodSearchResults activity, int textViewResourceId,
			List<Food> items) {
		super(activity, textViewResourceId, items);
		this.activity = activity;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		FoodWrapper wrapper = null;

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			row = inflater.inflate(R.layout.foodrow, null);
			wrapper = new FoodWrapper(row);
			row.setTag(wrapper);
		} else {
			wrapper = (FoodWrapper) row.getTag();
		}

		wrapper.populateFrom(getItem(position));

		return (row);
	}
}
