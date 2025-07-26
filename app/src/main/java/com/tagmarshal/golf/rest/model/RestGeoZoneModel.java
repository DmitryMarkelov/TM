package com.tagmarshal.golf.rest.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RestGeoZoneModel {

    @SerializedName("geometry")
    private Geometry geometry;

    @SerializedName("properties")
    private Properties properties;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getHoleInternalId() {
        return (getProperties().getColor() + getProperties().getHole()).trim().toLowerCase();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public class Geometry {

        @SerializedName("type")
        private String type;

        @SerializedName("coordinates")
        private List<List<Double>> coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<List<Double>> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<List<Double>> coordinates) {
            this.coordinates = coordinates;
        }

        public PolygonOptions getPolygon() {
            ArrayList<LatLng> coordinates = new ArrayList<>();
            for (int i = 0; i < getCoordinates().size(); i++) {
                coordinates.add(new LatLng(getCoordinates().get(i).get(0), getCoordinates().get(i).get(1)));
            }

            return new PolygonOptions()
                    .addAll(coordinates)
                    .clickable(false)
                    .visible(false);
        }
    }

    public class Properties {

        @SerializedName("hole")
        private int hole;

        @SerializedName("number")
        private int number;

        @SerializedName("colour")
        private String color;

        public int getHole() {
            return hole;
        }

        public void setHole(int hole) {
            this.hole = hole;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }



    }
}
