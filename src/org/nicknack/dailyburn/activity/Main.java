package org.nicknack.dailyburn.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.api.UserDao;
import org.nicknack.dailyburn.model.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Main extends Activity {
	
	private static final String DBURN_TAG = "dailyburndroid";
	
	CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer( 
			"1YHdpiXLKmueriS5v7oS2w", "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k", 
			//getString(R.string.consumer_key),getString(R.string.consumer_secret),
			SignatureMethod.HMAC_SHA1);  
	  
	DefaultOAuthProvider provider = new DefaultOAuthProvider(consumer, 
	        "http://dailyburn.com/api/oauth/request_token",
	        "http://dailyburn.com/api/oauth/access_token",
	        "http://dailyburn.com/api/oauth/authorize");  
	
	UserDao userDao;
	boolean isAuthenticated;
	private SharedPreferences pref;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //daoFactory = new DaoFactory(provider);
        //userDao = daoFactory.getUserDao();
        setContentView(R.layout.main);
        	Log.d(DBURN_TAG, "In Create");
        	pref = this.getSharedPreferences("dbdroid", 0);
        	isAuthenticated = pref.getBoolean("isAuthed", false);
        	String token = pref.getString("token", null);
        	String secret = pref.getString("secret", null);
        	consumer.setTokenWithSecret(token, secret);
        	userDao = new UserDao(new DefaultHttpClient(), consumer);
        	//userDao.setConsumer(consumer);
        	//loadProvider();
        }
        
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        menu.findItem(R.id.user_name_menu).setEnabled(isAuthenticated);
        menu.findItem(R.id.food_menu).setEnabled(isAuthenticated);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.authenticate_menu: 
        	String authUrl;
			try {
				authUrl = provider.retrieveRequestToken("dailyburndroid://org.nicknack.dailyburndroid/");
				persistProvider();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
			} catch (OAuthMessageSignerException e) {
				Log.d(DBURN_TAG, "OAuth: " + e.toString());
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				Log.d(DBURN_TAG, "OAuth: " + e.toString());
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				Log.d(DBURN_TAG, "OAuth: " + e.toString());
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				Log.d(DBURN_TAG, "OAuth: " + e.toString());
				e.printStackTrace();
			}  
            return true;
        case R.id.user_name_menu:
        	User user = userDao.getUserInfo();
            TextView tv = (TextView) findViewById(R.id.main_text);
            tv.setText("Username: " + user.getUsername() + ", Body Weight: " + user.getBodyWeight());
        	return true;
        case R.id.food_menu:
        	Intent intent = new Intent(this, FoodSearch.class);
        	startActivity(intent);
        	return true;
        }
        return false;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();  
    	Uri uri = this.getIntent().getData();  
    	if (uri != null && uri.toString().startsWith(getString(R.string.callbackUrl))) {
    		Log.d(DBURN_TAG,uri.toString());
    	    String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
    	    try {
    	    	loadProvider();
    	    	// this will populate token and token_secret in consumer
    	    	Log.d(DBURN_TAG,"Retrieving Access Token");
				provider.retrieveAccessToken(verifier);
				Editor editor = pref.edit();
				editor.putString("token", provider.getConsumer().getToken());
				editor.putString("secret", provider.getConsumer().getTokenSecret());
				isAuthenticated = true;
				editor.putBoolean("isAuthed", isAuthenticated);
				editor.commit();
				deleteProviderFile();				
				//persistProvider();
				//persistUserAccessToken("db");
			} catch (OAuthMessageSignerException e) {
				Log.d(DBURN_TAG, e.getMessage());
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				Log.d(DBURN_TAG, e.getMessage());
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				Log.d(DBURN_TAG, e.getMessage());
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				Log.d(DBURN_TAG, e.getMessage());
				e.printStackTrace();
			}  
    	}  
    }

    protected void loadProvider()
    {
    	Log.d(DBURN_TAG,"Loading provider");
    	try {
			FileInputStream fin = this.openFileInput("provider.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
		    this.provider = (DefaultOAuthProvider) ois.readObject();
		    ois.close();
		    consumer = (CommonsHttpOAuthConsumer) this.provider.getConsumer();
		    this.userDao.setConsumer(consumer);
		} catch (FileNotFoundException e) {
			Log.d(DBURN_TAG,e.getMessage());
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			Log.d(DBURN_TAG,e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(DBURN_TAG,e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.d(DBURN_TAG,e.getMessage());
			e.printStackTrace();
		}
		Log.d(DBURN_TAG,"Loaded Provider");
    }
    
    protected void persistProvider()
    {
    	Log.d(DBURN_TAG,"Provider Persisting");
    	try {
			FileOutputStream fout = this.openFileOutput("provider.dat", Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
		    //oos.writeObject(this.provider);
		    oos.writeObject(this.provider);
		    oos.close();
		} catch (FileNotFoundException e) {
			Log.d(DBURN_TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(DBURN_TAG, e.getMessage());
			e.printStackTrace();
		}
		Log.d(DBURN_TAG,"Provider Persisted");
    }
    
    protected void deleteProviderFile() {
    	this.deleteFile("provider.dat");
    }
}