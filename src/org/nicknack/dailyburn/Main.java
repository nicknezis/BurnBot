package org.nicknack.dailyburn;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Main extends Activity {
	
	private static final String DBURN_TAG = "Dailyburn";
	
	static DefaultOAuthConsumer consumer = new DefaultOAuthConsumer( 
			"1YHdpiXLKmueriS5v7oS2w", "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k", 
			SignatureMethod.HMAC_SHA1);  
	  
	static OAuthProvider provider = new DefaultOAuthProvider(consumer, 
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
				authUrl = provider.retrieveRequestToken("dailyburndroid://org.nicknack.dailyburndroid/");
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
        case R.id.user_name:
        	try {
        	// create an HTTP request to a protected resource
            URL url = new URL("https://dailyburn.com/api/users/current.xml");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            if (connection instanceof HttpsURLConnection) {
            	((HttpsURLConnection) connection)
            	.setHostnameVerifier(new AllowAllHostnameVerifier());
            	}

            // sign the request (consumer is a Signpost DefaultOAuthConsumer)
            consumer.sign(connection);

            // send the request
            connection.connect();

            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(connection.getInputStream());
            
            String tvText = null;
            String username = null;
            String bodyWeight = null;

            
            username = doc.getElementsByTagName("username").item(0).getFirstChild().getNodeValue();
            tvText = "Username: " + username + "\nBody Weight: ";
            if (doc.getElementsByTagName("body-weight").item(0).getFirstChild() != null) {
                bodyWeight = doc.getElementsByTagName("body-weight").item(0).getFirstChild().getNodeValue();
                tvText = tvText + bodyWeight;
            }
            TextView tv = (TextView) findViewById(R.id.main_text);
            tv.setText(tvText);
        	}
        	catch (Exception e) {
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
    	    String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
    	    try {
    	    	// this will populate token and token_secret in consumer  
				provider.retrieveAccessToken(verifier);
			} catch (OAuthMessageSignerException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}  
    	}  
    }
}