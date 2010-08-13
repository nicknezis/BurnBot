package com.nicknackhacks.dailyburn.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class BurnBotContract {

	interface UserColumns {
		String USER_ID = "UserId";
		String USER_TIMEZONE = "TimeZone";
		String USER_NAME = "Username";
		String USER_METRIC_WEIGHTS = "UsesMetricWeights";
		String USER_METRIC_DISTANCE = "UsesMetricDistances";
		String USER_CAL_GOALS_MET = "CalGoalsMetInPastWeek";
		String USER_DAYS_EXERCISED = "DaysExercisedInPastWeek";
		String USER_PICTURE_URL = "PictureUrl";
		String USER_URL = "Url";
		String USER_CAL_BURNED = "CaloriesBurned";
		String USER_CAL_CONSUMED = "CaloriesConsumed";
		String USER_BODY_WEIGHT = "BodyWeight";
		String USER_BODY_WEIGHT_GOAL = "BodyWeightGoal";
		String USER_PRO = "Pro";
		String USER_CREATED_AT = "CreatedAt";
		String USER_DYN_DIET_GOALS = "DynamicDietGoals";
	}
	
	interface FoodColumns {
		String FOOD_ID = "FoodId";
		String FOOD_NAME = "FoodName";
		String FOOD_BRAND = "Brand";
		String FOOD_CALORIES = "Calories";
		String FOOD_PROTEIN = "Protein";
		String FOOD_SERVING_SIZE = "ServingSize";
		String FOOD_TOTAL_CARBS = "TotalCarbs";
		String FOOD_TOTAL_FAT = "TotalFat";
		String FOOD_USER_ID = "UserId";
		String FOOD_THUMB_URL = "ThumbUrl";
		String FOOD_USDA = "USDA";
	}

	public static final String CONTENT_AUTHORITY = "com.nicknackhacks.dailyburn";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
    private static final String PATH_USER = "user";
    private static final String PATH_FOOD = "foods";
    private static final String PATH_FAV = "favorites";

	public static class UserContract implements UserColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

//        public static final String CONTENT_TYPE =
//                "vnd.android.cursor.dir/vnd.burnbot.user";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.burnbot.user";

        /** Build {@link Uri} for requested {@link #USER_ID}. */
		public static Uri buildUserUri(String userId) {
			return CONTENT_URI.buildUpon().appendPath(userId).build();
		}
	}
	
	public static class FoodContract implements FoodColumns, BaseColumns {
		public static final Uri CONTENT_URI =
			BASE_CONTENT_URI.buildUpon().appendPath(PATH_FOOD).build();
		
		public static final Uri FAVORITES_URI = 
			CONTENT_URI.buildUpon().appendPath(PATH_FAV).build();
		
		public static final String CONTENT_TYPE = 
			"vnd.android.cursor.dir/vnd.burnbot.food";
		public static final String CONTENT_ITEM_TYPE = 
			"vnd.android.cursor.item/vnd.burnbot.food";
		
		/** Build {@link Uri} for requested {@link #FOOD_ID}. */
		public static Uri buildFoodUri(String foodId) {
			return CONTENT_URI.buildUpon().appendPath(foodId).build();
		}
				
		 /** Read {@link #FOOD_ID} from {@link Foods} {@link Uri}. */
        public static String getFoodId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
	}
}
