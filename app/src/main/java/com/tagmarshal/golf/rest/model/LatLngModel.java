package com.tagmarshal.golf.rest.model;

import java.util.Objects;

public class LatLngModel {
    private double lat;
    private double lon;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof LatLngModel)) {
            return false;
        }
        LatLngModel latlng= (LatLngModel) o;
        return lat == latlng.lat &&
                Objects.equals(lat, latlng.lat) &&
                Objects.equals(lon, latlng.lon)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }
}
