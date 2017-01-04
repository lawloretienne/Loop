package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoConfig {

    // region Fields
    @SerializedName("request")
    private Request request;
    // endregion

    // region Getters
    public Request getRequest() {
        return request;
    }

    public String getVideoUrl() {
        String videoUrl = "";

        if (request != null) {
            Files files = request.getFiles();
            if (files != null) {
                H264 h264 = files.getH264();
                HLS hls = files.getHls();
                VP6 vp6 = files.getVp6();
                List<ProgressiveData> progressiveDataList = files.getProgressive();

                String progressiveDataUrl = getProgressiveDataUrl(progressiveDataList);
                String h264VideoUrl = getH264VideoUrl(h264);
                String vp6VideoUrl = getVP6VideoUrl(vp6);
                String hlsVideoUrl = getHLSVideoUrl(hls);

                if (!TextUtils.isEmpty(progressiveDataUrl)) {
                    videoUrl = progressiveDataUrl;
                } else if (!TextUtils.isEmpty(h264VideoUrl)) {
                    videoUrl = h264VideoUrl;
                } else if (!TextUtils.isEmpty(vp6VideoUrl)) {
                    videoUrl = vp6VideoUrl;
                } else if (!TextUtils.isEmpty(hlsVideoUrl)) {
                    videoUrl = hlsVideoUrl;
                }
            }
        }

        return videoUrl;
    }

    private String getHLSVideoUrl(HLS hls) {
        String videoUrl = "";
        if (hls != null) {
            String url = hls.getUrl();
            videoUrl = url;
        }
        return videoUrl;
    }

    private String getProgressiveDataUrl(List<ProgressiveData> progressiveDataList){
        String progressiveDataUrl = "";

        String progessiveData270pUrl = "";
        String progessiveData360pUrl = "";
        String progessiveData1080pUrl = "";

        for(ProgressiveData progressiveData : progressiveDataList){
            String quality = progressiveData.getQuality();
            switch (quality) {
                case "1080p":
                    progessiveData1080pUrl = progressiveData.getUrl();
                    break;
                case "360p":
                    progessiveData360pUrl = progressiveData.getUrl();
                    break;
                case "270p":
                    progessiveData270pUrl = progressiveData.getUrl();
                    break;
            }
        }

        if(!TextUtils.isEmpty(progessiveData1080pUrl)){
            progressiveDataUrl = progessiveData1080pUrl;
        } else if(!TextUtils.isEmpty(progessiveData360pUrl)){
            progressiveDataUrl = progessiveData360pUrl;
        } else if(!TextUtils.isEmpty(progessiveData270pUrl)){
            progressiveDataUrl = progessiveData270pUrl;
        }

        return progressiveDataUrl;
    }

    private String getH264VideoUrl(H264 h264) {
        String videoUrl = "";
        if (h264 != null) {

            VideoFormat hdVideoFormat = h264.getHd();
            VideoFormat sdVideoFormat = h264.getSd();
            VideoFormat mobileVideoFormat = h264.getMobile();

            int width = -1;
            int height = -1;
            if (hdVideoFormat != null) {
                videoUrl = hdVideoFormat.getUrl();
                width = hdVideoFormat.getWidth();
                height = hdVideoFormat.getHeight();
            } else if (sdVideoFormat != null) {
                videoUrl = sdVideoFormat.getUrl();
                width = sdVideoFormat.getWidth();
                height = sdVideoFormat.getHeight();
            } else if (mobileVideoFormat != null) {
                videoUrl = mobileVideoFormat.getUrl();
                width = mobileVideoFormat.getWidth();
                height = mobileVideoFormat.getHeight();
            }

            Timber.d(String.format("mGetVideoConfigCallback : url - %s", videoUrl));
            Timber.d(String.format("mGetVideoConfigCallback : width - %d : height - %d", width, height));
        }

        return videoUrl;
    }

    private String getVP6VideoUrl(VP6 vp6) {
        String videoUrl = "";
        if (vp6 != null) {

            VideoFormat hdVideoFormat = vp6.getHd();
            VideoFormat sdVideoFormat = vp6.getSd();
            VideoFormat mobileVideoFormat = vp6.getMobile();

            int width = -1;
            int height = -1;
            if (hdVideoFormat != null) {
                videoUrl = hdVideoFormat.getUrl();
                width = hdVideoFormat.getWidth();
                height = hdVideoFormat.getHeight();
            } else if (sdVideoFormat != null) {
                videoUrl = sdVideoFormat.getUrl();
                width = sdVideoFormat.getWidth();
                height = sdVideoFormat.getHeight();
            } else if (mobileVideoFormat != null) {
                videoUrl = mobileVideoFormat.getUrl();
                width = mobileVideoFormat.getWidth();
                height = mobileVideoFormat.getHeight();
            }

            Timber.d(String.format("mGetVideoConfigCallback : url - %s", videoUrl));
            Timber.d(String.format("mGetVideoConfigCallback : width - %d : height - %d", width, height));
        }

        return videoUrl;
    }
    // endregion

    // region Setters
    public void setRequest(Request request) {
        this.request = request;
    }
    // endregion
}
