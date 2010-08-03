package com.nicknackhacks.dailyburn.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.model.User;
import com.nicknackhacks.dailyburn.provider.DailyBurnContract.UserContract;

public class DailyBurnProvider extends ContentProvider {

	User user;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    
    private static final int USER = 100;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch(match) {
		case USER:
			return UserContract.CONTENT_ITEM_TYPE;
		default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DailyBurnContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "user", USER);
//        matcher.addURI(authority, "blocks/between/*/*", BLOCKS_BETWEEN);
//        matcher.addURI(authority, "blocks/*", BLOCKS_ID);
//        matcher.addURI(authority, "blocks/*/sessions", BLOCKS_ID_SESSIONS);
//
//        matcher.addURI(authority, "tracks", TRACKS);
//        matcher.addURI(authority, "tracks/*", TRACKS_ID);
//        matcher.addURI(authority, "tracks/*/sessions", TRACKS_ID_SESSIONS);
//        matcher.addURI(authority, "tracks/*/vendors", TRACKS_ID_VENDORS);
//
//        matcher.addURI(authority, "rooms", ROOMS);
//        matcher.addURI(authority, "rooms/*", ROOMS_ID);
//        matcher.addURI(authority, "rooms/*/sessions", ROOMS_ID_SESSIONS);
//
//        matcher.addURI(authority, "sessions", SESSIONS);
//        matcher.addURI(authority, "sessions/starred", SESSIONS_STARRED);
//        matcher.addURI(authority, "sessions/search/*", SESSIONS_SEARCH);
//        matcher.addURI(authority, "sessions/at/*", SESSIONS_AT);
//        matcher.addURI(authority, "sessions/*", SESSIONS_ID);
//        matcher.addURI(authority, "sessions/*/speakers", SESSIONS_ID_SPEAKERS);
//        matcher.addURI(authority, "sessions/*/tracks", SESSIONS_ID_TRACKS);
//        matcher.addURI(authority, "sessions/*/notes", SESSIONS_ID_NOTES);
//
//        matcher.addURI(authority, "speakers", SPEAKERS);
//        matcher.addURI(authority, "speakers/*", SPEAKERS_ID);
//        matcher.addURI(authority, "speakers/*/sessions", SPEAKERS_ID_SESSIONS);
//
//        matcher.addURI(authority, "vendors", VENDORS);
//        matcher.addURI(authority, "vendors/starred", VENDORS_STARRED);
//        matcher.addURI(authority, "vendors/search/*", VENDORS_SEARCH);
//        matcher.addURI(authority, "vendors/*", VENDORS_ID);
//
//        matcher.addURI(authority, "notes", NOTES);
//        matcher.addURI(authority, "notes/export", NOTES_EXPORT);
//        matcher.addURI(authority, "notes/*", NOTES_ID);
//
//        matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);

        return matcher;
    }
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER: {
            	user = new User(values);
            	Context context = getContext();
            	ContentResolver resolver = context.getContentResolver();
            	resolver.notifyChange(uri, null);
            	return uri;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
	}

	@Override
	public boolean onCreate() {
		user = new User();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		BurnBot.LogD("query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		
		final int match = sUriMatcher.match(uri);
        switch (match) {
        case USER:
        	final String[] columnNames = {
        			UserContract.USER_ID,
        			UserContract.USER_NAME, 
        			UserContract.USER_TIMEZONE,
        			UserContract.USER_METRIC_WEIGHTS,
        			UserContract.USER_METRIC_DISTANCE,
        			UserContract.USER_CAL_GOALS_MET,
        			UserContract.USER_DAYS_EXERCISED,
        			UserContract.USER_PICTURE_URL,
        			UserContract.USER_URL,
        			UserContract.USER_CAL_BURNED,
        			UserContract.USER_CAL_CONSUMED,
        			UserContract.USER_BODY_WEIGHT,
        			UserContract.USER_BODY_WEIGHT_GOAL,
        			UserContract.USER_PRO,
        			UserContract.USER_CREATED_AT,
        			UserContract.USER_DYN_DIET_GOALS
        			};
        	final MatrixCursor cursor = new MatrixCursor(columnNames, 1);
        	Object[] values = new Object[] {
        			user.getId(),
        			user.getUsername(),
        			user.getTimeZone(),
        			(user.isUsesMetricWeights()? 1:0),
        			(user.isUsesMetricDistances()? 1:0),
        			user.getCalGoalsMetInPastWeek(),
        			user.getDaysExercisedInPastWeek(),
        			user.getPictureUrl(),
        			user.getUrl(),
        			user.getCaloriesBurned(),
        			user.getCaloriesConsumed(),
        			user.getBodyWeight(),
        			user.getBodyWeightGoal(),
        			(user.isPro()? 1:0),
        			user.getCreatedAt(),
        			(user.isDynamicDietGoals()? 1:0)
        	};
        	cursor.addRow(values);
        	cursor.setNotificationUri(getContext().getContentResolver(), UserContract.CONTENT_URI);
        	return cursor;
        }
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
