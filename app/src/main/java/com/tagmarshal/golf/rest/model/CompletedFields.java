package com.tagmarshal.golf.rest.model;

public class CompletedFields {
    private int player;
    private String label;
    private String key;
    private String value;

    public CompletedFields(int player, String label, String key, String value) {
        this.player = player;
        this.label = label;
        this.key = key;
        this.value = value;
    }

    public int getPlayer() {
        return player;
    }

    public String getLabel() {
        return label;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
