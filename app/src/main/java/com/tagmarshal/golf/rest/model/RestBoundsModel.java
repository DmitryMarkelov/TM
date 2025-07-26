package com.tagmarshal.golf.rest.model;

import android.util.Log;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RestBoundsModel implements Serializable {
    @SerializedName("northEastLatitude")
    @Expose
    private String nELAT;

    @SerializedName("northEastLongitude")
    @Expose
    private String nELON;

    @SerializedName("southWestLatitude")
    @Expose
    private String sWLAT;

    @SerializedName("southWestLongitude")
    @Expose
    private String sWLON;

    @SerializedName("fixHdopThreshold")
    @Expose
    private double fixHdopThreshold = 0;

    @SerializedName("fixSnrThreshold")
    @Expose
    private double fixSnrThreshold = 0;

    @SerializedName("geoHdopThreshold")
    @Expose
    private double geoHdopThreshold = 0;

    @SerializedName("geoSnrThreshold")
    @Expose
    private double geoSnrThreshold = 0;

//    @SerializedName("cartControl")
//    @Expose
//    private String cartControl;

    @SerializedName("drinksActive")
    @Expose
    private boolean isDrinksActive;

    @SerializedName("softGeofenceTimer")
    @Expose
    private int highPriorityFenceTimer;

    @SerializedName("turnTime")
    @Expose
    private int turnTime;

    @SerializedName("kitchenOperatingHours")
    private List<ActiveTimeDaysItemModel> kitchen;

    @SerializedName("currency")
    @Expose
    private String currency;

    @SerializedName("scoringActive")
    @Expose
    private boolean isScoreActive;

    @SerializedName("leaderboardActive")
    @Expose
    private boolean isLeaderActive;

    @SerializedName("ratingActive")
    @Expose
    private boolean isRateRoundActive;

    @SerializedName("foodAndBeveragesActive")
    @Expose
    private boolean foodAndBeveragesActive;

    @SerializedName("groupSafetyActive")
    @Expose
    private boolean groupSafety;

    @SerializedName("outOfSequenceActive")
    @Expose
    private Boolean outOfSequenceActive;

    @SerializedName("returnDeviceActive")
    @Expose
    private boolean returnDevice;

    @SerializedName("courseCode")
    @Expose
    private String courseCode;

    @SerializedName("logo")
    @Expose
    private String logo;

    @SerializedName("map")
    @Expose
    private String map;

    @SerializedName("startHoles")
    @Expose
    private List<Integer> startHoles;

    @SerializedName("disclaimer")
    private Disclaimer disclaimer;

    @SerializedName("snap_to_play")
    private SnapToPlayModel snapToPlay;

    public List<ActiveTimeDaysItemModel> getKitchen() {
        return kitchen;
    }

    public Boolean isOutOfSequenceActive() {
        return outOfSequenceActive;
    }

    public boolean getGroupSafety() {
        return groupSafety;
    }

    public boolean isLeaderActive() {
        return isLeaderActive;
    }

    public boolean isDrinksActive() {
        return isDrinksActive;
    }

    public int getHighPriorityFenceTimer() {
        return highPriorityFenceTimer;
    }

    public boolean isScoreActive() {
        return isScoreActive;
    }

    public String getNELAT() {
        return nELAT;
    }

    public String getNELON() {
        return nELON;
    }

    public String getSWLAT() {
        return sWLAT;
    }

    public String getSWLON() {
        return sWLON;
    }

    public double getFixHdopThreshold() {
        return fixHdopThreshold;
    }

    public double getFixSnrThreshold() {
        return fixSnrThreshold;
    }

    public double getGeoHdopThreshold() {
        return geoHdopThreshold;
    }

    public double getGeoSnrThreshold() {
        return geoSnrThreshold;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isRateRoundActive() {
        return isRateRoundActive;
    }

    public boolean isFoodAndBeveragesActive() {
        return foodAndBeveragesActive;
    }

    public boolean getReturnDeviceActiveStatus() {
        return returnDevice;
    }

    public List<Integer> getStartHoles() {
        return startHoles;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getTurnTime() {
        return turnTime;
    }

    public String getLogo() {
        return logo;
    }

    public String getMap() {
        return map;
    }

    public Disclaimer getDisclaimer() {
        Log.d("GEOGT", "Disclaimer Tostring:"+disclaimer.toString());
        return disclaimer;
    }

    
//    public String getCartControl() {
//        return cartControl;
//    }
  
    public SnapToPlayModel getSnapToPlay() {
        return snapToPlay;
    }
}
