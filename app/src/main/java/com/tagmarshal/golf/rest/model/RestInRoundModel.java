package com.tagmarshal.golf.rest.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Set;

public class RestInRoundModel implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("currentHole")
    private String currentHole;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("goalTime")
    private String goalTime;

    @SerializedName("playTime")
    private int playTime;

    @SerializedName("pace")
    private int pace;

    @SerializedName("heldUp")
    private boolean heldUp;

    @SerializedName("holdingUp")
    private boolean holdingUp;

    @SerializedName("isNineHole")
    private boolean isNineHole;

    @SerializedName("complete")
    private boolean complete;

    @SerializedName("captureLogs")
    private boolean captureLogs;

    @SerializedName("messagesPending")
    private boolean messagesPending;

    @SerializedName("isGolfGenius")
    private boolean isGolfGenius;

    @SerializedName("golfGeniusEventId")
    private String golfGeniusEventId;

    @SerializedName("golfGeniusEventRoundId")
    private String golfGeniusEventRoundId;

    @SerializedName("golfGeniusId")
    private String golfGeniusId;

    @SerializedName("inRound")
    private boolean inRound;

    @SerializedName("ignoreGeofence")
    private boolean ignoreGeofence = false;

    @SerializedName("refresh")
    private boolean refresh;

    @SerializedName("waiverEnabled")
    private boolean waiverEnabled;

    @SerializedName("devices")
    @Nullable
    private Set<TMDevice> devices;

    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public boolean isInRound() {
        return inRound;
    }

    public boolean isCaptureLogs() {
        return captureLogs;
    }

    public String getCurrentHole() {
        if (currentHole != null && currentHole.equals(""))
            return "1";

        return currentHole;
    }

    @Nullable
    public Set<TMDevice> getDevices() {
        return devices;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public boolean isMessagesPending() {
        return messagesPending;
    }

    public int getPlayTime() {
        return playTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getGoalTime() {
        return goalTime;
    }

    public boolean isNineHole() {
        return isNineHole;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isNullModel() {
        return !isComplete() &&
                !isNineHole() &&
                !isInRound() &&
                getPace() == 0 &&
                getCurrentHole() == null &&
                getGoalTime() == null &&
                getPlayTime() == 0 &&
                getStartTime() == null &&
                !isHoldingUp() &&
                !isHeldUp();
    }

    public int getPace() {
        return pace;
    }

    public boolean isHeldUp() {
        return heldUp;
    }

    public boolean isHoldingUp() {
        return holdingUp;
    }

    public boolean isGolfGenius() {
        return isGolfGenius;
    }

    public void setGolfGenius(boolean golfGenius) {
        this.isGolfGenius = golfGenius;
    }

    public String getGolfGeniusEventId() {
        return golfGeniusEventId;
    }

    public String getGolfGeniusEventRoundId() {
        return golfGeniusEventRoundId;
    }

    public String getGolfGeniusId() {
        return golfGeniusId;
    }

    public boolean isIgnoreGeofence() {
        return ignoreGeofence;
    }

    public boolean isWaiverEnabled() { return waiverEnabled; }
}
