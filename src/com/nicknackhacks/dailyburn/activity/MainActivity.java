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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.flurry.android.FlurryAgent;
import com.nicknackhacks.dailyburn.BurnBot;
import com.nicknackhacks.dailyburn.R;
import com.nicknackhacks.dailyburn.adapters.FoodLogEntryAdapter;
import com.nicknackhacks.dailyburn.api.FoodDao;
import com.nicknackhacks.dailyburn.model.FoodLogEntry;
import com.nicknackhacks.dailyburn.model.MealName;

public class MainActivity extends Activity {

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
		BurnBot.LogD("In Create");
		pref = this.getSharedPreferences("dbdroid", 0);
		isAuthenticated = pref.getBoolean("isAuthed", false);
		consumer = ((BurnBot) getApplication()).getOAuthConsumer();
		provider = new DefaultOAuthProvider(consumer,
				"http://dailyburn.com/api/oauth/request_token",
				"http://dailyburn.com/api/oauth/access_token",
				"http://dailyburn.com/api/oauth/authorize");

		if (!pref.getAll().containsKey(BurnBot.DOFLURRY)) {
			OnClickListener listener = new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					final Editor editor = pref.edit();
					switch (which) {
					case 0:
						editor.putBoolean(BurnBot.DOFLURRY, true);
						BurnBot.DoFlurry = true;
						break;
					case 1:
						editor.putBoolean(BurnBot.DOFLURRY, false);
						BurnBot.DoFlurry = false;
						break;
					}
					editor.commit();
				}
			};
			
			AlertDialog flurryAlert = new AlertDialog.Builder(this)
					.setTitle(R.string.flurry_dialog_title)
					.setMessage(R.string.flurry_dialog_message)
					.setPositiveButton(R.string.enable, listener)
					.setNegativeButton(R.string.disable, listener)
					.setCancelable(false).create();
			flurryAlert.show();
		}
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
	protected void onStart() {
		super.onStart();
		if(BurnBot.DoFlurry)
			FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(BurnBot.DoFlurry)
			FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Uri uri = this.getIntent().getData();
		if (uri != null
				&& uri.toString().startsWith(getString(R.string.callbackUrl))) {
			BurnBot.LogD( uri.toString());
			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			try {
				loadProvider();
				// this will populate token and token_secret in consumer
				BurnBot.LogD( "Retrieving Access Token");
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
				BurnBot.LogE(e.getMessage(), e);
			} catch (OAuthNotAuthorizedException e) {
				BurnBot.LogE(e.getMessage(), e);
			} catch (OAuthExpectationFailedException e) {
				BurnBot.LogE(e.getMessage(), e);
			} catch (OAuthCommunicationException e) {
				BurnBot.LogE(e.getMessage(), e);
			}
		}
		findViewById(R.id.main_button_food).setEnabled(isAuthenticated);
		findViewById(R.id.main_button_user).setEnabled(isAuthenticated);
		//findViewById(R.id.main_button_diet).setEnabled(isAuthenticated);
		findViewById(R.id.main_button_metrics).setEnabled(isAuthenticated);
	}

	protected void loadProvider() {
		BurnBot.LogD( "Loading provider");
		try {
			FileInputStream fin = this.openFileInput("provider.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			this.provider = (DefaultOAuthProvider) ois.readObject();
			ois.close();
			consumer = (CommonsHttpOAuthConsumer) this.provider.getConsumer();
		} catch (FileNotFoundException e) {
			BurnBot.LogD( e.getMessage(), e);
		} catch (StreamCorruptedException e) {
			BurnBot.LogD( e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogD( e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			BurnBot.LogD( e.getMessage(), e);
		}
		BurnBot.LogD( "Loaded Provider");
	}

	protected void persistProvider() {
		BurnBot.LogD( "Provider Persisting");
		try {
			FileOutputStream fout = this.openFileOutput("provider.dat",
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this.provider);
			oos.close();
		} catch (FileNotFoundException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (IOException e) {
			BurnBot.LogE(e.getMessage(), e);
		}
		BurnBot.LogD( "Provider Persisted");
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
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthNotAuthorizedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthExpectationFailedException e) {
			BurnBot.LogE(e.getMessage(), e);
		} catch (OAuthCommunicationException e) {
			BurnBot.LogE(e.getMessage(), e);
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