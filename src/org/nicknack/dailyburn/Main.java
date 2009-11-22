package org.nicknack.dailyburn;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Main extends Activity {
	
	private static final String DBURN_TAG = "Dailyburn";
	
	CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer( 
			"1YHdpiXLKmueriS5v7oS2w", "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k", 
			SignatureMethod.HMAC_SHA1);  
	  
	OAuthProvider provider = new DefaultOAuthProvider(consumer, 
			//getString(R.string.requestTokenEndpointUrl),
	        "http://dailyburn.com/api/oauth/request_token",
			//getString(R.string.accessTokenEndpointUrl),
	        "http://dailyburn.com/api/oauth/access_token",
			//getString(R.string.authorizationWebsiteUrl));
	        "http://dailyburn.com/api/oauth/authorize");  
	HttpClient client = new DefaultHttpClient();  
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        }
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.authenticate: 
        	String authUrl;
			try {
				//authUrl = provider.retrieveRequestToken("dailyburndroid://org.nicknack.dailyburndroid/");
				authUrl = provider.retrieveRequestToken(getString(R.string.callbackUrl));
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
        }
        return false;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();  
    	Uri uri = this.getIntent().getData();  
    	if (uri != null && uri.toString().startsWith(getString(R.string.callbackUrl))) {  
    	    String verifier = uri.getQueryParameter("oauth_token");  
    	    // this will populate token and token_secret in consumer  
    	    try {
				provider.retrieveAccessToken(verifier);
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			}  
    	}  
    }
}