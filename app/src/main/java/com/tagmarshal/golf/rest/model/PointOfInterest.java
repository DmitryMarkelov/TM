package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PointOfInterest {

    @SerializedName("_id")
    private String id;
    @SerializedName("coordinates")
    private List<Double> coordinates;
    @SerializedName("name")
    private String name;
    @SerializedName("icon")
    private String image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
