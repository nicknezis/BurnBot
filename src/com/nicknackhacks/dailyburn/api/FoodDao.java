package com.nicknackhacks.dailyburn.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
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

import android.util.Log;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.model.FoodLogEntries;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;
import com.nicknackhacks.dailyburn.model.Foods;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.thoughtworks.xstream.XStream;

public class FoodDao {

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

	public void shutdown() {
		client.getConnectionManager().shutdown();
	}

	private void configureXStream() {
		xstream = new XStream();
		xstream.alias("foods", Foods.class);
		xstream.addImplicitCollection(Foods.class, "foods");
		xstream.alias("food", Food.class);
		xstream.registerConverter(new FoodConverter());

		xstream.alias("food-log-entries", FoodLogEntries.class);
		xstream.addImplicitCollection(FoodLogEntries.class, "entries");
		xstream.alias("food-log-entry", FoodLogEntry.class);
		xstream.registerConverter(new FoodLogEntryConverter());

		xstream.alias("nil-classes", NilClasses.class);

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
		return search(param, String.valueOf(1));
	}

	public List<Food> search(String param, String pageNum) {

		Foods foods = null;
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("input", param));
			qparams.add(new BasicNameValuePair("per_page", String.valueOf(10)));
			qparams.add(new BasicNameValuePair("page", pageNum));
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/foods/search.xml", URLEncodedUtils.format(qparams,
							"UTF-8"), null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);
			HttpResponse response = client.execute(request);

			if(response.getEntity() != null) {
				//foods = (Foods) xstream.fromXML(response.getEntity().getContent());
				Object result = xstream.fromXML(response.getEntity().getContent());
				if(result instanceof NilClasses) {
					return new ArrayList<Food>();
				} else {
					foods = (Foods) result; 
				}
			}
			
			Log.d(BurnBot.TAG, foods.foods.get(0).getName() + " "
					+ foods.foods.get(0).getBrand());
			Log.d(BurnBot.TAG, "T_Url: "
					+ foods.foods.get(0).getThumbUrl());
			Log.d(BurnBot.TAG, "N_Url: "
					+ foods.foods.get(0).getNormalUrl());
		} catch (Exception e) {
			Log.e(BurnBot.TAG, e.getMessage());
		}
		return foods.foods;
	}

	public String getNutritionLabel(int foodId) {
		BufferedReader in = null;
		String fixedHtml = null;
		try {
			String id = String.valueOf(foodId);
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/foods/" + id + "/nutrition_label", null, null);
			HttpGet request = new HttpGet(uri);
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
			for (char c : html.toCharArray()) {
				switch (c) {
				case '#':
					buf.append("%23");
					break;
				case '%':
					buf.append("%25");
					break;
				case '\'':
					buf.append("%27");
					break;
				case '?':
					buf.append("%3f");
					break;
				default:
					buf.append(c);
					break;
				}
			}
			fixedHtml = buf.toString();
		} catch (UnsupportedEncodingException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (OAuthMessageSignerException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (URISyntaxException e) {
			Log.e("dailyburndroid", e.getMessage());
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

	public void deleteFoodLogEntry(int entryId) throws ClientProtocolException,
			IOException, OAuthNotAuthorizedException, URISyntaxException,
			OAuthMessageSignerException, OAuthExpectationFailedException {
		URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
				"/api/food_log_entries/" + String.valueOf(entryId), null, null);
		HttpDelete delete = new HttpDelete(uri);
		consumer.sign(delete);
		HttpResponse response = client.execute(delete);
		int statusCode = response.getStatusLine().getStatusCode();
		final String reason = response.getStatusLine().getReasonPhrase();
		response.getEntity().consumeContent();
		if (statusCode != 200) {
			Log.e(BurnBot.TAG, reason);
			throw new OAuthNotAuthorizedException();
		}
	}

	public void addFoodLogEntry(int foodId, String servings_eaten, int year,
			int monthOfYear, int dayOfMonth)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, ClientProtocolException,
			IOException, OAuthNotAuthorizedException {
		// create a request that requires authentication
		HttpPost post = new HttpPost(
				"https://dailyburn.com/api/food_log_entries");
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		// 'status' here is the update value you collect from UI
		nvps.add(new BasicNameValuePair("food_log_entry[food_id]", String
				.valueOf(foodId)));
		nvps.add(new BasicNameValuePair("food_log_entry[servings_eaten]",
				servings_eaten));
		GregorianCalendar cal = new GregorianCalendar(year, monthOfYear,
				dayOfMonth);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = format.format(cal.getTime());
		nvps.add(new BasicNameValuePair("food_log_entry[logged_on]",
				formattedDate));
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
			Log.e(BurnBot.TAG, reason);
			throw new OAuthNotAuthorizedException();
		}
	}

	public List<FoodLogEntry> getFoodLogEntries() {
		return getFoodLogEntries(0, 0, 0);
	}

	public List<FoodLogEntry> getFoodLogEntries(int year, int monthOfYear,
			int dayOfMonth) {
		FoodLogEntries entries = null;
		try {
			HttpGet request = null;
			URI uri = null;
			if (year != 0 && monthOfYear != 0 && dayOfMonth != 0) {
				GregorianCalendar cal = new GregorianCalendar(year,
						monthOfYear, dayOfMonth);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String formattedDate = format.format(cal.getTime());
				List<NameValuePair> qparams = new ArrayList<NameValuePair>();
				qparams.add(new BasicNameValuePair("date", formattedDate));
				uri = URIUtils.createURI("https", "dailyburn.com", -1,
						"/api/food_log_entries.xml", URLEncodedUtils.format(
								qparams, "UTF-8"), null);
				// String dateParam = "?date=" + formattedDate;
				request = new HttpGet(uri);
			} else {
				uri = URIUtils.createURI("https", "dailyburn.com", -1,
						"/api/food_log_entries.xml", null, null);
			}
			request = new HttpGet(uri);

			consumer.sign(request);
			HttpResponse response = client.execute(request);
			// //USE TO PRINT TO LogCat (Make a filter on dailyburndroid tag)
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(response.getEntity().getContent()));
			// String line = null;
			// while((line = in.readLine()) != null) {
			// Log.d("dailyburndroid",line);
			// }
			Object result = xstream.fromXML(response.getEntity().getContent());
			if (result instanceof NilClasses) {
				return new ArrayList<FoodLogEntry>();
			} else {
				entries = (FoodLogEntries) result;
				// entries = (List<FoodLogEntry>)
				// xstream.fromXML(response.getEntity().getContent());
			}
		} catch (OAuthMessageSignerException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (URISyntaxException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		}
		return entries.entries;
	}

}
