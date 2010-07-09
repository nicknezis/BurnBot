package com.nicknackhacks.dailyburn.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.nicknackhacks.dailyburn.R;

public class EditPreferences extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}