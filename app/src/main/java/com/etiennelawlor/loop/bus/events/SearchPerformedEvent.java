package com.etiennelawlor.loop.bus.events;

/**
 * Created by etiennelawlor on 9/20/15.
 */
public class SearchPerformedEvent {

    // region Member Variables
    private String query;
    // endregion

    // region Constructors
    public SearchPerformedEvent(String query){
        this.query = query;
    }
    // endregion

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
