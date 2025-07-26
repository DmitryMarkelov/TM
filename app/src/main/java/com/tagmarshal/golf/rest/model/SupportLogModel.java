package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SupportLogModel {
    @SerializedName("date")
    private String date;
    @SerializedName("course_name")
    private String courseName;
    @SerializedName("course_url")
    private String courseUrl;
    @SerializedName("device_id")
    private String deviceID;
    @SerializedName("round_id")
    private String roundId;
    @SerializedName("logs")
    private List<String> logs;

    public SupportLogModel(String date, String courseName, String courseUrl, String deviceID, List<String> logs) {
        this.date = date;
        this.courseName = courseName;
        this.courseUrl = courseUrl;
        this.deviceID = deviceID;
        this.logs = logs;
    }

    public SupportLogModel(String deviceID, List<String> logs, String roundId) {
        this.deviceID = deviceID;
        this.logs = logs;
        this.roundId = roundId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseUrl() {
        return courseUrl;
    }

    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
