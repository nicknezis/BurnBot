package com.nicknackhacks.dailyburn;

import android.util.Log;

public class LogHelper {

	public static final String TAG = "BurnBot";
	
	public static void LogD(String msg) {
		LogD("%s",msg);
	}
	
	public static void LogD(String msg, Throwable tr) {
		LogD("%s",tr,msg);
	}
	
	public static void LogD(String format, Object... args) {
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, String.format(format, args));
	}
	
	public static void LogD(String format, Throwable tr, Object... args) {
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, String.format(format, args), tr);
	}
		
	public static void LogI(String msg) {
		LogI("%s",msg);
	}

	public static void LogI(String msg, Throwable tr) {
		LogI("%s",tr,msg);
	}

	public static void LogI(String format, Object... args) {
		if (Log.isLoggable(TAG, Log.INFO))
			Log.i(TAG, String.format(format, args));
	}
	
	public static void LogI(String format, Throwable tr, Object... args) {
		if (Log.isLoggable(TAG, Log.INFO))
			Log.i(TAG, String.format(format, args), tr);
	}
	
	public static void LogW(String msg) {
		LogW("%s",msg);
	}
	
	public static void LogW(String msg, Throwable tr) {
		LogW("%s",tr,msg);
	}
	
	public static void LogW(String format, Object... args) {
		if (Log.isLoggable(TAG, Log.WARN))
			Log.w(TAG, String.format(format, args));
	}
	
	public static void LogW(String format, Throwable tr, Object... args) {
		if (Log.isLoggable(TAG, Log.WARN))
			Log.w(TAG, String.format(format, args), tr);
	}
	
	public static void LogE(String msg) {
		LogE("%s",msg);
	}
	
	public static void LogE(String msg, Throwable tr) {
		LogE("%s",tr,msg);
	}
	
	public static void LogE(String format, Object... args) {
		if (Log.isLoggable(TAG, Log.ERROR))
			Log.e(TAG, String.format(format, args));
	}

	public static void LogE(String format, Throwable tr, Object... args) {
		if (Log.isLoggable(TAG, Log.ERROR))
			Log.e(TAG, String.format(format, args), tr);
	}

}
