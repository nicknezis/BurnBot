package org.nicknack.dailyburn.activity;

import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.FoodDao;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FoodSearch extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.food_search);
    	
    	final Button button = (Button) findViewById(R.id.food_search_button);
        button.setOnClickListener(this.buttonListener);
	}
	

	private View.OnClickListener buttonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.food_search_button:
				TextView txt = (TextView)findViewById(R.id.food_search);
				String param = txt.getText().toString();
				Intent intent = new Intent("com.nicknack.dailyburn.SEARCH_FOOD");
				intent.putExtra("query", param);
				startActivity(intent);
				return;
			}			
		}
	};
}
