package org.nicknack.dailyburn.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.model.Food;
import org.nicknack.dailyburn.model.Foods;

import android.util.Log;

import com.thoughtworks.xstream.XStream;

public class FoodDao {

	private final static String TAG = "dailyburndroid";
	private DefaultOAuthConsumer consumer;
	private XStream xstream;

	public FoodDao(DefaultHttpClient client, DefaultOAuthConsumer consumer) {
		this.consumer = consumer;
		configureXStream();
	}

	private void configureXStream() {
		xstream = new XStream();
		xstream.alias("foods", Foods.class);
		xstream.addImplicitCollection(Foods.class, "foods");
		xstream.alias("food", Food.class);
		xstream.registerConverter(new FoodConverter());
	}

	public List<Food> search(String param) {

		Foods foods = null;
		try {
			String encodedParam = URLEncoder.encode(param, "UTF-8");
			URL url = new URL(
					"https://dailyburn.com/api/foods/search.xml?input="
							+ encodedParam);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			if (connection instanceof HttpsURLConnection) {
				((HttpsURLConnection) connection)
						.setHostnameVerifier(new AllowAllHostnameVerifier());
			}

			// sign the request (consumer is a Signpost DefaultOAuthConsumer)
			consumer.sign(connection);

			// send the request
			connection.connect();

			// //USE TO PRINT TO LogCat (Make a filter on dailyburndroid tag)
//			 BufferedReader in = new BufferedReader(new
//			 InputStreamReader(connection.getInputStream()));
//			 String line = null;
//			 while((line = in.readLine()) != null) {
//			 Log.d("dailyburndroid",line);
//			 }
			foods = (Foods) xstream.fromXML(connection.getInputStream());
			Log.d(TAG, foods.foods.get(0).getName() + " "
					+ foods.foods.get(0).getBrand());
			Log.d(TAG, "T_Url: " + foods.foods.get(0).getThumbUrl());
			Log.d(TAG, "N_Url: " + foods.foods.get(0).getNormalUrl());
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
		return foods.foods;
	}

	public String getNutritionLabel(int foodId) {
		BufferedReader in = null;
		String fixedHtml = null;
		try {
			String encodedParam = URLEncoder.encode((new Integer(foodId)).toString(), "UTF-8");
			URL url = new URL("https://dailyburn.com/api/foods/" + encodedParam
					+ "/nutrition_label");
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
				fixedHtml = buf.toString();
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
		return fixedHtml;
	}
}
