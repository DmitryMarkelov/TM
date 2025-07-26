package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GolfGeniusModel {

    @SerializedName("round_id")
    private final String roundId;

    @SerializedName("players")
    private final List<Player> players;

    @SerializedName("scores")
    private List<Score> scores;

    @SerializedName("complete")
    private boolean complete;

    public GolfGeniusModel(String roundId, List<Player> players, List<Score> scores) {
        this.roundId = roundId;
        this.players = players;
        this.scores = scores;
        this.complete = false;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public String getRoundId() {
        return roundId;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public static class Player {
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String number;

        @SerializedName("scoreRegime")
        private List<String> scoreRegime;

        @SerializedName("pars")
        private List<Integer> pars;

        @SerializedName("yardage")
        private List<Integer> yardage;

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public List<String> getScoreRegime() {
            return scoreRegime;
        }

        public List<Integer> getPars() {
            return pars;
        }

        public List<Integer> getYardage() {
            return yardage;
        }
    }

    public static class Score {
        @SerializedName("numbers")
        private List<String> numbers; // player #s

        @SerializedName("scores")
        private List<Integer> scores; // player scores

        @SerializedName("noscores")
        private List<Boolean> noScores; // player is no scores

        @SerializedName("desc")
        private String desc; // hole desc

        @SerializedName("hole")
        private int hole;

        public Score(List<String> numbers, List<Integer> scores, List<Boolean> noScores, String desc, int hole) {
            this.numbers = numbers;
            this.scores = scores;
            this.desc = desc;
            this.hole = hole;
            this.noScores = noScores;
        }


        public List<String> getNumbers() {
            return numbers;
        }

        public void setNumbers(List<String> numbers) {
            this.numbers = numbers;
        }

        public List<Integer> getScores() {
            return scores;
        }

        public void setScores(List<Integer> scores) {
            this.scores = scores;
        }

        public void setNoScores(List<Boolean> noScores) {
            this.noScores = noScores;
        }

        public int getHole() {
            return hole;
        }

        public void setHole(int hole) {
            this.hole = hole;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public List<Boolean> getNoScores() {
            return noScores;
        }
    }



    public static class PlayerScore {
        @SerializedName("id")
        private String id;

        @SerializedName("strokes")
        private int strokes;

        @SerializedName("noscore")
        private boolean noScore;

        public PlayerScore(String id, int strokes) {
            this.id = id;
            this.strokes = strokes;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getStrokes() {
            return strokes;
        }

        public void setStrokes(int strokes) {
            this.strokes = strokes;
        }

        public boolean isNoScore() {
            return noScore;
        }

        public void setNoScore(boolean noScore) {
            this.noScore = noScore;
        }
    }
}

