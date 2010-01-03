package org.nicknack.dailyburn.activity;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.FoodDao;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FoodSearch extends Activity {

	private SharedPreferences pref;
	private boolean isAuthenticated;
	private FoodDao foodDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.food_search);
		pref = this.getSharedPreferences("dbdroid", 0);
    	isAuthenticated = pref.getBoolean("isAuthed", false);
    	String token = pref.getString("token", null);
    	String secret = pref.getString("secret", null);
    	DefaultOAuthConsumer consumer = new DefaultOAuthConsumer( 
    			//"1YHdpiXLKmueriS5v7oS2w", "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k", 
    			getString(R.string.consumer_key),getString(R.string.consumer_secret),
    			SignatureMethod.HMAC_SHA1);
    	consumer.setTokenWithSecret(token, secret);
    	foodDao = new FoodDao(new DefaultHttpClient(), consumer);
    	
    	final Button button = (Button) findViewById(R.id.food_search_button);
        button.setOnClickListener(this.buttonListener);
	}
	

	private View.OnClickListener buttonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.food_search_button:
				TextView txt = (TextView)findViewById(R.id.food_search);
				String param = txt.getText().toString();
				foodDao.search(param);
				return;
			}
			
		}
	};

}
