package com.etiennelawlor.loop.network.models.response;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.utilities.DateUtility;
import com.google.gson.annotations.SerializedName;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Video implements Parcelable {

    // region Constants
    public static final String PATTERN = "yyyy-MM-dd'T'hh:mm:ssZ";
    // endregion

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("link")
    private String link;
    @SerializedName("duration")
    private int duration;
    @SerializedName("width")
    private int width;
    @SerializedName("language")
    private String language;
    @SerializedName("height")
    private int height;
    @SerializedName("embed")
    private Embed embed;
    @SerializedName("created_time")
    private String createdTime;
    @SerializedName("modified_time")
    private String modifiedTime;
    @SerializedName("content_rating")
    private List<String> contentRating;
//    @SerializedName("license")
//    private Object license;
    @SerializedName("pictures")
    private Pictures pictures;
    @SerializedName("tags")
    private List<Tag> tags;
    @SerializedName("stats")
    private Stats stats;
    @SerializedName("metadata")
    private Metadata metadata;
    @SerializedName("user")
    private User user;
//    @SerializedName("app")
//    private Object app;
    @SerializedName("status")
    private String status;
//    @SerializedName("embed_presets")
//    private Object embedPresets;
    // endregion

    // region Constructors
    public Video() {
    }

    protected Video(Parcel in) {
        this.uri = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.link = in.readString();
        this.duration = in.readInt();
        this.width = in.readInt();
        this.language = in.readString();
        this.height = in.readInt();
        this.embed = in.readParcelable(Embed.class.getClassLoader());
        this.createdTime = in.readString();
        this.modifiedTime = in.readString();
        this.contentRating = in.createStringArrayList();
        this.pictures = in.readParcelable(Pictures.class.getClassLoader());
        this.tags = in.createTypedArrayList(Tag.CREATOR);
        this.stats = in.readParcelable(Stats.class.getClassLoader());
        this.metadata = in.readParcelable(Metadata.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.status = in.readString();
    }
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public int getDuration() {
        return duration;
    }

    public int getWidth() {
        return width;
    }

    public String getLanguage() {
        return language;
    }

    public int getHeight() {
        return height;
    }

    public Embed getEmbed() {
        return embed;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public List<String> getContentRating() {
        return contentRating;
    }

    public Pictures getPictures() {
        return pictures;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Stats getStats() {
        return stats;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public long getId() {
        long id = -1L;
        String uri = getUri();
        if (!TextUtils.isEmpty(uri)) {
            String lastPathSegment = Uri.parse(uri).getLastPathSegment();
            id = Long.parseLong(lastPathSegment);
        }
        return id;
    }

    public String getThumbnailUrl(){
        String thumbnailUrl = "";
        if (pictures != null) {
            List<Size> sizes = pictures.getSizes();
            if (sizes != null && sizes.size() > 0) {
                Size size = sizes.get(sizes.size() - 1);
                if (size != null) {
                    thumbnailUrl = size.getLink();
                }
            }
        }
        return thumbnailUrl;
    }

    public String getFormattedDuration(){
        long minutes = duration / 60;
        long seconds = duration % 60;

        String formattedDuration;
        if (minutes == 0L) {
            if (seconds > 0L) {
                if (seconds < 10L)
                    formattedDuration = String.format("0:0%s", String.valueOf(seconds));
                else
                    formattedDuration = String.format("0:%s", String.valueOf(seconds));
            } else {
                formattedDuration = "0:00";
            }
        } else {
            if (seconds > 0L) {
                if (seconds < 10L)
                    formattedDuration = String.format("%s:0%s", String.valueOf(minutes), String.valueOf(seconds));
                else
                    formattedDuration = String.format("%s:%s", String.valueOf(minutes), String.valueOf(seconds));
            } else {
                formattedDuration = String.format("%s:00", String.valueOf(minutes));
            }
        }
        return formattedDuration;
    }

    public String getCaption(){
        String caption = "";
        int viewCount = 0;
        if (stats != null) {
            viewCount = stats.getPlays();
        }

        String formattedCreatedTime = DateUtility.getFormattedDateAndTime(DateUtility.getCalendar(createdTime, PATTERN), DateUtility.FORMAT_RELATIVE);

        if (viewCount > 0) {
            String formattedViewCount = formatViewCount(viewCount);
            if(!TextUtils.isEmpty(createdTime))
                caption = String.format("%s \u2022 %s", formattedViewCount, formattedCreatedTime);
            else
                caption = formattedViewCount;

        } else {
            caption = formattedCreatedTime;
        }
        return caption;
    }

    private String formatViewCount(int viewCount) {
        String formattedViewCount = "";

        if (viewCount < 1000000000 && viewCount >= 1000000) {
            formattedViewCount = String.format("%dM views", viewCount / 1000000);
        } else if (viewCount < 1000000 && viewCount >= 1000) {
            formattedViewCount = String.format("%dK views", viewCount / 1000);
        } else if (viewCount < 1000 && viewCount > 1) {
            formattedViewCount = String.format("%d views", viewCount);
        } else if (viewCount == 1) {
            formattedViewCount = String.format("%d view", viewCount);
        }

        return formattedViewCount;
    }

    public boolean isLiked(){
        boolean isLiked = false;
        if (metadata != null) {
            Interactions interactions = metadata.getInteractions();
            if (interactions != null) {
                Interaction likeInteraction = interactions.getLike();
                if (likeInteraction != null) {
                    if (likeInteraction.isAdded()) {
                        isLiked = true;
                    }
                }
            }
        }
        return isLiked;
    }

    public boolean isAddedToWatchLater(){
        boolean isAddedToWatchLater = false;
        if (metadata != null) {
            Interactions interactions = metadata.getInteractions();
            if (interactions != null) {
                Interaction watchLaterInteraction = interactions.getWatchlater();
                if (watchLaterInteraction != null) {
                    if (watchLaterInteraction.isAdded()) {
                        isAddedToWatchLater = true;
                    }
                }
            }
        }
        return isAddedToWatchLater;
    }

    public String getFormattedViewCount(){
        String formattedViewCount = "";

        int viewCount = 0;
        if (stats != null) {
            viewCount = stats.getPlays();
        }

        if (viewCount > 0) {
            formattedViewCount = NumberFormat.getNumberInstance(Locale.US).format(viewCount);
            if (viewCount > 1) {
                formattedViewCount = String.format("%s views", formattedViewCount);
            } else {
                formattedViewCount = String.format("%s view", formattedViewCount);
            }
        }
        return formattedViewCount;
    }

    public String getFormattedCreatedTime(){
        String formattedCreatedTime = DateUtility.getFormattedDateAndTime(DateUtility.getCalendar(createdTime, PATTERN), DateUtility.FORMAT_RELATIVE);
        if(!TextUtils.isEmpty(formattedCreatedTime))
            formattedCreatedTime = String.format("Uploaded %s", formattedCreatedTime);
        return formattedCreatedTime;
    }

    public List<String> getCanonicalTags(){
        ArrayList<String> canonicalTags = new ArrayList<>();
        if (tags != null && tags.size() > 0) {
            for (Tag tag : tags) {
                String canonicalTag = tag.getCanonical();
                if (canonicalTag.length() > 0) {
                    canonicalTags.add(canonicalTag);
                }
            }
        }
        return canonicalTags;
    }

    public String getFormattedDescription(){
        String formattedDescription = "";
        if(!TextUtils.isEmpty(description))
            formattedDescription = description.trim();
        return formattedDescription;
    }
    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setEmbed(Embed embed) {
        this.embed = embed;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setContentRating(List<String> contentRating) {
        this.contentRating = contentRating;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uri);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.link);
        dest.writeInt(this.duration);
        dest.writeInt(this.width);
        dest.writeString(this.language);
        dest.writeInt(this.height);
        dest.writeParcelable(this.embed, flags);
        dest.writeString(this.createdTime);
        dest.writeString(this.modifiedTime);
        dest.writeStringList(this.contentRating);
        dest.writeParcelable(this.pictures, flags);
        dest.writeTypedList(this.tags);
        dest.writeParcelable(this.stats, flags);
        dest.writeParcelable(this.metadata, flags);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.status);
    }
    // endregion

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}