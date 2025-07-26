package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

public class SnapToPlayModel {

    @SerializedName("music")
    private boolean music = false;

    @SerializedName("range_finding")
    private boolean rangeFinding;

    @SerializedName("fee")
    private double fee;

    @SerializedName("currency")
    private String currency;

    public boolean hasMusic() {
        return music;
    }

    public boolean hasRangeFinding() {
        return rangeFinding;
    }

    public double getFee() {
        return fee;
    }

    public String getCurrency() {
        return currency;
    }
}
