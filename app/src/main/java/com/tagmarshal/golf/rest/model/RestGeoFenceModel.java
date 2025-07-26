package com.tagmarshal.golf.rest.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tagmarshal.golf.util.TMUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RestGeoFenceModel implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("properties")
    private Properties properties;

    @SerializedName("geometry")
    private Geometry geometry;

    @SerializedName("activeTimeDays")
    private List<ActiveTimeDaysItemModel> activeTimeDays;
    
    @SerializedName("isCartControlActive")
    private boolean isCartControlActive = false;

    @SerializedName("cartControl")
    private CartControl cartControl = null;

    public String getId() {
        return id;
    }
    
    public boolean isCartControlActive() {
        return isCartControlActive;
    }

    public CartControl getCartControl() {
        return cartControl;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public List<ActiveTimeDaysItemModel> getActiveTimeDays() {
        return activeTimeDays != null ? activeTimeDays : new ArrayList<>();
    }

    public String getPriority() {
        return playSound() ? "high" : "low";
    }

    public boolean isNotify() {
        return getProperties().getAlertType().contains("Notify Operator");
    }

    public boolean isSubtleAlert() {
        return getProperties().getAlertType().contains("Subtle Player Alert");
    }

    public boolean playSound() {
        return getProperties().getAlertType().contains("Audible Player Alert");
    }

    public boolean isActive() {
        return TMUtil.activeTimeDaysPass(getActiveTimeDays());
    }

    public boolean isVisible() {
        return isActive() && getProperties().isTwoWayVisible();
    }
    

    public static class Geometry {

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

        public ArrayList<LatLng> getLatLngCoordinates() {
            ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
            for (int i = 0; i < getCoordinates().size(); i++)
                latLngs.add(new LatLng(getCoordinates().get(i).get(0), getCoordinates().get(i).get(1)));

            return latLngs;
        }

        public LatLngBounds getLatLngBoundsCoordinates() {
            LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
            for (LatLng latLng : getLatLngCoordinates()) {
                latLngBoundsBuilder.include(latLng);
            }

            return latLngBoundsBuilder.build();
        }

        public PolygonOptions getPolygons(int color, int backgroundColor) {
            return new PolygonOptions().addAll(getLatLngCoordinates()).clickable(false).fillColor(backgroundColor).strokeColor(color);
        }
    }

    public static class Properties {

        @SerializedName("alertType")
        private String alertType;

        @SerializedName("color")
        private String color;

        @SerializedName("message")
        private String message;

        @SerializedName("twoWayVisible")
        private boolean twoWayVisible;

        public String getMessage() {
            return message;
        }

        public String getColor() {
            return color.replace("#", "#99");
        }

        public String getAlertType() {
            return alertType;
        }

        public boolean isTwoWayVisible() {
            return twoWayVisible;
        }
    }

    public static class CartControl {
        @SerializedName("bufferZone")
        private int bufferZone;

        @SerializedName("stopAtTimer")
        private int stopAtTimer;

        @SerializedName("goAtTimer")
        private int goAtTimer;

        @SerializedName("goForTimer")
        private int goForTimer;

        @SerializedName("goAtTimeout")
        private int goAtTimeout;
        
        public int getBufferZone() {
            return bufferZone;
        }
        
        public int getStopAtTimer() {
            return stopAtTimer;
        }
        
        public int getGoAtTimer() {
            return goAtTimer;
        }
        
        public int getGoForTimer() {
            return goForTimer;
        }
        
        public int getGoAtTimeout() {
            return goAtTimeout;
        }
    }

}
 