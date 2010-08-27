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

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.thoughtworks.xstream.XStream;

public class BodyDao {

	private CommonsHttpOAuthConsumer consumer;
	private HttpClient client;
	private XStream xstream;

	public BodyDao(BurnBot app) {
		this.client = app.getHttpClient();
		this.consumer = app.getOAuthConsumer();
		configureXStream();
	}

	private void configureXStream() {
		xstream = new XStream();

		xstream.alias("body-metrics", ArrayList.class);
		xstream.alias("body-metric", BodyMetric.class);
		xstream.registerConverter(new BodyMetricConverter());

		xstream.alias("body-log-entries", ArrayList.class);
		xstream.alias("body-log-entry", BodyLogEntry.class);
		xstream.registerConverter(new BodyLogEntryConverter());

		xstream.alias("nil-classes", NilClasses.class);

	}

	public List<BodyMetric> getBodyMetrics() {
		ArrayList<BodyMetric> metrics = null;
		try {
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/body_metrics", null, null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			LogHelper.LogD(response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				metrics = new ArrayList<BodyMetric>();
			} else {
				metrics = (ArrayList<BodyMetric>) result;
			}
		} catch (Exception e) {
			LogHelper.LogE(e.getMessage(), e);
		}
		return metrics;
	}

	public List<BodyLogEntry> getBodyLogEntries(String bodyMetricIdentifier) {
		ArrayList<BodyLogEntry> entries = null;
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("body_metric_identifier",
					bodyMetricIdentifier));
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/body_log_entries.xml", URLEncodedUtils.format(
							qparams, "UTF-8"), null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				entries = new ArrayList<BodyLogEntry>();
			} else {
				entries = (ArrayList<BodyLogEntry>) result;
			}
		} catch (Exception e) {
			LogHelper.LogE(e.getMessage(), e);
		}
		return entries;
	}

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
			LogHelper.LogE(reason);
			throw new OAuthNotAuthorizedException();
		}
	}

	/*
	 * body_log_entry[body_metric_identifier] - a string value pulled from the
	 * Body Metric response. body_log_entry[value] - the decimal value entered
	 * by the user. body_log_entry[unit] - the unit selected by the user, in the
	 * same form given by the Body Metric. Optional Parameters:
	 * body_log_entry[logged_on] - a date value (YYYY-MM-DD) pulled from the
	 * Body Metric response.
	 */
	public void addBodyLogEntry(BodyLogEntry entry)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, ClientProtocolException,
			IOException, OAuthNotAuthorizedException {

		addBodyLogEntry(entry.getMetricIdentifier(), String.valueOf(entry
				.getValue()), entry.getUnit());
	}

	public void addBodyLogEntry(String identifier, String value, String unit)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, ClientProtocolException,
			IOException, OAuthNotAuthorizedException {
		// create a request that requires authentication
		URI uri = null;
		try {
			uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"api/body_log_entries.xml", null, null);
		} catch (URISyntaxException e) {
			LogHelper.LogE(e.getMessage(), e);
		}
		HttpPost post = new HttpPost(uri);
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		// 'status' here is the update value you collect from UI
		nvps.add(new BasicNameValuePair(
				"body_log_entry[body_metric_identifier]", identifier));
		nvps.add(new BasicNameValuePair("body_log_entry[value]", value));
		nvps.add(new BasicNameValuePair("body_log_entry[unit]", unit));
		// GregorianCalendar cal = new GregorianCalendar(year, monthOfYear,
		// dayOfMonth);
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// String formattedDate = format.format(cal.getTime());
		// nvps.add(new BasicNameValuePair("food_log_entry[logged_on]",
		// formattedDate));
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
			LogHelper.LogE(reason);
			throw new OAuthNotAuthorizedException();
		}
	}
}
