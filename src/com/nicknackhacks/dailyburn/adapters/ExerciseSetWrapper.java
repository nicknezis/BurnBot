package com.nicknackhacks.dailyburn.adapters;

import android.view.View;
import android.widget.TextView;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.model.ExerciseSet;

public class ExerciseSetWrapper {
	private TextView cell00;	//(Row,Column)
	private TextView cell01;
	private TextView cell10;
	private TextView cell11;
	private TextView cell20;
	private TextView cell21;
	private View row;

	public ExerciseSetWrapper(View row) {
		this.row = row;
	}

//	public void populateFrom(String s) {
//		((TextView) row).setText(s);
//	}
	
	public void populateFrom(ExerciseSet set) {
		getCell00().setText(set.getExerciseName());
		getCell01().setText(Integer.toString(set.getSetOrder()));
		getCell10().setText(Integer.toString(set.getCaloriesBurned()));
		getCell11().setText(set.getLoggedOn());
		switch(set.getSetType()) {
		case WeightSet:
			getCell20().setText(Integer.toString(set.getReps()));
			getCell21().setText(Float.toString(set.getWeight()));
			break;
		case CardioSet:
			getCell20().setText(Float.toString(set.getDistance()));
			getCell21().setText(Float.toString(set.getTime()));
			break;
		}
	}

	public TextView getCell00() {
		if(cell00 == null) {
			cell00 = (TextView) row.findViewById(R.id.cell00);
		}
		return cell00;
	}

	public TextView getCell01() {
		if(cell01 == null) {
			cell01 = (TextView) row.findViewById(R.id.cell01);
		}
		return cell01;
	}

	public TextView getCell10() {
		if(cell10 == null) {
			cell10 = (TextView) row.findViewById(R.id.cell10);
		}
		return cell10;
	}

	public TextView getCell11() {
		if(cell11 == null) {
			cell11 = (TextView) row.findViewById(R.id.cell11);
		}
		return cell11;
	}

	public TextView getCell20() {
		if(cell20 == null) {
			cell20 = (TextView) row.findViewById(R.id.cell20);
		}
		return cell20;
	}

	public TextView getCell21() {
		if(cell21 == null) {
			cell21 = (TextView) row.findViewById(R.id.cell21);
		}
		return cell21;
	}

	public View getRow() {
		return row;
	}

}
