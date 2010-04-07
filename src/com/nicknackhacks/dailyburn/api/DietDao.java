package com.nicknackhacks.dailyburn.api;

import java.io.IOException;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.nicknackhacks.dailyburn.model.DietGoal;
import com.nicknackhacks.dailyburn.model.DietGoals;
import com.nicknackhacks.dailyburn.model.GoalType;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.enums.EnumSingleValueConverter;

public class DietDao {

	private final static String TAG = "dailyburndroid";
	private CommonsHttpOAuthConsumer consumer;
	DefaultHttpClient client;
	private XStream xstream;

	public DietDao(DefaultHttpClient client, CommonsHttpOAuthConsumer consumer) {

//		HttpParams parameters = new BasicHttpParams();
//		SchemeRegistry schemeRegistry = new SchemeRegistry();
//		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
//		sslSocketFactory
//				.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
//		ClientConnectionManager manager = new ThreadSafeClientConnManager(
//				parameters, schemeRegistry);
//		this.client = new DefaultHttpClient(manager, parameters);
		this.client = new DefaultHttpClient();

		this.consumer = consumer;
		configureXStream();
	}

	private void configureXStream() {
		xstream = new XStream();
		
		xstream.alias("diet-goals", DietGoals.class);
		xstream.addImplicitCollection(DietGoals.class, "goals");
		xstream.alias("diet-goal", DietGoal.class);
		xstream.aliasField("lower-bound", DietGoal.class, "lowerBound");
		xstream.aliasField("upper-bound", DietGoal.class, "upperBound");
		xstream.aliasField("user-id", DietGoal.class, "userId");
		xstream.aliasField("adjusted-lower-bound", DietGoal.class, "adjustedLowerBound");
		xstream.aliasField("adjusted-upper-bound", DietGoal.class, "adjustedUpperBound");
		xstream.aliasField("goal-type", DietGoal.class, "goalType");
		//xstream.alias("goal-type", GoalType.class);
		xstream.registerConverter(new EnumSingleValueConverter(GoalType.class));
		
		//xstream.registerConverter(new FoodConverter());

		xstream.alias("nil-classes", NilClasses.class);
	}

	private List<DietGoal> getDietGoals() {
		DietGoals goals = null;
		try {
			HttpGet request = new HttpGet(
					"https://dailyburn.com/api/foods/favorites.xml");
			consumer.sign(request);
			HttpResponse response = client.execute(request);
			goals = (DietGoals) xstream.fromXML(response.getEntity().getContent());
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
		return goals.goals;

	}
}
