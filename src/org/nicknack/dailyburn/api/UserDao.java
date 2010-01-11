package org.nicknack.dailyburn.api;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.OAuthConsumer;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.nicknack.dailyburn.model.User;

import com.thoughtworks.xstream.XStream;

public class UserDao {

	OAuthConsumer consumer;
	XStream xstream;
	
	public UserDao(HttpClient client, OAuthConsumer consumer) {
		this.consumer = consumer;
		this.configureXStream();
	}

	private void configureXStream() {
        xstream = new XStream();
        xstream.alias("user", User.class);
        xstream.aliasField("time-zone", User.class, "timeZone");
        xstream.aliasField("uses-metric-weights", User.class, "usesMetricWeights");
        xstream.aliasField("uses-metric-distances", User.class, "usesMetricDistances");
        xstream.aliasField("cal-goals-met-in-past-week", User.class, "calGoalsMetInPastWeek");
        xstream.aliasField("days-exercised-in-past-week", User.class, "daysExercisedInPastWeek");
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
            URL url = new URL("https://dailyburn.com/api/users/current.xml");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            if (connection instanceof HttpsURLConnection) {
            	((HttpsURLConnection) connection)
            	.setHostnameVerifier(new AllowAllHostnameVerifier());
            	}

            // sign the request (consumer is a Signpost DefaultOAuthConsumer)
            consumer.sign(connection);

            // send the request
            connection.connect();
            
            user = (User)xstream.fromXML(connection.getInputStream());
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return user;
	}

	public void setConsumer(OAuthConsumer consumer) {
		this.consumer = consumer;
	}
}
