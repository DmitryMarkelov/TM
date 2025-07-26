package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RestTeeTimesModel implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("startHole")
    private String hole;

    @SerializedName("players")
    private List<String> players;

    @SerializedName("time")
    private String teeTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHole() {
        return hole;
    }

    public void setHole(String hole) {
        this.hole = hole;
    }

    public String getPlayer1() {
        return players != null && players.size() > 0 ? players.get(0) : "";
    }

    public String getPlayer2() {
        return players != null && players.size() > 1 ? players.get(1) : "";
    }

    public String getPlayer3() {
        return players != null && players.size() > 2 ? players.get(2) : "";
    }

    public String getPlayer4() {
        return players != null && players.size() > 3 ? players.get(3) : "";
    }

    public String getTeeTime() {
        return teeTime;
    }
}
