package com.nicknackhacks.dailyburn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import android.app.Application;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.commonsware.cwac.cache.CacheBase.DiskCachePolicy;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.google.ads.AdSenseSpec;
import com.google.ads.AdSenseSpec.AdType;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.MealName;
import com.nicknackhacks.dailyburn.provider.BurnBotContract;

public class BurnBot extends Application {

	public static final String DOFLURRY = "DoFlurry";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
	public static boolean DoFlurry = false;
	private ThumbnailBus bus=new ThumbnailBus();
	private DiskCachePolicy policy=new DiskCachePolicy() {
		public boolean eject(File file) {
			return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
		}
	};
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache=null;
	public HashMap<Long, WeakReference<Object> > objects = new HashMap<Long, WeakReference<Object> >();
	private HttpClient httpClient;
	private CommonsHttpOAuthConsumer oAuthConsumer;
	List<MealName> mealNames;
	Map<Integer, String> mealNameMap;
	
	
	public BurnBot() {
		super();
		//Thread.setDefaultUncaughtExceptionHandler(onBlooey);
	}
	
	
//	public void goBlooey(Throwable t) {
//		AlertDialog.Builder builder=new AlertDialog.Builder(this);
//		
//		builder
//			.setTitle(R.string.exception)
//			.setMessage(t.toString())
//			.setPositiveButton(R.string.ok, null)
//			.show();
//	}
//	
//	private Thread.UncaughtExceptionHandler onBlooey=
//		new Thread.UncaughtExceptionHandler() {
//		public void uncaughtException(Thread thread, Throwable ex) {
//			Log.e(TAG, "Uncaught exception", ex);
//			goBlooey(ex);
//		}
//	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//		SharedPreferences pref = this.getSharedPreferences("dbdroid", 0);
		String token = pref.getString("token", null);
		String secret = pref.getString("secret", null);
		oAuthConsumer = new CommonsHttpOAuthConsumer(
				getString(R.string.consumer_key),
				getString(R.string.consumer_secret), SignatureMethod.HMAC_SHA1);
		oAuthConsumer.setTokenWithSecret(token, secret);
		
		cache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(getCacheDir(), policy, 101, bus);
//		AdManager.setTestDevices( new String[] {
//				AdManager.TEST_EMULATOR,
//				"392AB5CC52B8A2D22CEC1606EF614FB9",
//				} );
	}
	
	public static AdSenseSpec getAdSpec() {
		String CLIENT_ID = "ca-mb-app-pub-1372885313238698";
		String COMPANY_NAME = "Nick Nack Hacks";
		String APP_NAME = "BurnBot";
		String KEYWORDS = "nutrition, health, exercise ";
		AdSenseSpec adSenseSpec = new AdSenseSpec(CLIENT_ID)
			.setCompanyName(COMPANY_NAME)
			.setAppName(APP_NAME)
			.setKeywords(KEYWORDS)
//			.setChannel(CHANNEL_ID)
			.setAdTestEnabled(false)
	    	.setAdType(AdType.TEXT_IMAGE);
		
		return adSenseSpec;
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
	
    /**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private static String buildUserAgent(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName
                    + " (" + info.versionCode + ") (gzip)";
        } catch (NameNotFoundException e) {
            return null;
        }
    }

	private HttpClient initializeHttpClient() {
		HttpParams parameters = new BasicHttpParams();
		
		// Turn off stale checking.  Our connections break all the time anyway,
	    // and it's not worth it to pay the penalty of checking every time.
	    HttpConnectionParams.setStaleCheckingEnabled(parameters, false);
	    
		 // Default connection and socket timeout of 20 seconds.  Tweak to taste.
	    HttpConnectionParams.setConnectionTimeout(parameters, 20 * 1000);
	    HttpConnectionParams.setSoTimeout(parameters, 20 * 1000);
	    HttpConnectionParams.setSocketBufferSize(parameters, 8192);
	    
	    HttpProtocolParams.setUserAgent(parameters, buildUserAgent(getApplicationContext()));
	        
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory
				.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		schemeRegistry.register(new Scheme("http",
		        PlainSocketFactory.getSocketFactory(), 80));

		ClientConnectionManager manager = new ThreadSafeClientConnManager(
				parameters, schemeRegistry);
		final DefaultHttpClient client = new DefaultHttpClient(manager, parameters);
		
		client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                // Add header to accept gzip content
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                    LogHelper.LogD("Adding Gzip Header");
                }
            }
        });

        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                // Inflate any responses compressed with gzip
                final HttpEntity entity = response.getEntity();
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            LogHelper.LogD("Inflated Gzip");
                            break;
                        }
                    }
                }
            }
        });
        
		return client;
	}

	/**
     * Simple {@link HttpEntityWrapper} that inflates the wrapped
     * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
     */
    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }
    
	public void loadMealNames(boolean refresh) {
		if (mealNameMap == null || refresh == true) {
			FoodDao foodDao = new FoodDao(this);
			mealNames = foodDao.getMealNames();
			mealNameMap = new HashMap<Integer, String>();
			for (MealName name : mealNames) {
				mealNameMap.put(name.getId(), name.getName());
			}
		}
	}
	
	public boolean retrieveAndStoreMealNames() {
//		if (mealNameMap == null || refresh == true) {
			FoodDao foodDao = new FoodDao(this);
			try {
				List<MealName> mealNames = foodDao.getMealNames();
				getContentResolver().applyBatch(
						BurnBotContract.CONTENT_AUTHORITY,
						foodDao.getMealNameOps(mealNames));
				mealNameMap = new HashMap<Integer, String>();
				for (MealName name : mealNames) {
					mealNameMap.put(name.getId(), name.getName());
				}
			} catch (RemoteException e) {
				LogHelper.LogE("Binder error.", e);
				return false;
			} catch (OperationApplicationException e) {
				LogHelper.LogE("ContentProviderOperation error.",e);
				return false;
			} catch (Exception e) {
				LogHelper.LogE("Exception retrieving and storing meal names", e);
				return false;
			}
			return true;
//		}
	}

	public Map<Integer, String> getMealNameMap() {
		loadMealNames(false);
		return mealNameMap;
	}

	
	//
//	public List<MealName> getMealNames() {
//		if(mealNames == null) {
//			loadMealNames(false);
//		}
//		return mealNames;
//	}
}
