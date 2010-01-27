package org.nicknack.dailyburn.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.DrawableManager;
import org.nicknack.dailyburn.model.Food;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodDetail extends Activity {
	private DailyBurnDroid app;
	private boolean hasNutritionHtml;
	private SharedPreferences pref;
	private DefaultOAuthConsumer consumer;
	private DrawableManager dManager = new DrawableManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);
		pref = this.getSharedPreferences("dbdroid", 0);
		// boolean isAuthenticated = pref.getBoolean("isAuthed", false);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		consumer = new DefaultOAuthConsumer(
				// "1YHdpiXLKmueriS5v7oS2w",
				// "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k",
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		consumer.setTokenWithSecret(token, secret);

	}

	@Override
	protected void onResume() {
		super.onResume();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Long selectedFoodKey = extras.getLong("selectedFood");
		app = (DailyBurnDroid) this.getApplication();
		Food detailFood = (Food) app.objects.get(selectedFoodKey).get();
		final TextView tv = (TextView) findViewById(R.id.food_name);
		tv.setText("Name: " + detailFood.getName());
		final ImageView icon = (ImageView) findViewById(R.id.food_icon);
		Drawable foodImage = dManager.fetchDrawable("http://dailyburn.com"
				+ detailFood.getNormalUrl());
		icon.setImageDrawable(foodImage);

		// Long foodIconKey = extras.getLong("selectedFoodImage");
		// final ImageView icon = (ImageView) findViewById(R.id.food_icon);
		// Drawable foodImage = (Drawable) app.objects.get(foodIconKey).get();
		// icon.setImageDrawable(foodImage);
		final WebView nutrition = (WebView) findViewById(R.id.nutrition);
		// nutrition.loadUrl("file:///android_asset/nut.html");
		BufferedReader in = null;
		try {
			if (hasNutritionHtml == false) {
				String encodedParam = URLEncoder.encode((new Integer(detailFood
						.getId())).toString(), "UTF-8");
				URL url = new URL("https://dailyburn.com/api/foods/"
						+ encodedParam + "/nutrition_label");
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				// connection.setFollowRedirects(true);
				if (connection instanceof HttpURLConnection) {
					((HttpsURLConnection) connection)
							.setHostnameVerifier(new AllowAllHostnameVerifier());
				}
				// sign the request (consumer is a Signpost
				// DefaultOAuthConsumer)
				consumer.sign(connection);

				// send the request
				connection.connect();
				in = new BufferedReader(new InputStreamReader(connection
						.getInputStream()));
				StringBuilder sb = new StringBuilder();

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine).append('\n');
					Log.d("dailyburndroid", inputLine);
				}

				String html = sb.toString();
				int len = html.length();
				// The following snippet is needed to make the html safe
				// for the data:// uri which is passed to WebView
				StringBuilder buf = new StringBuilder(len + 100);
				for (int i = 0; i < len; i++) {
					char chr = html.charAt(i);
					switch (chr) {
					case '%':
						buf.append("%25");
						break;
					case '\'':
						buf.append("%27");
						break;
					case '#':
						buf.append("%23");
						break;
					default:
						buf.append(chr);
					}
				}
				String fixedHtml = buf.toString();
				nutrition.loadData(fixedHtml, "text/html", "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (OAuthMessageSignerException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
