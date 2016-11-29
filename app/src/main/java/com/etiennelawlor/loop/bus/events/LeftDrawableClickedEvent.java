package com.etiennelawlor.loop.bus.events;

/**
 * Created by etiennelawlor on 9/20/15.
 */
public class LeftDrawableClickedEvent {

    private Type type;

    public LeftDrawableClickedEvent(Type type){
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        BACK,
        MENU,
        SEARCH
    }
}
