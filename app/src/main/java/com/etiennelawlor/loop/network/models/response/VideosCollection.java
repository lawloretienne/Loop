package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideosCollection {

    // region Fields
    @SerializedName("total")
    private Integer total;
    @SerializedName("page")
    private Integer page;
    @SerializedName("per_page")
    private Integer perPage;
    @SerializedName("paging")
    private Paging paging;
    @SerializedName("data")
    private List<Video> videos;
    // endregion

    // region Getters
    public Integer getTotal() {
        return total == null ? -1 : total;
    }

    public Integer getPage() {
        return page == null ? -1 : page;
    }

    public Integer getPerPage() {
        return perPage == null ? -1 : perPage;
    }

    public Paging getPaging() {
        return paging;
    }

    public List<Video> getVideos() {
        return videos;
    }
    // endregion

    // region Setters
    public void setTotal(Integer total) {
        this.total = total;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
    // endregion
}
