package com.nicknackhacks.dailyburn.api;

import java.net.URI;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.BasicResponseHandler;

import android.util.Log;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.model.User;
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
				if(Log.isLoggable(BurnBot.TAG, Log.DEBUG)) {
					Log.d(BurnBot.TAG, response);
				}
				user = (User) xstream.fromXML(response);
			}
		} catch (Exception e) {
			if(Log.isLoggable(BurnBot.TAG, Log.ERROR)) {
				Log.e(BurnBot.TAG, e.getMessage());
			}
			e.printStackTrace();
		}
		return user;
	}

	public void setConsumer(CommonsHttpOAuthConsumer consumer) {
		this.consumer = consumer;
	}
}
