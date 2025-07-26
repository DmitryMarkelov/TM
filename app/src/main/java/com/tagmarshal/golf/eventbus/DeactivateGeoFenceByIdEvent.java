package com.tagmarshal.golf.eventbus;

import java.util.ArrayList;

public class DeactivateGeoFenceByIdEvent {
    private final ArrayList<String> geoFenceModels;

    public DeactivateGeoFenceByIdEvent(ArrayList<String> geoFenceModels) {
        this.geoFenceModels = geoFenceModels;
    }

    public ArrayList<String> getIds() {
        return geoFenceModels;
    }
}
