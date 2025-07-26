package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import java.util.List;

public class ActiveTimeDaysItemModel implements Serializable {

    @SerializedName("days")
    private List<String> days;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    public List<String> getDays() {
        return days;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
