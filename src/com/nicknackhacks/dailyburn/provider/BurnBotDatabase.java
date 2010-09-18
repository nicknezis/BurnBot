package com.nicknackhacks.dailyburn.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nicknackhacks.dailyburn.LogHelper;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodColumns;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.FoodLogColumns;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.MealNameColumns;
import com.nicknackhacks.dailyburn.provider.BurnBotContract.UserColumns;

public class BurnBotDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "burnbot.db";
	private static final int DATABASE_VERSION = 2;
	
	interface Tables {
		String USER = "User";
		String FOODS = "Foods";
		String FOOD_LABELS = "FoodLabels";
		String FAV_FOODS = "FavoriteFoods";
		
		String FOOD_LOGS = "FoodLogs";
		
		String MEAL_NAMES = "MealNames";
		
		String FAV_FOODS_JOIN_FOODS = FAV_FOODS 
		+ " LEFT OUTER JOIN " + FOODS + " ON " 
		+ FOODS + "." + FoodColumns.FOOD_ID + "="
		+ FAV_FOODS + "." + FoodColumns.FOOD_ID;
	}
	
	public BurnBotDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LogHelper.LogD("Creating database");
		db.execSQL("CREATE TABLE " + Tables.USER + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ UserColumns.USER_ID + " INTEGER NOT NULL,"
				+ UserColumns.USER_NAME + " TEXT NOT NULL,"
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
				+ UserColumns.USER_SEX + " TEXT NOT NULL,"
				+ "UNIQUE (" + UserColumns.USER_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.FOODS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ FoodColumns.FOOD_ID + " INTEGER NOT NULL,"
				+ FoodColumns.FOOD_NAME + " TEXT NOT NULL,"
				+ FoodColumns.FOOD_BRAND + " TEXT,"
				+ FoodColumns.FOOD_CALORIES + " INTEGER NOT NULL,"
				+ FoodColumns.FOOD_PROTEIN + " REAL NOT NULL,"
				+ FoodColumns.FOOD_SERVING_SIZE + " TEXT NOT NULL,"
				+ FoodColumns.FOOD_TOTAL_CARBS + " REAL NOT NULL,"
				+ FoodColumns.FOOD_TOTAL_FAT + " REAL NOT NULL,"
				+ FoodColumns.FOOD_USER_ID + " INTEGER NOT NULL,"
				+ FoodColumns.FOOD_THUMB_URL + " TEXT NOT NULL,"
				+ FoodColumns.FOOD_USDA + " INTEGER NOT NULL,"
				+ " UNIQUE (" + FoodColumns.FOOD_ID + ") ON CONFLICT REPLACE)");
				
		db.execSQL("CREATE TABLE " + Tables.FAV_FOODS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ FoodColumns.FOOD_ID + " INTEGER NOT NULL,"
				+ " UNIQUE (" + FoodColumns.FOOD_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.FOOD_LOGS +" ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ FoodLogColumns.FOODLOG_CREATED_AT + " TEXT NOT NULL,"
				+ FoodLogColumns.FOODLOG_FOOD_ID + " INTEGER NOT NULL,"
				+ FoodLogColumns.FOODLOG_ID + " INTEGER NOT NULL,"
				+ FoodLogColumns.FOODLOG_MEAL_ID + " INTEGER NOT NULL,"
				+ FoodLogColumns.FOODLOG_LOGGED_ON + " TEXT NOT NULL,"
				+ FoodLogColumns.FOODLOG_SERVINGS_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_USER_ID + " INTEGER NOT NULL,"
				+ FoodLogColumns.FOODLOG_CALORIES_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_FAT_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_CARBS_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_PROTEIN_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_FOOD_NAME + " TEXT NOT NULL,"
				+ FoodLogColumns.FOODLOG_PIC_URL + " TEXT NOT NULL,"
				+ FoodLogColumns.FOODLOG_FIBER_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_SODIUM_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_CHOLESTEROL_EATEN + " REAL NOT NULL,"
				+ FoodLogColumns.FOODLOG_POTASSIUM_EATEN + " REAL NOT NULL,"
				+ " UNIQUE (" + FoodLogColumns.FOODLOG_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.MEAL_NAMES +" ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MealNameColumns.MEALNAME_ID + " INTEGER NOT NULL,"
				+ MealNameColumns.MEALNAME_NAME + " TEXT NOT NULL,"
				+ " UNIQUE (" + MealNameColumns.MEALNAME_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.FAV_FOODS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ FoodColumns.FOOD_ID + " INTEGER NOT NULL,"
				+ " UNIQUE (" + FoodColumns.FOOD_ID + ") ON CONFLICT REPLACE)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogHelper.LogD("onUpgrade() from " + oldVersion + " to " + newVersion);
		
		int version = oldVersion;
		
		if(version != DATABASE_VERSION) {
			LogHelper.LogW("Destroying old data during upgrade");
			
			db.execSQL("DROP TABLE IF EXISTS " + Tables.USER);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.FOODS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.FAV_FOODS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.FOOD_LOGS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.MEAL_NAMES);
			
			onCreate(db);
		}
	}

}
