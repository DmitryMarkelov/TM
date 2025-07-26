package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;
import com.tagmarshal.golf.util.GeofenceHelper;

public class FixModel {

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("accuracy")
    private double accuracy;

    @SerializedName("speed")
    private double speed;

    @SerializedName("dop")
    private double dop;

    @SerializedName("snr")
    private double snr;

    @SerializedName("battery")
    private int battery;

    @SerializedName("source")
    private String source = "TM App";

    @SerializedName("date")
    private String date;

    @SerializedName("geoBreach")
    private boolean geoBreach;

    @SerializedName("inGeofence")
    private boolean inGeofence;

    public FixModel(
            double lat,
            double lon,
            double accuracy,
            double speed,
            double dop,
            double snr,
            int battery,
            String date
    ) {
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
        this.speed = speed;
        this.dop = dop;
        this.snr = snr;
        this.battery = battery;
        this.date = date;
    }

    public FixModel(
            double lat,
            double lon,
            double accuracy,
            double speed,
            double dop,
            double snr,
            int battery,
            String date,
            boolean geoBreach,
            boolean inGeofence
    ) {
        this(lat, lon, accuracy, speed, dop, snr, battery, date);
        this.geoBreach = geoBreach;
        this.inGeofence = inGeofence;
    }

    public FixModel(
            double lat,
            double lon,
            double accuracy,
            double speed,
            double dop,
            double snr,
            int battery,
            String date,
            boolean geoBreach,
            boolean inGeofence,
            String source
    ) {
        this(lat, lon, accuracy, speed, dop, snr, battery, date, geoBreach, inGeofence);
        this.source = source;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getSNR() {
        return GeofenceHelper.averageTop3SNR;
    }

    public int getBattery() {
        return battery;
    }

    public String getDate() {
        return date;
    }
}
