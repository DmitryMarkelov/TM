package com.tagmarshal.golf.rest.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RestDeviceConfigModel implements Serializable {

    @SerializedName("tag")
    private String tag;

    @SerializedName("course")
    private RestBoundsModel course;

    @SerializedName("holes")
    private List<RestHoleModel> holes;

    @SerializedName("sections")
    private List<RestGeoZoneModel> sections;

    @SerializedName("geofences")
    private List<RestGeoFenceModel> geofences;

    private String errMessage = null;

    public boolean isValid() {
        if(this.getTag() == null) {
            this.setErrMessage("Device has no tag.");
            return false;
        } else if(
                this.getCourse() == null
                        || this.getCourse().getNELAT() == null
                        || this.getCourse().getNELON() == null
                        || this.getCourse().getSWLAT() == null
                        || this.getCourse().getSWLON() == null
        ) {
            this.setErrMessage("Map bounds error.");
            return false;
        } if(this.getHoles() == null || this.getHoles().isEmpty()) {
            this.setErrMessage("Course has no holes.");
            return false;
        } if(this.getSections() == null || this.getSections().isEmpty()) {
            this.setErrMessage("Course has no sections.");
            return false;
        } else {
            for(RestHoleModel holeModel: this.getHoles()) {
                if(
                        holeModel.getStartPoint() == null
                                || holeModel.getStartPoint().size() != 2
                                || holeModel.getFrontPoint() == null
                                || holeModel.getFrontPoint().size() != 2
                                || holeModel.getCenterPoint() == null
                                || holeModel.getCenterPoint().size() != 2
                                || holeModel.getEndPoint() == null
                                || holeModel.getEndPoint().size() != 2
                ) {
                    this.setErrMessage("Error with hole " + holeModel.getHole() + " coordinates.");
                    return false;
                }
            }
        }
        return true;
    }

    public void setCourse(RestBoundsModel course) {
        this.course = course;
    }

    public List<RestHoleModel> getHoles() {
        return holes;
    }

    public void setHoles(List<RestHoleModel> holes) {
        this.holes = holes;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public RestBoundsModel getCourse() {
        return course;
    }

    public List<RestGeoZoneModel> getSections() {
        return sections;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public List<RestGeoFenceModel> getGeofences() {
        return geofences;
    }
}
