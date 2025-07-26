package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RestHoleDistanceModel implements Serializable {

    @SerializedName("center")
    private String center;

    @SerializedName("back")
    private String back;

    @SerializedName("front")
    private String front;

    @SerializedName("tee")
    private String tee;

    @SerializedName("mylocation")
    private String myLocation;

    public String getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(String myLocation) {
        this.myLocation = myLocation;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getTee() {
        return tee;
    }

    public void setTee(String tee) {
        this.tee = tee;
    }
}
