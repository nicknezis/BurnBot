package com.nicknackhacks.dailyburn;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Application;
import android.util.Log;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

public class BurnBot extends Application {

	public static String TAG = "BurnBot";
	private ThumbnailBus bus=new ThumbnailBus();
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache=
							new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(null, null, 101, bus);
	public HashMap<Long, WeakReference<Object> > objects = new HashMap<Long, WeakReference<Object> >();
	
	public BurnBot() {
		super();
		
		Thread.setDefaultUncaughtExceptionHandler(onBlooey);
	}
	
	public void goBlooey(Throwable t) {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		
		builder
			.setTitle(R.string.exception)
			.setMessage(t.toString())
			.setPositiveButton(R.string.ok, null)
			.show();
	}
	
	private Thread.UncaughtExceptionHandler onBlooey=
		new Thread.UncaughtExceptionHandler() {
		public void uncaughtException(Thread thread, Throwable ex) {
			Log.e(TAG, "Uncaught exception", ex);
			goBlooey(ex);
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onTerminate() {
		objects.clear();
		super.onTerminate();
	}
	
	public ThumbnailBus getBus() {
		return(bus);
	}
	
	public SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getCache() {
		return(cache);
	}
}
