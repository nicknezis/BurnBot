package com.nicknackhacks.dailyburn.provider;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserContract;
import com.nicknackhacks.dailyburn.provider.BurnBotDatabase.Tables;
import com.nicknackhacks.dailyburn.util.SelectionBuilder;

public class BurnBotProvider extends ContentProvider {

	// User user;
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final int USER = 100;

	private BurnBotDatabase mOpenHelper;

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
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
		final String authority = BurnBotContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, "user", USER);
		// matcher.addURI(authority, "blocks/between/*/*", BLOCKS_BETWEEN);
		// matcher.addURI(authority, "blocks/*", BLOCKS_ID);
		// matcher.addURI(authority, "blocks/*/sessions", BLOCKS_ID_SESSIONS);
		//
		// matcher.addURI(authority, "tracks", TRACKS);
		// matcher.addURI(authority, "tracks/*", TRACKS_ID);
		// matcher.addURI(authority, "tracks/*/sessions", TRACKS_ID_SESSIONS);
		// matcher.addURI(authority, "tracks/*/vendors", TRACKS_ID_VENDORS);
		//
		// matcher.addURI(authority, "rooms", ROOMS);
		// matcher.addURI(authority, "rooms/*", ROOMS_ID);
		// matcher.addURI(authority, "rooms/*/sessions", ROOMS_ID_SESSIONS);
		//
		// matcher.addURI(authority, "sessions", SESSIONS);
		// matcher.addURI(authority, "sessions/starred", SESSIONS_STARRED);
		// matcher.addURI(authority, "sessions/search/*", SESSIONS_SEARCH);
		// matcher.addURI(authority, "sessions/at/*", SESSIONS_AT);
		// matcher.addURI(authority, "sessions/*", SESSIONS_ID);
		// matcher.addURI(authority, "sessions/*/speakers",
		// SESSIONS_ID_SPEAKERS);
		// matcher.addURI(authority, "sessions/*/tracks", SESSIONS_ID_TRACKS);
		// matcher.addURI(authority, "sessions/*/notes", SESSIONS_ID_NOTES);
		//
		// matcher.addURI(authority, "speakers", SPEAKERS);
		// matcher.addURI(authority, "speakers/*", SPEAKERS_ID);
		// matcher.addURI(authority, "speakers/*/sessions",
		// SPEAKERS_ID_SESSIONS);
		//
		// matcher.addURI(authority, "vendors", VENDORS);
		// matcher.addURI(authority, "vendors/starred", VENDORS_STARRED);
		// matcher.addURI(authority, "vendors/search/*", VENDORS_SEARCH);
		// matcher.addURI(authority, "vendors/*", VENDORS_ID);
		//
		// matcher.addURI(authority, "notes", NOTES);
		// matcher.addURI(authority, "notes/export", NOTES_EXPORT);
		// matcher.addURI(authority, "notes/*", NOTES_ID);
		//
		// matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mOpenHelper = new BurnBotDatabase(context);
		return true;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		BurnBot.LogD("insert(URI=" + uri + ", values=" + values.toString()
				+ ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case USER: {
			db.insertOrThrow(Tables.USER, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		BurnBot.LogD("query(uri=" + uri + ", proj="
				+ Arrays.toString(projection) + ")");
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		final int match = sUriMatcher.match(uri);
		switch (match) {
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
			case USER: {
				final SelectionBuilder builder = new SelectionBuilder();
				builder.table(Tables.USER);
				return builder.where(selection, selectionArgs).query(db,
						projection, sortOrder);
			}
		}
	}

    /** {@inheritDoc} */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		BurnBot.LogD("update(URI=" + uri + ", values=" + values.toString() + ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		return builder.where(selection, selectionArgs).update(db, values);
	}

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        BurnBot.LogD("delete(URI=" + uri + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).delete(db);
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER: {
                return builder.table(Tables.USER);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
