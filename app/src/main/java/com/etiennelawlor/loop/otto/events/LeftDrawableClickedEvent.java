package com.etiennelawlor.loop.otto.events;

/**
 * Created by etiennelawlor on 9/20/15.
 */
public class LeftDrawableClickedEvent {

    private Type mType;

    public LeftDrawableClickedEvent(Type type){
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public enum Type {
        BACK,
        MENU,
        SEARCH
    }
}
