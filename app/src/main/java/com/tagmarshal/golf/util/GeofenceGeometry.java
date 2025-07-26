package com.tagmarshal.golf.util;

import java.util.ArrayList;
import java.util.List;

public class GeofenceGeometry {
    private List<Point> points;
    private Boolean audible = false;
    private Boolean notify = true;
    private Boolean active = true;
    private String id = "";

    public GeofenceGeometry() {
        this.points = new ArrayList<>();
    }

    public void addPoint(double latitude, double longitude) {
        this.points.add(new Point(latitude, longitude));
    }

    public void setAudible(Boolean priority) {
        this.audible = priority;
    }

    public void setActive(Boolean isActive) {
        this.active = isActive;
    }

    public void setNotify(Boolean isNotify) {
        this.notify = isNotify;
    }

    public void setID(String id) {
        this.id = id;
    }

    public List<Point> getPoints() {
        return points;
    }

    public Boolean getAudible() { return audible; }

    public Boolean getActive() {
        return active;
    }

    public Boolean getNotify() {
        return active;
    }

    public String getID() {
        return id;
    }
}
