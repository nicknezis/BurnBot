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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.InputFilter.LengthFilter;
import android.util.Log;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.adapters.BodyMetricsAdaptor;
import com.nicknackhacks.dailyburn.model.BodyLogEntry;
import com.nicknackhacks.dailyburn.model.BodyMetric;
import com.nicknackhacks.dailyburn.model.NilClasses;
import com.nicknackhacks.dailyburn.util.DataHelper;
import com.thoughtworks.xstream.XStream;

public class BodyDao {

	public static class BodyMetricCache extends DataHelper {
		public static final String BODYMETRICS_TABLE_NAME = "bodyMetrics";	
		private static final String BODYMETRICS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + BODYMETRICS_TABLE_NAME + 
			" (id INTEGER NOT NULL PRIMARY KEY ASC, " +
			"name NOT NULL UNIQUE, " +
			"pro BOOLEAN NOT NULL, " +
			"metricIdentifier TEXT NOT NULL UNIQUE, " +
			"unit TEXT NOT NULL);";
	
		private static final int ID = 1;
		
		public BodyMetricCache(Context context) {
			super(context);
			
			
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			super.onCreate(db);
			
			db.execSQL(BODYMETRICS_TABLE_CREATE);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			super.onUpgrade(db, oldVersion, newVersion);
		}
		
	}

	private CommonsHttpOAuthConsumer consumer;
	private HttpClient client;
	private XStream xstream;
	
	private BodyDao.BodyMetricCache cache;
	private SQLiteDatabase db = cache.getWritableDatabase();

	public BodyDao(BurnBot app) {
		this.client = app.getHttpClient();
		this.consumer = app.getOAuthConsumer();
		configureXStream();
		
		cache = new BodyDao.BodyMetricCache(app.getApplicationContext());
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

	public ArrayList<BodyMetric> getBodyMetrics(Context context) {
		ArrayList<BodyMetric> metrics = null;
		
		metrics = getAll(context);
		
		// If the metrics were cached, return them.
		if(metrics.size() > 0) {
			return metrics;
		}
		
		try {
			URI uri = URIUtils.createURI("https", "dailyburn.com", -1,
					"/api/body_metrics", null, null);
			HttpGet request = new HttpGet(uri);
			consumer.sign(request);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(request, responseHandler);

			if (Log.isLoggable(BurnBot.TAG, Log.DEBUG))
				Log.d(BurnBot.TAG, response);

			Object result = xstream.fromXML(response);
			if (result instanceof NilClasses) {
				metrics = new ArrayList<BodyMetric>();
			} else {
				metrics = (ArrayList<BodyMetric>) result;
			}
		} catch (Exception e) {
			if (Log.isLoggable(BurnBot.TAG, Log.ERROR))
				Log.e(BurnBot.TAG, e.getMessage());
		}
		
		// Cache the metrics for next time.
		putAll(context, metrics);
		
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
			if (Log.isLoggable(BurnBot.TAG, Log.ERROR))
				Log.e(BurnBot.TAG, e.getMessage());
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
			if (Log.isLoggable(BurnBot.TAG, Log.ERROR))
				Log.e(BurnBot.TAG, reason);
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
			if (Log.isLoggable(BurnBot.TAG, Log.ERROR))
				Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
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
			if (Log.isLoggable(BurnBot.TAG, Log.ERROR))
				Log.e(BurnBot.TAG, reason);
			throw new OAuthNotAuthorizedException();
		}
	}
	
	/**
	 * Retrieves a single Body Metric from the cache.
	 * @param name The name of the metric to fetch.
	 * @return
	 */
	public BodyMetric getMetricDetailsByName(String name) {
		BodyMetric metric = new BodyMetric(); 
			
		Cursor cursor = db.query(BodyMetricCache.BODYMETRICS_TABLE_NAME, 
				new String[] {"id", "name", "pro", "metricIdentifier", "unit"}, 
				"name = '?'", 
				new String[] {name}, 
				null, 
				null, 
				null);
		
		if(cursor.moveToFirst()) {
			metric.setId(cursor.getInt(cursor.getColumnIndex("id")));
			metric.setName(cursor.getString(cursor.getColumnIndex("name")));
			metric.setPro(cursor.getInt(cursor.getColumnIndex("pro")) != 0);
			metric.setMetricIdentifier(cursor.getString(cursor.getColumnIndex("metricIdentifier")));
			metric.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
		}

		return metric;
	}

	/**
	 * Returns all
	 */
	public ArrayList<BodyMetric> getAll(Context context) {
		ArrayList<BodyMetric> list = new ArrayList<BodyMetric>();
		Cursor cursor = db.query(BodyMetricCache.BODYMETRICS_TABLE_NAME, new String[] {"id", "name", "pro", "metricIdentifier", "unit"}, 
				null, null, null, null, null);
		
		try {
			
			if(cursor.moveToFirst()) {
				do {
					boolean pro = false;
					if(cursor.getInt(2) == 1) {
						pro = true;
					}
					
					BodyMetric metric = new BodyMetric(cursor.getInt(0),
							cursor.getString(1),
							pro,
							cursor.getString(3),
							cursor.getString(4));
					list.add(metric);
				} while (cursor.moveToNext());
			}
		}
		catch (Exception e) {
			// TODO Do something useful with this exception.
			Log.e("BurnBot.BodyMeric.getAll", e.getMessage());
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
	
			db.close();
		}
		
		return list;
	}

	public void putAll(Context context, ArrayList<BodyMetric> list) {
		Log.d("BurnBot.BodyMetric.putAll", "Writing BodyMeric data to the cache");
		
		String insertSql = "INSERT INTO " + BodyDao.BodyMetricCache.BODYMETRICS_TABLE_NAME + 
			" (id, name, pro, metricIdentifier, unit) VALUES (?, ?, ?, ?, ?)";
		
		SQLiteStatement insert = db.compileStatement(insertSql);
		
		try {
			for (BodyMetric bodyMetric : list) {
				Log.d("BurnBot.BodyMetric.putAll", "Writing " + bodyMetric.getName());
				
				insert.clearBindings();
				
				insert.bindLong(1, bodyMetric.getId());
				insert.bindString(2, bodyMetric.getName());
				if(bodyMetric.pro) {
					insert.bindLong(3, 1);
				} else {
					insert.bindLong(3, 0);
				}
				insert.bindString(4, bodyMetric.getMetricIdentifier());
				insert.bindString(5, bodyMetric.getUnit());
				
				insert.execute();
			}
		} catch (Exception e) {
			// TODO Do something useful with this exception.
			Log.d("BurnBot.BodyMetric.putAll", "Caught and exception " + e.getMessage());
		} finally {
			insert.close();
			db.close();
		}
		
	}
}
