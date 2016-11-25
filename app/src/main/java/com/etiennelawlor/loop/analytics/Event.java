package com.etiennelawlor.loop.analytics;

import java.util.HashMap;

/**
 * Created by etiennelawlor on 9/22/15.
 */
public class Event {

    // region Member Variables
    private String name;
    private HashMap<String, Object> map;
    // endregion

    // region Constructors
    public Event(String name, HashMap<String, Object> map) {
        this.name = name;
        this.map = map;
    }
    // endregion

    // region Getters
    public String getName(){
        return name;
    }

    public HashMap<String, Object> getMap(){
        return map;
    }
    // endregion

}
