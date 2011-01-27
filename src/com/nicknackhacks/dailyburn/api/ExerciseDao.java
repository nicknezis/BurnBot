package com.nicknackhacks.dailyburn.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.model.ExerciseSet;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;
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

		xstream.alias("exercise-sets", ArrayList.class);
		xstream.alias("exercise-set", ExerciseSet.class);
		xstream.registerConverter(new ExerciseSetConverter());
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

	@SuppressWarnings("unchecked")
	public List<ExerciseSet> getExerciseSets() {
		List<ExerciseSet> sets = null;
		try {
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/exercise_sets.xml", null, null);

			HttpGet request = new HttpGet(uri);
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			LogHelper.LogD(response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				sets = new ArrayList<ExerciseSet>();
			} else {
				sets = (ArrayList<ExerciseSet>) result;
			}
		} catch (Exception e) {
			LogHelper.LogD(e.getLocalizedMessage());
		}
		return sets;
	}
	
	public List<ExerciseSet> getExerciseSets(int year, int monthOfYear,
			int dayOfMonth) {
		ArrayList<ExerciseSet> sets = null;
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
						"/api/exercise_sets.xml", URLEncodedUtils.format(
								qparams, "UTF-8"), null);
				// String dateParam = "?date=" + formattedDate;
				request = new HttpGet(uri);
			} else {
				uri = URIUtils.createURI("https", "dailyburn.com", -1,
						"/api/exercise_sets.xml", null, null);
			}
			request = new HttpGet(uri);

			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			LogHelper.LogD( response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				sets = new ArrayList<ExerciseSet>();
			} else {
				sets = (ArrayList<ExerciseSet>) result;
			}
		} catch (OAuthMessageSignerException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (IllegalStateException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (IOException e) {
			LogHelper.LogE(e.getMessage(), e);
		} catch (URISyntaxException e) {
			LogHelper.LogE(e.getMessage(), e);
		}
		return sets;
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
