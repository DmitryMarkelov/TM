package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AdvertisementImage implements Serializable {

    @SerializedName("5i")
    private String fiveInch;

    @SerializedName("7i")
    private String sevenInch;

    @SerializedName("8i")
    private String eightInch;

    @SerializedName("10i")
    private String tenInch;


    public String getFiveInch() {
        return fiveInch;
    }

    public String getSevenInch() {
        return sevenInch;
    }

    public String getTenInch() {
        return tenInch;
    }

    public String getEightInch() {
        return eightInch;
    }
}
