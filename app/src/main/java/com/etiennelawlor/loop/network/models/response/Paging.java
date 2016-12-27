package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Paging {

    // region Fields
    @SerializedName("next")
    private String next;
    @SerializedName("previous")
    private String previous;
    @SerializedName("first")
    private String first;
    @SerializedName("last")
    private String last;
    // endregion

    // region Getters

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    // endregion

    // region Setters
    public void setNext(String next) {
        this.next = next;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public void setLast(String last) {
        this.last = last;
    }
    // endregion
}