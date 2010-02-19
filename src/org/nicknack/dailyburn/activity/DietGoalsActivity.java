package org.nicknack.dailyburn.activity;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.DietDao;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class DietGoalsActivity extends Activity {

	private SharedPreferences pref;
	private DietDao dietDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dietgoals);
		pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret));
		consumer.setTokenWithSecret(token, secret);
		dietDao = new DietDao(new DefaultHttpClient(), consumer);
	}
}
