package org.nicknack.dailyburn;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
	
	CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(  
			"1YHdpiXLKmueriS5v7oS2w", "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k"
, SignatureMethod.HMAC_SHA1);  
	  
	OAuthProvider provider = new DefaultOAuthProvider(consumer,  
	        "http://dailyburn.com/api/oauth/request_token", 
	        "http://dailyburn.com/api/oauth/access_token",  
	        "http://dailyburn.com/api/oauth/authorize");  
	HttpClient client = new DefaultHttpClient();  
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        }
}