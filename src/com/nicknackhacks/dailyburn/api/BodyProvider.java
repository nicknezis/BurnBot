package com.nicknackhacks.dailyburn.api;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.nicknackhacks.dailyburn.R;

public class BodyProvider extends ContentProvider {

	public static final String AUTHORITY = "com.nicknackhacks.dailyburn.api.BodyProvider";
	private static final int BODY_WEIGHT = 1;
	
	private static final UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		sUriMatcher.addURI(AUTHORITY, "body_weight", BODY_WEIGHT);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/vnd.com.googlecode.chartdroid.graphable";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
//		pref = this.getSharedPreferences("dbdroid", 0);
//		// boolean isAuthenticated = pref.getBoolean("isAuthed", false);
//		String token = pref.getString("token", null);
//		String secret = pref.getString("secret", null);
//		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
//				getString(R.string.consumer_key),
//				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
//		consumer.setTokenWithSecret(token, secret);
//		bodyDao = new BodyDao(new DefaultHttpClient(), consumer);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
