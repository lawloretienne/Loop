package com.etiennelawlor.loop.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Request {

    // region Member Variables
    @SerializedName("files")
    private Files files;
    // endregion

    // region Getters
    public Files getFiles() {
        return files;
    }
    // endregion

    // region Setters
    public void setFiles(Files files) {
        this.files = files;
    }
    // endregion
}
