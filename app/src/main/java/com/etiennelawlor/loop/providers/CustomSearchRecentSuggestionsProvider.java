package com.etiennelawlor.loop.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by etiennelawlor on 9/23/15.
 */
public class CustomSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.lawloretienne.loop.providers.CustomSearchRecentSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public CustomSearchRecentSuggestionsProvider(){
        setupSuggestions(AUTHORITY, MODE);
    }
}
