package com.etiennelawlor.loop.network.models.request;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class CommentPost {

    // region Fields
    @SerializedName("text")
    String text;
    // endregion

    // region Getters

    public String getText() {
        return text;
    }

    // endregion

    // region Setters
    public void setText(String text) {
        this.text = text;
    }
    // endregion

}

