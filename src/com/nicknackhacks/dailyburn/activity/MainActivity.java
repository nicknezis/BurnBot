package com.nicknackhacks.dailyburn.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oauth.signpost.OAuth;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodLogEntryAdapter;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;
import com.nicknackhacks.dailyburn.model.MealName;

public class MainActivity extends TabActivity {

	CommonsHttpOAuthConsumer consumer;
	DefaultOAuthProvider provider;

	boolean isAuthenticated;
	private SharedPreferences pref;
	MealNamesAsyncTask mealNameTask = new MealNamesAsyncTask();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if(Log.isLoggable(BurnBot.TAG, Log.DEBUG))
			Log.d(BurnBot.TAG, "In Create");
		pref = this.getSharedPreferences("dbdroid", 0);
		isAuthenticated = pref.getBoolean("isAuthed", false);
		consumer = ((BurnBot) getApplication()).getOAuthConsumer();
		provider = new DefaultOAuthProvider(consumer,
				"http://dailyburn.com/api/oauth/request_token",
				"http://dailyburn.com/api/oauth/access_token",
				"http://dailyburn.com/api/oauth/authorize");
		
		if(!isAuthenticated) {
			// Show a dialog asking the user to authenticate.
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			
			builder
				.setTitle("Please Log in")
				.setMessage("Please authenticate with the DailyBurn website.")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						startAuthentication();
					}
				});
			builder.show();
		} else {
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			
			builder
				.setTitle("Authenticated")
				.setMessage("You are authenticated.")
				.setPositiveButton(R.string.ok, null);
			builder.show();
		}
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		Intent intent;
		TabHost.TabSpec spec;
		
		// Create the Body Tab
		intent = new Intent().setClass(this, BodyEntryAddActivity.class);
		spec = tabHost.newTabSpec("bodyentry")
			.setIndicator(getString(R.string.tab_body), res.getDrawable(R.drawable.ic_tab_body))
			.setContent(intent);
		tabHost.addTab(spec);
		
		// Create the Food Tab
		intent = new Intent().setClass(this, FoodSearchActivity.class);
		spec = tabHost.newTabSpec("foodsearch")
			.setIndicator(getString(R.string.tab_food), res.getDrawable(R.drawable.ic_tab_food))
			.setContent(intent);
		tabHost.addTab(spec);
		
		// Create the Workout Tab
		intent = new Intent().setClass(this, FoodSearchActivity.class);
		spec = tabHost.newTabSpec("foodsearch")
			.setIndicator(getString(R.string.tab_workout), res.getDrawable(R.drawable.ic_tab_workout))
			.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
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
			startAuthentication();
			return true;
		case R.id.user_name_menu:
			startUserActivity();
			return true;
		case R.id.food_menu:
			startFoodsActivity();
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Uri uri = this.getIntent().getData();
		if (uri != null
				&& uri.toString().startsWith(getString(R.string.callbackUrl))) {
			Log.d(BurnBot.TAG, uri.toString());
			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			try {
				loadProvider();
				// this will populate token and token_secret in consumer
				Log.d(BurnBot.TAG, "Retrieving Access Token");
				provider.retrieveAccessToken(verifier);
				Editor editor = pref.edit();
				editor.putString("token", provider.getConsumer().getToken());
				editor.putString("secret", provider.getConsumer()
						.getTokenSecret());
				isAuthenticated = true;
				editor.putBoolean("isAuthed", isAuthenticated);
				editor.commit();
				BurnBot app = (BurnBot) getApplication();
				app.setOAuthConsumer(consumer);
				FoodDao foodDao = new FoodDao(app);
				mealNameTask.execute(foodDao);
				deleteProviderFile();
			} catch (OAuthMessageSignerException e) {
				Log.e(BurnBot.TAG, e.getMessage());
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				Log.e(BurnBot.TAG, e.getMessage());
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				Log.e(BurnBot.TAG, e.getMessage());
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				Log.e(BurnBot.TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		//findViewById(R.id.main_button_food).setEnabled(isAuthenticated);
		//findViewById(R.id.main_button_user).setEnabled(isAuthenticated);
		//findViewById(R.id.main_button_diet).setEnabled(isAuthenticated);
		//findViewById(R.id.main_button_metrics).setEnabled(isAuthenticated);
	}

	protected void loadProvider() {
		Log.d(BurnBot.TAG, "Loading provider");
		try {
			FileInputStream fin = this.openFileInput("provider.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			this.provider = (DefaultOAuthProvider) ois.readObject();
			ois.close();
			consumer = (CommonsHttpOAuthConsumer) this.provider.getConsumer();
		} catch (FileNotFoundException e) {
			Log.d(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			Log.d(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.d(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		}
		Log.d(BurnBot.TAG, "Loaded Provider");
	}

	protected void persistProvider() {
		Log.d(BurnBot.TAG, "Provider Persisting");
		try {
			FileOutputStream fout = this.openFileOutput("provider.dat",
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this.provider);
			oos.close();
		} catch (FileNotFoundException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(BurnBot.TAG, e.getMessage());
			e.printStackTrace();
		}
		Log.d(BurnBot.TAG, "Provider Persisted");
	}

	protected void deleteProviderFile() {
		this.deleteFile("provider.dat");
	}

	private void startAuthentication() {
		String authUrl;
		try {
			authUrl = provider
					.retrieveRequestToken(getString(R.string.callbackUrl));
			persistProvider();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
		} catch (OAuthMessageSignerException e) {
			Log.e(BurnBot.TAG, "OAuth: " + e.toString());
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			Log.e(BurnBot.TAG, "OAuth: " + e.toString());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			Log.e(BurnBot.TAG, "OAuth: " + e.toString());
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			Log.e(BurnBot.TAG, "OAuth: " + e.toString());
			e.printStackTrace();
		}
	}

	private void startUserActivity() {
		Intent intent = new Intent(this, UserActivity.class);
		startActivity(intent);
	}

	private void startFoodsActivity() {
		Intent intent = new Intent(this, FoodSearchActivity.class);
		startActivity(intent);
	}
	
	private void startDietActivity() {
		Intent intent = new Intent(this, DietGoalsActivity.class);
		startActivity(intent);
	}
	
	private void startMetricsActivity() {
		Intent intent = new Intent(this, BodyMetricsListActivity.class);
		startActivity(intent);
	}

	public void onClickFoodButton(View v) {
		startFoodsActivity();
	}

	public void onClickUserButton(View v) {
		startUserActivity();
	}

	public void onClickAuthButton(View v) {
		startAuthentication();
	}
	
	public void onClickDietButton(View v) {
		startDietActivity();
	}
	
	public void onClickMetricsButton(View v) {
		startMetricsActivity();
	}
	
	private class MealNamesAsyncTask extends AsyncTask<FoodDao, Void, 	Map<Integer,String> > {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			progressDialog = ProgressDialog.show(FoodLogEntriesActivity.this,
//					"Please wait...", "Retrieving data ...", true);
		}

		@Override
		protected Map<Integer, String> doInBackground(FoodDao... foodDao) {
			Map<Integer, String> mealNameMap = new HashMap<Integer, String>();
				List<MealName> mealNames = foodDao[0].getMealNames();
				mealNameMap = new HashMap<Integer, String>();
				for (MealName name : mealNames) {
					mealNameMap.put(name.getId(), name.getName());
				}
				return mealNameMap;
		}

		@Override
		protected void onPostExecute(Map<Integer, String> result) {
			super.onPostExecute(result);
			BurnBot app = (BurnBot) getApplication();
			if(result != null)
				app.setMealNameMap(result);
		}
	}

}