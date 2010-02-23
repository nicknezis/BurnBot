package org.nicknack.dailyburn.api;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.nicknack.dailyburn.model.User;

import com.thoughtworks.xstream.XStream;

public class UserDao {

	CommonsHttpOAuthConsumer consumer;
	DefaultHttpClient client;
	XStream xstream;

	public UserDao(HttpClient client, CommonsHttpOAuthConsumer consumer) {
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
		this.configureXStream();
	}

	public void shutdown() {
		client.getConnectionManager().shutdown();
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

	}

	public User getUserInfo() {
		User user = null;
		try {
			// create an HTTP request to a protected resource
			HttpGet request = new HttpGet(
					"https://dailyburn.com/api/users/current.xml");
			consumer.sign(request);
			HttpResponse response = client.execute(request);

			user = (User) xstream.fromXML(response.getEntity().getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	public void setConsumer(CommonsHttpOAuthConsumer consumer) {
		this.consumer = consumer;
	}
}
