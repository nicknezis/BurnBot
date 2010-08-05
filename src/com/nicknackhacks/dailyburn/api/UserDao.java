package com.nicknackhacks.dailyburn.api;

import java.net.URI;
import java.util.ArrayList;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.BasicResponseHandler;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.model.User;
import com.nicknackhacks.dailyburn.provider.BurnBotContract;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserContract;
import com.thoughtworks.xstream.XStream;

public class UserDao {

	CommonsHttpOAuthConsumer consumer;
	HttpClient client;
	XStream xstream;

	public UserDao(BurnBot app) {
		this.consumer = app.getOAuthConsumer();
		this.client = app.getHttpClient();
		configureXStream();
	}

	private void configureXStream() {
		xstream = new XStream();
		xstream.alias("user", User.class);
		xstream.aliasField("time-zone", User.class, "timeZone");
		xstream.aliasField("uses-metric-weights", User.class,
				"usesMetricWeights");
		xstream.aliasField("uses-metric-distances", User.class,
				"usesMetricDistances");
		xstream.aliasField("cal-goals-met-in-past-week", User.class,
				"calGoalsMetInPastWeek");
		xstream.aliasField("days-exercised-in-past-week", User.class,
				"daysExercisedInPastWeek");
		xstream.aliasField("picture-url", User.class, "pictureUrl");
		xstream.aliasField("calories-burned", User.class, "caloriesBurned");
		xstream.aliasField("calories-consumed", User.class, "caloriesConsumed");
		xstream.aliasField("body-weight", User.class, "bodyWeight");
		xstream.aliasField("body-weight-goal", User.class, "bodyWeightGoal");
		xstream.aliasField("created-at", User.class, "createdAt");
		xstream
				.aliasField("dynamic-diet-goals", User.class,
						"dynamicDietGoals");

	}

	public User getUserInfo() {
		User user = null;
		try {
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/users/current.xml", null, null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			if (response != null) {
				BurnBot.LogD(response);
				user = (User) xstream.fromXML(response);
			}
		} catch (Exception e) {
			BurnBot.LogE(e.getMessage(), e);
		}
		return user;
	}

	public void getUserAndApply(ContentResolver resolver) {
		ArrayList<ContentProviderOperation> ops = getUserOps();
		try {
			resolver.applyBatch(BurnBotContract.CONTENT_AUTHORITY, ops);
		} catch (RemoteException e) {
			throw new RuntimeException("Problem applying batch operation", e);
		} catch (OperationApplicationException e) {
			throw new RuntimeException("Problem applying batch operation", e);
		}
	}
	
	public ArrayList<ContentProviderOperation> getUserOps() {
		final ArrayList<ContentProviderOperation> batch = 
			new ArrayList<ContentProviderOperation>(1);
		User user = getUserInfo();
		final ContentProviderOperation.Builder builder = 
			ContentProviderOperation.newInsert(UserContract.CONTENT_URI);
		builder.withValue(UserContract.USER_ID, user.getId());
		builder.withValue(UserContract.USER_TIMEZONE, user.getTimeZone());
		builder.withValue(UserContract.USER_NAME, user.getUsername());
		builder.withValue(UserContract.USER_METRIC_WEIGHTS, user.isUsesMetricWeights());
		builder.withValue(UserContract.USER_METRIC_DISTANCE, user.isUsesMetricDistances());
		builder.withValue(UserContract.USER_CAL_GOALS_MET, user.getCalGoalsMetInPastWeek());
		builder.withValue(UserContract.USER_DAYS_EXERCISED, user.getDaysExercisedInPastWeek());
		builder.withValue(UserContract.USER_PICTURE_URL, user.getPictureUrl());
		builder.withValue(UserContract.USER_URL, user.getUrl());
		builder.withValue(UserContract.USER_CAL_BURNED, user.getCaloriesBurned());
		builder.withValue(UserContract.USER_CAL_CONSUMED, user.getCaloriesConsumed());
		builder.withValue(UserContract.USER_BODY_WEIGHT, user.getBodyWeight());
		builder.withValue(UserContract.USER_BODY_WEIGHT_GOAL, user.getBodyWeightGoal());
		builder.withValue(UserContract.USER_PRO, user.isPro());
		builder.withValue(UserContract.USER_CREATED_AT, user.getCreatedAt());
		builder.withValue(UserContract.USER_DYN_DIET_GOALS, user.isDynamicDietGoals());

		batch.add(builder.build());
		return batch;
	}
	
	public void setConsumer(CommonsHttpOAuthConsumer consumer) {
		this.consumer = consumer;
	}
}
