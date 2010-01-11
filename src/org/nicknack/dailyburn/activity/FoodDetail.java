package org.nicknack.dailyburn.activity;

import org.nicknack.dailyburn.DailyBurnDroid;
import org.nicknack.dailyburn.R;
import org.nicknack.dailyburn.model.Food;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodDetail extends Activity {
	private DailyBurnDroid app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fooddetail);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Long selectedFoodKey = extras.getLong("selectedFood");
		app = (DailyBurnDroid) this.getApplication();
		Food detailFood = (Food) app.objects.get(selectedFoodKey).get();
		final TextView tv = (TextView) findViewById(R.id.food_name);
		tv.setText("Name: " + detailFood.getName());
		Long foodIconKey = extras.getLong("selectedFoodImage");
		final ImageView icon = (ImageView) findViewById(R.id.food_icon);
		Drawable foodImage = (Drawable) app.objects.get(foodIconKey).get();
		icon.setImageDrawable(foodImage);
		final WebView nutrition = (WebView) findViewById(R.id.nutrition);
		//TODO: Use OAuthConsumer to sign a HttpRequest and store returned data in the WebView
		//nutrition.loadUrl("https://dailyburn.com/api/foods/nutrition_label?id=" + detailFood.getId());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}
