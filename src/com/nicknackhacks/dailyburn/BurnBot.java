package com.nicknackhacks.dailyburn;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.admob.android.ads.AdManager;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

public class BurnBot extends Application {

	public static String TAG = "BurnBot";
	private ThumbnailBus bus=new ThumbnailBus();
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache=
							new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(null, null, 101, bus);
	public HashMap<Long, WeakReference<Object> > objects = new HashMap<Long, WeakReference<Object> >();
	private HttpClient httpClient;
	private CommonsHttpOAuthConsumer oAuthConsumer;
	Map<Integer, String> mealNameMap;
	
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
		SharedPreferences pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		oAuthConsumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		oAuthConsumer.setTokenWithSecret(token, secret);
		
//		AdManager.setTestDevices( new String[] {
//				AdManager.TEST_EMULATOR,
//				"392AB5CC52B8A2D22CEC1606EF614FB9",
//				} );
	}
	
	@Override
	public void onTerminate() {
		httpClient.getConnectionManager().shutdown();
		objects.clear();
		super.onTerminate();
	}
	
	public ThumbnailBus getBus() {
		return(bus);
	}
	
	public SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getCache() {
		return(cache);
	}

	public CommonsHttpOAuthConsumer getOAuthConsumer() {
		return oAuthConsumer;
	}

	public void setOAuthConsumer(CommonsHttpOAuthConsumer oAuthConsumer) {
		this.oAuthConsumer = oAuthConsumer;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public HttpClient getHttpClient() {
		if(httpClient == null) {
			httpClient = initializeHttpClient();
		}
		return httpClient;
	}
	
	private HttpClient initializeHttpClient() {
		HttpParams parameters = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(parameters, 5000);
		HttpConnectionParams.setConnectionTimeout(parameters, 5000);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory
				.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
//		ClientConnectionManager manager = new SingleClientConnManager(
		ClientConnectionManager manager = new ThreadSafeClientConnManager(
				parameters, schemeRegistry);
		return new DefaultHttpClient(manager, parameters);
	}

	public Map<Integer, String> getMealNameMap() {
		return mealNameMap;
	}

	public void setMealNameMap(Map<Integer, String> mealNameMap) {
		this.mealNameMap = mealNameMap;
	}
}
