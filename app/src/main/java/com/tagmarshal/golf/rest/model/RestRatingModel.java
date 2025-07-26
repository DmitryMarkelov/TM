package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

public class RestRatingModel {

    @SerializedName("position")
    private String position;

    @SerializedName("stars")
    private int stars;

    @SerializedName("comment")
    private String comment;

    public RestRatingModel(
            String position,
            int stars,
            String comment
    ) {
        this.position = position;
        this.stars = stars;
        this.comment = comment;
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public int getStars() {
        return stars;
    }
    public void setStars(int stars) {
        this.stars = stars;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
}
