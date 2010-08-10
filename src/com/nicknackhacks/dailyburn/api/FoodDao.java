package com.nicknackhacks.dailyburn.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.model.Food;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;
import com.nicknackhacks.dailyburn.model.MealName;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.nicknackhacks.dailyburn.model.User;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserContract;
import com.thoughtworks.xstream.XStream;

public class FoodDao {

	private CommonsHttpOAuthConsumer consumer;
	private HttpClient client;
	private XStream xstream;
	private String perPage = "10";
	private String sortBy = "best_match";
	private String reverse = "false";

	public FoodDao(BurnBot app) {
		this.consumer = app.getOAuthConsumer();
		this.client = app.getHttpClient();
		configureXStream();
	}

	private void configureXStream() {
		xstream = new XStream();
		xstream.alias("foods", ArrayList.class);
		xstream.alias("food", Food.class);
		xstream.registerConverter(new FoodConverter());

		xstream.alias("food-log-entries", ArrayList.class);
		xstream.alias("food-log-entry", FoodLogEntry.class);
		xstream.registerConverter(new FoodLogEntryConverter());

		xstream.alias("meal-names", ArrayList.class);
		xstream.alias("meal-name", MealName.class);
		xstream.alias("nil-classes", NilClasses.class);

	}

	public List<MealName> getMealNames() {
		ArrayList<MealName> names = null;
		try {
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/foods/meal_names.xml", null, null);

			HttpGet request = new HttpGet(uri);
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			BurnBot.LogD( response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				names = new ArrayList<MealName>();
			} else {
				names = (ArrayList<MealName>) result;
			}
		} catch (OAuthMessageSignerException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IllegalStateException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (URISyntaxException e) {
			BurnBot.LogE(e.getMessage(), e);
		}
		return names;
	}

	public List<Food> getFavoriteFoods() {
		ArrayList<Food> foods = null;
		try {
			HttpGet request = new HttpGet(
					"https://dailyburn.com/api/foods/favorites.xml");
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			BurnBot.LogD( response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				foods = new ArrayList<Food>();
			} else {
				foods = (ArrayList<Food>) result;
			}
		} catch (OAuthMessageSignerException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IllegalStateException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogE(e.getMessage(), e);
		}
		return foods;
	}
	
	public ArrayList<ContentProviderOperation> getFavoriteFoodsOps(List<Food> foods) {
		final ArrayList<ContentProviderOperation> batch = 
			new ArrayList<ContentProviderOperation>();
		
		for(Food food : foods) {
			final ContentProviderOperation.Builder builder = 
				ContentProviderOperation.newInsert(FoodContract.buildFavoriteFoodUri());
			builder.withValue(FoodContract.FOOD_BRAND, food.getBrand());
			builder.withValue(FoodContract.FOOD_CALORIES, food.getCalories());
			builder.withValue(FoodContract.FOOD_ID, food.getId());
			builder.withValue(FoodContract.FOOD_NAME, food.getName());
			builder.withValue(FoodContract.FOOD_PROTEIN, food.getProtein());
			builder.withValue(FoodContract.FOOD_SERVING_SIZE, food.getServingSize());
			builder.withValue(FoodContract.FOOD_THUMB_URL, food.getThumbUrl());
			builder.withValue(FoodContract.FOOD_TOTAL_CARBS, food.getTotalCarbs());
			builder.withValue(FoodContract.FOOD_TOTAL_FAT, food.getTotalFat());
			builder.withValue(FoodContract.FOOD_USDA, food.isUsda());
			builder.withValue(FoodContract.FOOD_USER_ID, food.getUserId());
			
			batch.add(builder.build());
		}
		
		return batch;
	}
	
	public List<Food> search(String param, String pageNum) {

		ArrayList<Food> foods = null;
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("input", param));
			qparams.add(new BasicNameValuePair("per_page", perPage));
			qparams.add(new BasicNameValuePair("page", pageNum));
			if(sortBy != null && !sortBy.contains("best_match")) {
				qparams.add(new BasicNameValuePair("sort_by", sortBy));
				qparams.add(new BasicNameValuePair("reverse", reverse));
			}
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/foods/search.xml", URLEncodedUtils.format(qparams,
							"UTF-8"), null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			BurnBot.LogD(response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				foods = new ArrayList<Food>();
			} else {
				foods = (ArrayList<Food>) result;
			}
		} catch (Exception e) {
			BurnBot.LogE(e.getMessage(), e);
		}
		return foods;
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

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			BurnBot.LogD( response);

			String html = response;
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
			BurnBot.LogE(e.getMessage(), e);
		} catch (MalformedURLException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthMessageSignerException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (URISyntaxException e) {
			BurnBot.LogE(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					BurnBot.LogE(e.getMessage(), e);
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
			BurnBot.LogE(reason);
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
			if (Log.isLoggable(BurnBot.TAG, Log.ERROR))
				Log.e(BurnBot.TAG, reason);
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
			BurnBot.LogE(reason);
			throw new OAuthNotAuthorizedException();
		}
	}

	public void addFoodLogEntry(int foodId, String servings_eaten, int year,
			int monthOfYear, int dayOfMonth, int mealId)
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
		nvps.add(new BasicNameValuePair("food_log_entry[meal_name_id]", String
				.valueOf(mealId)));
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
			BurnBot.LogE(reason);
			throw new OAuthNotAuthorizedException();
		}
	}

	public List<FoodLogEntry> getFoodLogEntries() {
		return getFoodLogEntries(0, 0, 0);
	}

	public List<FoodLogEntry> getFoodLogEntries(int year, int monthOfYear,
			int dayOfMonth) {
		ArrayList<FoodLogEntry> entries = null;
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

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			BurnBot.LogD( response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				entries = new ArrayList<FoodLogEntry>();
			} else {
				entries = (ArrayList<FoodLogEntry>) result;
			}
		} catch (OAuthMessageSignerException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IllegalStateException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (URISyntaxException e) {
			BurnBot.LogE(e.getMessage(), e);
		}
		return entries;
	}

	public String getPerPage() {
		return perPage;
	}

	public void setPerPage(String perPage) {
		this.perPage = perPage;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getReverse() {
		return reverse;
	}

	public void setReverse(String reverse) {
		this.reverse = reverse;
	}

}
