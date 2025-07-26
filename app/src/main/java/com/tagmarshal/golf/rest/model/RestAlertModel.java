package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RestAlertModel implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("image")
    private String image;

    @SerializedName("message")
    private String message;

    @SerializedName("sound")
    private boolean sound;

    private Boolean visible = true;

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public boolean isSound() {
        return sound;
    }
}
