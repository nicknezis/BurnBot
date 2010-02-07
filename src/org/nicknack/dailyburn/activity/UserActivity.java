package org.nicknack.dailyburn.activity;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.UserDao;
import org.nicknack.dailyburn.model.User;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class UserActivity extends Activity {

	private UserDao userDao;
	private SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.userdetail);
		pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);
		userDao = new UserDao(new DefaultHttpClient(), consumer);
	}

	@Override
	protected void onResume() {
		super.onResume();
		User user = userDao.getUserInfo();
		((TextView)findViewById(R.id.user_name)).setText("Username: " + user.getUsername());
		((TextView)findViewById(R.id.user_weight)).setText("Body Weight: " + user.getBodyWeight());
	}
}
