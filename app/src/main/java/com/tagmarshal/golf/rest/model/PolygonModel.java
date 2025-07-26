package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class PolygonModel {
    @SerializedName("type")
    private String type;
    @SerializedName("coordinates")
    private CoordinateModel coordinate;





    public String getType() {
        return type;
    }

    public CoordinateModel getCoordinate() {
        return coordinate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCoordinate(CoordinateModel coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof PolygonModel)) {
            return false;
        }
        PolygonModel polygon= (PolygonModel) o;
        return type.equals(polygon.type) &&
                Objects.equals(type, polygon.type) &&
                Objects.equals(coordinate,polygon.coordinate)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinate);
    }
}
