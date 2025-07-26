package com.tagmarshal.golf.rest.model;

import java.util.List;
import java.util.Objects;

public class CoordinateModel {
    private List<LatLngModel> latLng;

    public List<LatLngModel> getLatLng() {
        return latLng;
    }

    public void setLatLng(List<LatLngModel> latLng) {
        this.latLng = latLng;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof CoordinateModel)) {
            return false;
        }
        CoordinateModel coordinate= (CoordinateModel) o;
        return latLng.equals(coordinate.latLng) &&
                Objects.equals(latLng, coordinate.latLng);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latLng);
    }
}
