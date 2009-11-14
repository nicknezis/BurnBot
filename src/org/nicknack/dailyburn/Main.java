package org.nicknack.dailyburn;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
//      OAuthConsumer consumer = new DefaultOAuthConsumer(
//      // the consumer key of this app (replace this with yours)
//      "iIlNngv1KdV6XzNYkoLA",
//      // the consumer secret of this app (replace this with yours)
//      "exQ94pBpLXFcyttvLoxU2nrktThrlsj580zjYzmoM",
//      SignatureMethod.HMAC_SHA1);
//
//OAuthProvider provider = new DefaultOAuthProvider(consumer,
//		"http://dailyburn.com/api/request_token",
//		"http://dailyburn.com/api/authorize",
//		"http://dailyburn.com/api/access_token");
//
///****************************************************
//* The following steps should only be performed ONCE
//***************************************************/
//
//// we do not support callbacks, thus pass OOB
//String authUrl = provider.retrieveRequestToken(OAuth.OUT_OF_BAND);
//
//// bring the user to authUrl, e.g. open a web browser and note the PIN code
//// ...         
//
//String pinCode = // ... you have to ask this from the user, or obtain it
//// from the callback if you didn't do an out of band request
//
//// user must have granted authorization at this point
//provider.retrieveAccessToken(pinCode);
//
//// store consumer.getToken() and consumer.getTokenSecret(),
//// for the current user, e.g. in a relational database
//// or a flat file
//// ...
//
///****************************************************
//* The following steps are performed everytime you
//* send a request accessing a resource on Twitter
//***************************************************/
//
//// if not yet done, load the token and token secret for
//// the current user and set them
//consumer.setTokenWithSecret(ACCESS_TOKEN, TOKEN_SECRET);
//
//// create a request that requires authentication
//URL url = new URL("http://twitter.com/statuses/mentions.xml");
//HttpURLConnection request = (HttpURLConnection) url.openConnection();
//
//// sign the request
//consumer.sign(request);
//
//// send the request
//request.connect();
//
//// response status should be 200 OK
//int statusCode = request.getResponseCode();    
    }
}