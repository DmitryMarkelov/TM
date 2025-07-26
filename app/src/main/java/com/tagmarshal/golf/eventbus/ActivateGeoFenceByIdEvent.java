package com.tagmarshal.golf.eventbus;

import java.util.ArrayList;

public class ActivateGeoFenceByIdEvent {
    private final ArrayList<String> ids;

    public ActivateGeoFenceByIdEvent(ArrayList<String> ids) {
        this.ids = ids;
    }

    public ArrayList<String> getIds() {
        return ids;
    }
}
