package com.nicknackhacks.dailyburn.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.nicknackhacks.dailyburn.DailyBurnDroid;
import com.nicknackhacks.dailyburn.model.DietGoal;
import com.nicknackhacks.dailyburn.model.DietGoals;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.thoughtworks.xstream.XStream;

public class DietDao {

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
	
	public void shutdown() {
		client.getConnectionManager().shutdown();
	}

	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800): <diet-goal>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <dynamic type="boolean">true</dynamic>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <id type="integer">2956609</id>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <lower-bound type="integer">48</lower-bound>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <upper-bound type="integer">83</upper-bound>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <user-id type="integer">176766</user-id>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <unit>grams</unit>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <goal-type>TotalFatDietGoal</goal-type>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <adjusted-lower-bound type="integer">48</adjusted-lower-bound>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800):   <adjusted-upper-bound type="integer">83</adjusted-upper-bound>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800): </diet-goal>
	//04-19 22:53:57.733: DEBUG/DailyBurnDroid(2800): </diet-goals>
	private void configureXStream() {
		xstream = new XStream();
		xstream.alias("diet-goals", DietGoals.class);
		xstream.addImplicitCollection(DietGoals.class, "goals");
		xstream.alias("diet-goal", DietGoal.class);
		xstream.aliasField("lower-bound", DietGoal.class, "lowerBound");
		xstream.aliasField("upper-bound", DietGoal.class, "upperBound");
		xstream.aliasField("user-id", DietGoal.class, "userId");
		xstream.aliasField("goal-type", DietGoal.class, "goalType");
		xstream.aliasField("adjusted-lower-bound", DietGoal.class, "adjustedLowerBound");
		xstream.aliasField("adjusted-upper-bound", DietGoal.class, "adjustedUpperBound");
				
		xstream.alias("nil-classes", NilClasses.class);		
	}

	public List<DietGoal> getDietGoals() {
		DietGoals goals = null;
		try {
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/diet_goals.xml", null, null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);
			HttpResponse response = client.execute(request);
//			 //USE TO PRINT TO LogCat (Make a filter on dailyburndroid tag)
//			 BufferedReader in = new BufferedReader(new
//			 InputStreamReader(response.getEntity().getContent()));
//			 String line = null;
//			 while((line = in.readLine()) != null) {
//			 Log.d(DailyBurnDroid.TAG,line);
//			 }
			goals = (DietGoals) xstream.fromXML(response.getEntity().getContent());
		} catch (OAuthMessageSignerException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		} catch (URISyntaxException e) {
			Log.e("dailyburndroid", e.getMessage());
			e.printStackTrace();
		}
		return goals.goals;

	}
}
