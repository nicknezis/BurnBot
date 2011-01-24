package com.nicknackhacks.dailyburn.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.model.DietGoal;
import com.nicknackhacks.dailyburn.model.ExerciseSet;
import com.nicknackhacks.dailyburn.model.MealName;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.thoughtworks.xstream.XStream;

public class ExerciseDao {

	private CommonsHttpOAuthConsumer consumer;
	private HttpClient client;
	private XStream xstream;

	public ExerciseDao(BurnBot app) {
		this.consumer = app.getOAuthConsumer();
		this.client = app.getHttpClient();
		configureXStream();
	}

	private void configureXStream() {
		xstream = new XStream();

//		xstream.alias("diet-goals", ArrayList.class);
//		xstream.alias("diet-goal", DietGoal.class);
//		xstream.aliasField("created-on", DietGoal.class, "createdOn");
//		xstream.aliasField("lower-bound", DietGoal.class, "lowerBound");
//		xstream.aliasField("upper-bound", DietGoal.class, "upperBound");
//		xstream.aliasField("user-id", DietGoal.class, "userId");
//		xstream.aliasField("adjusted-lower-bound", DietGoal.class,
//				"adjustedLowerBound");
//		xstream.aliasField("adjusted-upper-bound", DietGoal.class,
//				"adjustedUpperBound");
//		xstream.aliasField("goal-type", DietGoal.class, "goalType");
//		xstream.aliasField("diet-plan-percent", DietGoal.class, "dietPlanPercent");
		// xstream.alias("goal-type", GoalType.class);
//		xstream.registerConverter(new EnumSingleValueConverter(GoalType.class));

		xstream.alias("nil-classes", NilClasses.class);
	}

	public String getExerciseSets() {
		List<ExerciseSet> set = null;
		String returnValue = null;
		try {
		URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
				"/api/exercise_sets.xml", null, null);

		HttpGet request = new HttpGet(uri);
		consumer.sign(request);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response = client.execute(request, responseHandler);
		returnValue = response;
		
		LogHelper.LogD( response);

//		Object result = xstream.fromXML(response);
//		if (result instanceof NilClasses) {
//			names = new ArrayList<MealName>();
//		} else {
//			names = (ArrayList<MealName>) result;
//		}
		} catch (Exception e) {
			LogHelper.LogD(e.getLocalizedMessage());
		}
		return returnValue;
	}
	
//	public List<DietGoal> getDietGoals() {
//		ArrayList<DietGoal> goals = null;
//		try {
//			HttpClient cli = new DefaultHttpClient();
//			URI uri = URIUtils.createURI("http", "dailyburn.com", -1,
//					"/api/diet_goals.xml", null, null);
//			HttpGet request = new HttpGet(uri);
//			consumer.sign(request);
//
//			ResponseHandler<String> responseHandler = new BasicResponseHandler();
//			String response = cli.execute(request, responseHandler);
//
//			LogHelper.LogD(response);
//			Object result = xstream.fromXML(response);
//			if (result instanceof NilClasses) {
//				goals = new ArrayList<DietGoal>();
//			} else {
//				goals = (ArrayList<DietGoal>) result;
//			}
//		} catch (OAuthMessageSignerException e) {
//			LogHelper.LogE(e.getMessage(), e);
//		} catch (OAuthExpectationFailedException e) {
//			LogHelper.LogE(e.getMessage(), e);
//		} catch (IllegalStateException e) {
//			LogHelper.LogE(e.getMessage(), e);
//		} catch (IOException e) {
//			LogHelper.LogE(e.getMessage(), e);
//		} catch (URISyntaxException e) {
//			LogHelper.LogE(e.getMessage(), e);
//		}
//		return goals;
//	}
}
