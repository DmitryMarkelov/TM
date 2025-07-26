package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScoreHoleModel {

    @SerializedName("hole_number")
    private final int holeNumber;

    @SerializedName("desc")
    private final String desc;

    @SerializedName("players")
    private final List<Player> players;

    public ScoreHoleModel(int holeNumber, String desc, List<Player> players) {
        this.holeNumber = holeNumber;
        this.desc = desc;
        this.players = players;
    }

    public int getHoleNumber() {
        return holeNumber;
    }

    public String getDesc() {
        return desc;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public static class Player {

        @SerializedName("name")
        private String name;

        @SerializedName("number")
        private String number;

        @SerializedName("strokes")
        private int strokes;

        public Player(String name, String number, int strokes) {
            this.name = name;
            this.number = number;
            this.strokes = strokes;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public int getStrokes() {
            return strokes;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public void setStrokes(int strokes) {
            this.strokes = strokes;
        }
    }
}
