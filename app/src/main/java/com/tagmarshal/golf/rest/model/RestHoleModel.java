package com.tagmarshal.golf.rest.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RestHoleModel implements Serializable, Comparable {
    @SerializedName("hole")
    @Expose
    private int hole;

    @SerializedName("par")
    @Expose
    private String par;

    @SerializedName("startPoint")
    @Expose
    private List<Double> startPoint = null;

    @SerializedName("endPoint")
    @Expose
    private List<Double> endPoint = null;

    @SerializedName("centerPoint")
    @Expose
    private List<Double> centerPoint = null;

    @SerializedName("pin")
    @Expose
    private List<Double> pin = null;

    @SerializedName("frontPoint")
    @Expose
    private List<Double> frontPoint = null;

    @SerializedName("colour")
    @Expose
    private String color;

    @SerializedName("order")
    @Expose
    private int order;

    @SerializedName("displayHole")
    @Expose
    private String displayHole;

    public void setPar(String par) {
        this.par = par;
    }

    public void setCenterPoint(List<Double> centerPoint) {
        this.centerPoint = centerPoint;
    }

    public void setPin(List<Double> pin) {
        this.pin = pin;
    }

    public void setFrontPoint(List<Double> frontPoint) {
        this.frontPoint = frontPoint;
    }

    public void setDisplayHole(String displayHole) {
        this.displayHole = displayHole;
    }

    public String getDisplayHole() {
        return displayHole;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHoleInternalId() {
        return (getColor() + getHole()).trim().toLowerCase();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getPar() {
        return par;
    }

    public List<Double> getFrontPoint() {
        return frontPoint;
    }

    public int getHole() {
        return hole;
    }

    public String getHoleDisplay() {
        return displayHole != null
                ? displayHole
                : String.valueOf(hole);
    }

    public void setHole(int hole) {
        this.hole = hole;
    }

    public List<Double> getStartPoint() {
        return startPoint;
    }

    public LatLng getCenterPointLatLng() {
        return new LatLng(getCenterPoint().get(0), getCenterPoint().get(1));
    }

    public boolean checkIsNullRequiredFields() {
        if (getStartPoint() == null || getFrontPoint() == null || getEndPoint() == null ||
                getCenterPoint() == null || getHole() == 0)
            return true;
        else
            return false;
    }

    public LatLng getFrontPointLatLng() {
        return new LatLng(getFrontPoint().get(0), getFrontPoint().get(1));
    }

    public LatLng getBackPointLatLng() {
        return new LatLng(getEndPoint().get(0), getEndPoint().get(1));
    }

    public LatLng getStartPointLatLng() {
        return new LatLng(getStartPoint().get(0), getStartPoint().get(1));
    }

    public void setStartPoint(List<Double> startPoint) {
        this.startPoint = startPoint;
    }

    public List<Double> getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(List<Double> endPoint) {
        this.endPoint = endPoint;
    }

    public List<Double> getCenterPoint() {
        if(getPin() != null && getPin().size() == 2)
            return getPin();
        return centerPoint;
    }

    public List<Double> getPin() {
        return pin;
    }

    @Override
    public int compareTo(Object o) {
        int compareQuantity = ((RestHoleModel) o).getOrder();

        return this.getOrder() - compareQuantity;
    }

    @NonNull
    @Override
    public String toString() {
        return " hole=" + hole +
                " startpoint=" + startPoint +
                " centerpoint=" + centerPoint +
                " endpoint=" + endPoint +
                " frontpoint="+ frontPoint +
                " color=" + color;
    }
}
