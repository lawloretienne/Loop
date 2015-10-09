package com.etiennelawlor.loop.otto.events;

/**
 * Created by etiennelawlor on 9/20/15.
 */
public class ShowSearchSuggestionsEvent {

    // region Member Variables
    private String mQuery;
    // endregion

    // region Constructors
    public ShowSearchSuggestionsEvent(String query){
        mQuery = query;
    }
    // endregion

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        mQuery = query;
    }
}
