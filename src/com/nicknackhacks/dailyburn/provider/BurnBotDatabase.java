package com.nicknackhacks.dailyburn.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.provider.DailyBurnContract.UserColumns;
import com.nicknackhacks.dailyburn.provider.DailyBurnContract.UserContract;

public class BurnBotDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "burnbot.db";
	private static final int DATABASE_VERSION = 1;
	
	interface Tables {
		String USER = "user";
	}
	
	public BurnBotDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		BurnBot.LogD("Creating database");
		db.execSQL("CREATE TABLE " + Tables.USER + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ UserColumns.USER_ID + " INTEGER NOT NULL,"
				+ UserColumns.USER_TIMEZONE + " TEXT NOT NULL,"
				+ UserColumns.USER_METRIC_WEIGHTS + " INTEGER NOT NULL,"
				+ UserColumns.USER_METRIC_DISTANCE + " INTEGER NOT NULL,"
				+ UserColumns.USER_CAL_GOALS_MET + " INTEGER NOT NULL,"
				+ UserColumns.USER_DAYS_EXERCISED + " INTEGER NOT NULL,"
				+ UserColumns.USER_PICTURE_URL + " TEXT NOT NULL,"
				+ UserColumns.USER_URL + " TEXT NOT NULL,"
				+ UserColumns.USER_CAL_BURNED + " INTEGER NOT NULL,"
				+ UserColumns.USER_CAL_CONSUMED + " INTEGER NOT NULL,"
				+ UserColumns.USER_BODY_WEIGHT + " REAL NOT NULL,"
				+ UserColumns.USER_BODY_WEIGHT_GOAL + " REAL NOT NULL,"
				+ UserColumns.USER_PRO + " INTEGER NOT NULL,"
				+ UserColumns.USER_CREATED_AT + " TEXT NOT NULL,"
				+ UserColumns.USER_DYN_DIET_GOALS + " INTEGER NOT NULL,"
				+ "UNIQUE (" + UserColumns.USER_ID + ") ON CONFLICT REPLACE)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		BurnBot.LogD("onUpgrade() from " + oldVersion + " to " + newVersion);
		
		int version = oldVersion;
		
		if(version != DATABASE_VERSION) {
			BurnBot.LogW("Destroying old data during upgrade");
			
			db.execSQL("DROP TABLE IF EXISTS " + Tables.USER);
			
			onCreate(db);
		}
	}

}
