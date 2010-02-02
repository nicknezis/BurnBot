package org.nicknack.dailyburn.activity;

import org.nicknack.dailyburn.R;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        	IntentIntegrator.initiateScan(this);
        	return true;
        }
		return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    	if(result.getContents() == null)
    		return;
    	String contents = result.getContents();
    	String formatName = result.getFormatName();
    	//ProgressDialog.show(this, "Info", "Contents: " + contents + ", Format: " + formatName);
		Intent intent = new Intent("com.nicknack.dailyburn.SEARCH_FOOD");
		intent.putExtra("query", contents);
		startActivity(intent);

    	//ProgressDialog.show(this, "Info", "Contents: " + contents + ", Format: " + formatName);
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
