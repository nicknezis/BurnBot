package com.nicknackhacks.dailyburn.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

import com.nicknackhacks.dailyburn.DailyBurnDroid;
import com.nicknackhacks.dailyburn.model.BodyLogEntries;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.BodyMetrics;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.thoughtworks.xstream.XStream;

public class BodyDao {

	private CommonsHttpOAuthConsumer consumer;
	DefaultHttpClient client;
	private XStream xstream;

	public BodyDao(DefaultHttpClient client, CommonsHttpOAuthConsumer consumer) {

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
		xstream.alias("body-metrics", BodyMetrics.class);
		xstream.addImplicitCollection(BodyMetrics.class, "metrics");
		xstream.alias("body-metric", BodyMetric.class);
		xstream.registerConverter(new BodyMetricConverter());

		xstream.alias("body-log-entries", BodyLogEntries.class);
		xstream.addImplicitCollection(BodyLogEntries.class, "body-log-entry");
		xstream.alias("body-log-entry", BodyMetric.class);
		xstream.registerConverter(new BodyLogEntryConverter()); 
		
		xstream.alias("nil-classes", NilClasses.class);

	}

	public List<BodyMetric> getBodyMetrics() {
		BodyMetrics metrics = null;
		try {
		URI uri = URIUtils.createURI("https", "dailyburn.com", -1, 
				"/api/body_metrics", null, null);
		HttpGet request = new HttpGet(uri);
		consumer.sign(request);
		HttpResponse response = client.execute(request);
		
		metrics = (BodyMetrics) xstream.fromXML(response.getEntity().getContent());
		} catch (Exception e) {
			Log.d(DailyBurnDroid.TAG, e.getMessage());
		}
		return metrics.metrics;
	}
	
	public List<BodyLogEntry> getBodyLogEntries() {
		BodyLogEntries entries = null;
		try {
		URI uri = URIUtils.createURI("https", "dailyburn.com", -1, 
				"/api/body_log_entries.xml", null, null);
		HttpGet request = new HttpGet(uri);
		consumer.sign(request);
		HttpResponse response = client.execute(request);
		
		entries = (BodyLogEntries) xstream.fromXML(response.getEntity().getContent());
		} catch (Exception e) {
			Log.d(DailyBurnDroid.TAG, e.getMessage());
		}
		return entries.entries;
	}
	
	//https://dailyburn.com/api/body_log_entries.format
//	public void addFavoriteFood(int id) throws OAuthMessageSignerException,
//			OAuthExpectationFailedException, OAuthNotAuthorizedException,
//			ClientProtocolException, IOException {
//		// create a request that requires authentication
//		HttpPost post = new HttpPost(
//				"https://dailyburn.com/api/foods/add_favorite");
//		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//		// 'status' here is the update value you collect from UI
//		nvps.add(new BasicNameValuePair("id", String.valueOf(id)));
//		post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
//		// set this to avoid 417 error (Expectation Failed)
//		post.getParams().setBooleanParameter(
//				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//		// sign the request
//		consumer.sign(post);
//		// send the request
//		final HttpResponse response = client.execute(post);
//		// response status should be 200 OK
//		int statusCode = response.getStatusLine().getStatusCode();
//		final String reason = response.getStatusLine().getReasonPhrase();
//		// release connection
//		response.getEntity().consumeContent();
//		if (statusCode != 200) {
//			Log.e("dailyburndroid", reason);
//			throw new OAuthNotAuthorizedException();
//		}
//	}
	
	public void deleteBodyLogEntry(int entryId) throws ClientProtocolException,
			IOException, OAuthNotAuthorizedException, URISyntaxException,
			OAuthMessageSignerException, OAuthExpectationFailedException {
		URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
				"/api/body_log_entries" + String.valueOf(entryId), null, null);
		HttpDelete delete = new HttpDelete(uri);
		consumer.sign(delete);
		HttpResponse response = client.execute(delete);
		int statusCode = response.getStatusLine().getStatusCode();
		final String reason = response.getStatusLine().getReasonPhrase();
		response.getEntity().consumeContent();
		if (statusCode != 200) {
			Log.e(DailyBurnDroid.TAG, reason);
			throw new OAuthNotAuthorizedException();
		}
	}
}
