package com.tagmarshal.golf.refresher;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;

import java.util.List;

import io.reactivex.Single;

public class GeoRefresherModel implements GeoRefresherContract.Model {
    @Override
    public Single<List<RestGeoFenceModel>> getGeofences(String imei) {
        return GolfAPI.getGolfCourseApi().getGeoFences(imei);
    }

    @Override
    public Single<List<RestGeoZoneModel>> getGeoZones() {
        return GolfAPI.getGolfCourseApi().getGeoZones();
    }

    @Override
    public Single<List<RestBoundsModel>> getMapBounds(String imei) {
        return GolfAPI.getGolfCourseApi().getMapBounds(imei);
    }
}
