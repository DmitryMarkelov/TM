package com.tagmarshal.golf.rest.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.tagmarshal.golf.manager.PreferenceManager;

import java.util.List;

public class RestGolfGeniusModel {

    @SerializedName("id")
    private String roundId;

    @SerializedName("holes")
    private int holes;

    @SerializedName("players")
    @Nullable
    private List<GolfGeniusModel.Player> players;

    public int getHoles() {
        return holes;
    }

    @Nullable
    public List<GolfGeniusModel.Player> getPlayers() {
        return players;
    }

    @Nullable
    public List<Integer> getPars() {
        return getPlayers().get(0).getPars();
    }

    @Nullable
    public List<Integer> getYardage() {
        return getPlayers().get(0).getYardage();
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public String getDescByIndex(int index) {
        String yardage = "0 yards";
        if(getYardage() != null) {
            if(PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER)) {
                yardage = Math.round(getYardage().get(index) * 0.9144) + " meters";
            } else {
                yardage = getYardage().get(index) + " yards";
            }
        }
        return "Par " + (getPars() != null ? getPars().get(index) : 0) + " - " + yardage;
    }
}
