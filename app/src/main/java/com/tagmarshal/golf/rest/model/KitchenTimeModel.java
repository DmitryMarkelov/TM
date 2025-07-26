package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

public class KitchenTimeModel {

    @SerializedName("start")
    private String start;

    @SerializedName("end")
    private String end;


    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
