package com.etiennelawlor.loop.analytics;

import java.util.HashMap;

/**
 * Created by etiennelawlor on 9/22/15.
 */
public class Event {

    // region Member Variables
    private String mName;
    private HashMap<String, Object> mMap;
    // endregion

    // region Constructors
    public Event(String name, HashMap<String, Object> map) {
        mName = name;
        mMap = map;
    }
    // endregion

    // region Getters
    public String getName(){
        return mName;
    }

    public HashMap<String, Object> getMap(){
        return mMap;
    }
    // endregion

}
