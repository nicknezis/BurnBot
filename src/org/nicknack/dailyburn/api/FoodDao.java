package org.nicknack.dailyburn.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.nicknack.dailyburn.model.Food;
import org.nicknack.dailyburn.model.Foods;

import android.util.Log;

import com.thoughtworks.xstream.XStream;

public class FoodDao {

	private final static String TAG = "dailyburndroid";
	private CommonsHttpOAuthConsumer consumer;
	DefaultHttpClient client;
	private XStream xstream;

	public FoodDao(DefaultHttpClient client, CommonsHttpOAuthConsumer consumer) {

		HttpParams parameters = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory
				.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		ClientConnectionManager manager = new ThreadSafeClientConnManager(
				parameters, schemeRegistry);
		this.client = new DefaultHttpClient(manager, parameters);

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

	public List<Food> getFavoriteFoods() {
		Foods foods = null;
		try {
			HttpGet request = new HttpGet(
					"https://dailyburn.com/api/foods/favorites.xml");
			consumer.sign(request);
			HttpResponse response = client.execute(request);
			foods = (Foods) xstream.fromXML(response.getEntity().getContent());
		} catch (OAuthMessageSignerException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("dailyburndroid", e.getMessage());
			e.printStackTrace();
		}
		return foods.foods;
	}

	public List<Food> search(String param) {

		Foods foods = null;
		try {
			String encodedParam = URLEncoder.encode(param, "UTF-8");
			HttpGet request = new HttpGet(
					"https://dailyburn.com/api/foods/search.xml?input="
							+ encodedParam);
			consumer.sign(request);
			HttpResponse response = client.execute(request);

			// //USE TO PRINT TO LogCat (Make a filter on dailyburndroid tag)
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(connection.getInputStream()));
			// String line = null;
			// while((line = in.readLine()) != null) {
			// Log.d("dailyburndroid",line);
			// }
			foods = (Foods) xstream.fromXML(response.getEntity().getContent());

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
			String encodedParam = URLEncoder.encode((new Integer(foodId))
					.toString(), "UTF-8");
			HttpGet request = new HttpGet("https://dailyburn.com/api/foods/"
					+ encodedParam + "/nutrition_label");
			consumer.sign(request);
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

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

	public void addFavoriteFood(int id) throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthNotAuthorizedException,
			ClientProtocolException, IOException {
		// create a request that requires authentication
		HttpPost post = new HttpPost(
				"https://dailyburn.com/api/foods/add_favorite");
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		// 'status' here is the update value you collect from UI
		nvps.add(new BasicNameValuePair("id", String.valueOf(id)));
		post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		// set this to avoid 417 error (Expectation Failed)
		post.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		// sign the request
		consumer.sign(post);
		// send the request
		final HttpResponse response = client.execute(post);
		// response status should be 200 OK
		int statusCode = response.getStatusLine().getStatusCode();
		final String reason = response.getStatusLine().getReasonPhrase();
		// release connection
		response.getEntity().consumeContent();
		if (statusCode != 200) {
			Log.e("dailyburndroid", reason);
			throw new OAuthNotAuthorizedException();
		}
	}

	public void deleteFavoriteFood(int id) throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthNotAuthorizedException,
			ClientProtocolException, IOException {
		// create a request that requires authentication
		HttpPost post = new HttpPost(
				"https://dailyburn.com/api/foods/delete_favorite");
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		// 'status' here is the update value you collect from UI
		nvps.add(new BasicNameValuePair("id", String.valueOf(id)));
		post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		// set this to avoid 417 error (Expectation Failed)
		post.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		// sign the request
		consumer.sign(post);
		// send the request
		final HttpResponse response = client.execute(post);
		// response status should be 200 OK
		int statusCode = response.getStatusLine().getStatusCode();
		final String reason = response.getStatusLine().getReasonPhrase();
		// release connection
		response.getEntity().consumeContent();
		if (statusCode != 200) {
			Log.e("dailyburndroid", reason);
			throw new OAuthNotAuthorizedException();
		}
	}

}
