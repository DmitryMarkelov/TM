package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SendOrderModel {

    @SerializedName("items")
    private List<SendOrderItem> items;

    @SerializedName("round")
    private String round;

    @SerializedName("specialInstructions")
    private String specialInstructions;

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public List<SendOrderItem> getItems() {
        return items;
    }

    public void setItems(List<SendOrderItem> items) {
        this.items = items;
    }
}

