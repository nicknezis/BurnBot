package org.nicknack.dailyburn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
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
import org.nicknack.dailyburn.domain.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.thoughtworks.xstream.XStream;

public class Main extends Activity {
	
	private static final String DBURN_TAG = "dailyburndroid";
	
	OAuthConsumer consumer = new DefaultOAuthConsumer( 
			"1YHdpiXLKmueriS5v7oS2w", "7SgQOoMQ2SG5tRPdQvvMxIv9Y6BDeI1ABuLrey6k", 
			SignatureMethod.HMAC_SHA1);  
	  
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
        	Log.d(DBURN_TAG, "In Create");
        	loadProvider();
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

            XStream stream = new XStream();
            stream.alias("user", User.class);
            stream.aliasField("time-zone", User.class, "timeZone");
            stream.aliasField("uses-metric-weights", User.class, "usesMetricWeights");
            stream.aliasField("uses-metric-distances", User.class, "usesMetricDistances");
            stream.aliasField("cal-goals-met-in-past-week", User.class, "calGoalsMetInPastWeek");
            stream.aliasField("days-exercised-in-past-week", User.class, "daysExercisedInPastWeek");
            stream.aliasField("picture-url", User.class, "pictureUrl");
            stream.aliasField("calories-burned", User.class, "caloriesBurned");
            stream.aliasField("calories-consumed", User.class, "caloriesConsumed");
            stream.aliasField("body-weight", User.class, "bodyWeight");
            stream.aliasField("body-weight-goal", User.class, "bodyWeightGoal");
            stream.aliasField("created-at", User.class, "createdAt");
            
            User user = (User)stream.fromXML(connection.getInputStream());
            TextView tv = (TextView) findViewById(R.id.main_text);
            tv.setText("Username: " + user.getUsername() + ", Body Weight: " + user.getBodyWeight());
            
            //USE TO PRINT TO LogCat (Make a filter on dailyburndroid tag)
//            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line = null;
//            while((line = in.readLine()) != null) {
//              Log.d(DBURN_TAG,line);
//            }
//            
            //OLD WAY
//            Document doc = null;
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            doc = db.parse(connection.getInputStream());
//            
//            
//            String username = doc.getElementsByTagName("username").item(0).getFirstChild().getNodeValue();
//            String bodyWeight = doc.getElementsByTagName("body-weight").item(0).getFirstChild().getNodeValue();
//            TextView tv = (TextView) findViewById(R.id.main_text);
//            tv.setText("Username: " + username + ", Body Weight: " + bodyWeight);
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
    		Log.d(DBURN_TAG,uri.toString());
    	    String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
    	    try {
    	    	loadProvider();
    	    	// this will populate token and token_secret in consumer
    	    	Log.d(DBURN_TAG,"Retrieving Access Token");
				provider.retrieveAccessToken(verifier);
				persistProvider();
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
		    consumer = this.provider.getConsumer();
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
}