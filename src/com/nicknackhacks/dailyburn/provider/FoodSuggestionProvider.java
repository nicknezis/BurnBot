package com.nicknackhacks.dailyburn.provider;

import android.content.SearchRecentSuggestionsProvider;

public class FoodSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.nicknackhacks.dailyburn.FoodSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public FoodSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}