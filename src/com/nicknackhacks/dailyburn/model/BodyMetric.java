package com.nicknackhacks.dailyburn.model;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.util.DataHelper;

//<?xml version="1.0" encoding="UTF-8"?>
//<body-metrics type="array">
//<body-metric>
//  <id type="integer">8</id>
//  <name>Body Weight</name>
//  <pro type="boolean" nil="true"></pro>
//  <body-metric-identifier>body_weight</body-metric-identifier>
//  <unit>lbs|kg</unit>
//</body-metric>
//...
//<body-metric>
//  <id type="integer">646</id>
//  <name>Energy Level</name>
//  <pro type="boolean">true</pro>
//  <body-metric-identifier>energy_level</body-metric-identifier>
//  <unit>(1-10)</unit>
//</body-metric>
//</body-metrics>
public class BodyMetric {
	
	/**
	 * 
	 */
	public BodyMetric() {
	}

	/**
	 * @param id
	 * @param name
	 * @param pro
	 * @param metricIdentifier
	 * @param unit
	 */
	public BodyMetric(int id, String name, boolean pro,
			String metricIdentifier, String unit) {
		this.id = id;
		this.name = name;
		this.pro = pro;
		this.metricIdentifier = metricIdentifier;
		this.unit = unit;
	}

	private int id;
	private String name;
	private boolean pro;
	private String metricIdentifier;
	private String unit;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPro() {
		return pro;
	}

	public void setPro(boolean pro) {
		this.pro = pro;
	}

	public String getMetricIdentifier() {
		return metricIdentifier;
	}

	public void setMetricIdentifier(String identifier) {
		this.metricIdentifier = identifier;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public String toString() {
		if(isPro()) {
			return name + " (Pro)";
		}
		
		return name;
	}
	
	/**
	 * Returns all
	 */
	public static ArrayList<BodyMetric> getAll(Context context) {
		BodyMetricCache cache = new BodyMetricCache(context);
		SQLiteDatabase db = cache.getReadableDatabase();
		ArrayList<BodyMetric> list = new ArrayList<BodyMetric>();
		Cursor cursor = db.query(BodyMetricCache.BODYMETRICS_TABLE_NAME, new String[] {"id", "name", "pro", "metricIdentifier", "unit"}, 
				null, null, null, null, null);
		
		try {
			
			if(cursor.moveToFirst()) {
				do {
					BodyMetric metric = new BodyMetric(cursor.getInt(0),
							cursor.getString(1),
							cursor.getInt(2) == 1,
							cursor.getString(3),
							cursor.getString(4));
					list.add(metric);
				} while (cursor.moveToNext());
			}
		}
		catch (Exception e) {
			// TODO Do something useful with this exception.
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}

			db.close();
		}
		
		return list;
	}
	
	public static void putAll(Context context, ArrayList<BodyMetric> list) {
		Log.d("BurnBot.BodyMetric.putAll", "Writing BodyMeric data to the cache");
		
		BodyMetricCache cache = new BodyMetricCache(context);
		SQLiteDatabase db = cache.getWritableDatabase();
		
		String insertSql = "INSERT INTO " + BodyMetricCache.BODYMETRICS_TABLE_NAME + 
			" (id, name, pro, metricIdentifier, unit) VALUES (?, ?, ?, ?, ?)";
		
		SQLiteStatement insert = db.compileStatement(insertSql);
		
		try {
			for (BodyMetric bodyMetric : list) {
				Log.d("BurnBot.BodyMetric.putAll", "Writing " + bodyMetric.getName());
				
				insert.clearBindings();
				
				insert.bindLong(0, bodyMetric.getId());
				insert.bindString(1, bodyMetric.getName());
				if(bodyMetric.pro) {
					insert.bindLong(2, 1);
				} else {
					insert.bindLong(2, 0);
				}
				insert.bindString(3, bodyMetric.getMetricIdentifier());
				insert.bindString(4, bodyMetric.getUnit());
				
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
	
	public static class BodyMetricCache extends DataHelper {
		private static final String BODYMETRICS_TABLE_NAME = "bodyMetrics";	
		private static final String BODYMETRICS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + BODYMETRICS_TABLE_NAME + 
			" (id INTEGER NOT NULL PRIMARY KEY ASC, " +
			"name NOT NULL UNIQUE, " +
			"pro BOOLEAN NOT NULL, " +
			"metricIdentifier TEXT NOT NULL UNIQUE, " +
			"unit TEXT NOT NULL);";

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
}
