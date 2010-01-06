package org.nicknack.dailyburn.api;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.basic.DefaultOAuthConsumer;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nicknack.dailyburn.model.Food;
import org.nicknack.dailyburn.model.Foods;

import android.util.Log;

import com.thoughtworks.xstream.XStream;

public class FoodDao {

	private final static String DBURN_TAG = "dailyburndroid";
	private DefaultHttpClient client;
	private DefaultOAuthConsumer consumer;
	private XStream xstream;
	
	public FoodDao(DefaultHttpClient client,
			DefaultOAuthConsumer consumer) {
		this.client = client;
		this.consumer = consumer;
		configureXStream();
	}

	private void configureXStream() {
        xstream = new XStream();
        xstream.alias("foods", Foods.class);
        xstream.addImplicitCollection(Foods.class, "foods");
        xstream.alias("food", Food.class);
        xstream.aliasField("serving-size", Food.class, "servingSize");
        xstream.aliasField("total-carbs", Food.class, "totalCarbs");
        xstream.aliasField("total-fat", Food.class, "totalFat");
        xstream.aliasField("user-id", Food.class, "userId");
        xstream.aliasField("thumb-url", Food.class, "thumbUrl");
	}

	public ArrayList<Food> search(String param) {
		
		Foods foods = null;
		try {
		String encodedParam = URLEncoder.encode(param,"UTF-8");
		URL url = new URL("https://dailyburn.com/api/foods/search.xml?input=" + encodedParam);
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
        
//      //USE TO PRINT TO LogCat (Make a filter on dailyburndroid tag)
//      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//      String line = null;
//      while((line = in.readLine()) != null) {
//        Log.d(DBURN_TAG,line);
//      }
        foods = (Foods)xstream.fromXML(connection.getInputStream());
        Log.d(this.DBURN_TAG,foods.foods.get(0).getName() + " " + foods.foods.get(0).getBrand());
		}
		catch(Exception e) {
			Log.d(DBURN_TAG,e.getMessage());
		}
		return foods.foods;
	}

}
