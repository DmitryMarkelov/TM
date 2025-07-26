package com.tagmarshal.golf.refresher;

import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;

import java.util.List;

import io.reactivex.Single;

public interface GeoRefresherContract {

    interface Presenter{
        void getGeofences(String imei);

        void getGeoZones();

        void onDestroy();

        void getMapBounds();
    }

    interface  Model{
        Single<List<RestGeoFenceModel>> getGeofences(String imei);

        Single<List<RestGeoZoneModel>> getGeoZones();

        Single<List<RestBoundsModel>> getMapBounds(String imei);
    }


}
