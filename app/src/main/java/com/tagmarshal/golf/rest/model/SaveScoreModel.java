package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveScoreModel {
    @SerializedName("round_id")
    private String roundId;

    @SerializedName("hole")
    private int hole;

    @SerializedName("players")
    private List<GolfGeniusModel.PlayerScore> players;

    public SaveScoreModel(String roundId, int hole, List<GolfGeniusModel.PlayerScore> players) {
        this.roundId = roundId;
        this.hole = hole;
        this.players = players;
    }

    public int getHole() {
        return hole;
    }

    public String getRoundId() {
        return roundId;
    }
}
