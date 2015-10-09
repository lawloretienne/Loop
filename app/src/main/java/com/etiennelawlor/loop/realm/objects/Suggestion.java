package com.etiennelawlor.loop.realm.objects;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by etiennelawlor on 10/7/15.
 */
public class Suggestion extends RealmObject {

    @PrimaryKey
    private String token;

    private Date timestamp;

    // region Getters
    public String getToken() { return token; }

    public Date getTimestamp() {
        return timestamp;
    }
    // endregion

    // region Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    // endregion
}
