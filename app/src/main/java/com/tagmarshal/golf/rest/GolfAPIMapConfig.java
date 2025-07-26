package com.tagmarshal.golf.rest;

import com.tagmarshal.golf.rest.model.RestGeoZoneModel;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface GolfAPIMapConfig {

    @GET("app/getzones/")
    Observable<List<RestGeoZoneModel>> getGeoZones();
}
