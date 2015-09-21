package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Paging {
    // region Member Variables
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
        if (TextUtils.isEmpty(next))
            return "";
        else
            return next;
    }

    public String getPrevious() {
        if (TextUtils.isEmpty(previous))
            return "";
        else
            return previous;
    }

    public String getFirst() {
        if (TextUtils.isEmpty(first))
            return "";
        else
            return first;
    }

    public String getLast() {
        if (TextUtils.isEmpty(last))
            return "";
        else
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