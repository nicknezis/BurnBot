package com.nicknackhacks.dailyburn.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DailyBurnContract {

	interface UserColumns {
		String USER_ID = "id";
		String USER_TIMEZONE = "time-zone";
		String USER_NAME = "username";
		String USER_METRIC_WEIGHTS = "uses-metric-weights";
	}
//		int id; //  <id type="integer">123</id>
//		String timeZone; //<time-zone>Mountain Time (US &amp; Canada)</time-zone>
//		String username; //<username>StephenB</username>
//		boolean usesMetricWeights; //<uses-metric-weights type="boolean">false</uses-metric-weights>
//		boolean usesMetricDistances; //<uses-metric-distances type="boolean">false</uses-metric-distances>
//		int calGoalsMetInPastWeek; //<cal-goals-met-in-past-week type="integer">3</cal-goals-met-in-past-week>
//		int daysExercisedInPastWeek; //<days-exercised-in-past-week type="integer">1</days-exercised-in-past-week>
//		private String pictureUrl; //<picture-url>/images/fu/0003/2674/me_normal.png</picture-url>
//		private String url; //<url>/locker_room/stephenb</url>
//		private int caloriesBurned; //<calories-burned type="integer">0</calories-burned>
//		private int caloriesConsumed; //<calories-consumed type="integer">0</calories-consumed>
//		private float bodyWeight; //<body-weight type="decimal">186.0</body-weight>
//		private float bodyWeightGoal; //<body-weight-goal type="decimal">175.0</body-weight-goal>
//		private boolean pro; //<pro type="boolean">true</pro>
//		private String createdAt; //<created-at>2007-08-02 18:00:00 -0600</created-at>
//		private boolean dynamicDietGoals;

//    interface UserColumns {
//        /** Unique string identifying this block of time. */
//        String BLOCK_ID = "block_id";
//        /** Title describing this block of time. */
//        String BLOCK_TITLE = "block_title";
//        /** Time when this block starts. */
//        String BLOCK_START = "block_start";
//        /** Time when this block ends. */
//        String BLOCK_END = "block_end";
//        /** Type describing this block. */
//        String BLOCK_TYPE = "block_type";
//    }

	public static final String CONTENT_AUTHORITY = "com.nicknackhacks.dailyburn";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
    private static final String PATH_USER = "user";

	public static class UserContract implements UserColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

//        public static final String CONTENT_TYPE =
//                "vnd.android.cursor.dir/vnd.burnbot.user";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.burnbot.user";
	}

}
