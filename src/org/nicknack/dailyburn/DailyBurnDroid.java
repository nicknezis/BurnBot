package org.nicknack.dailyburn;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Application;

public class DailyBurnDroid extends Application {

	public HashMap<Long, WeakReference<Object> > objects = new HashMap<Long, WeakReference<Object> >();
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onTerminate() {
		objects.clear();
		super.onTerminate();
	}
}
