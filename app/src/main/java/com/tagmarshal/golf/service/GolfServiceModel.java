package com.tagmarshal.golf.service;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

public class GolfServiceModel implements GolfServiceContract.Model {

    @Override
    public Observable<Long> startSendLocationTimer() {
        return Observable.interval(0, 30, TimeUnit.SECONDS);
    }

    @Override
    public Observable<Long> startHourUpdatePaceInfo() {
        return Observable.interval(1, 1, TimeUnit.HOURS);
    }

    @Override
    public Observable<Long> startUpdatingLocationTimer() {
        return Observable.interval(0, 1, TimeUnit.SECONDS);
    }

    @Override
    public Observable<Long> startGetAlertsTimer() {
        return Observable.interval(0, 1, TimeUnit.MINUTES);
    }

    @Override
    public Single<List<RestGeoFenceModel>> getGeofences(String imei) {
        return GolfAPI.getGolfCourseApi().getGeoFences(imei);
    }

    @Override
    public Observable<Long> startPaceInfoTimer() {
        return Observable.timer(10, TimeUnit.SECONDS);
    }

    @Override
    public Observable<RestInRoundModel> getPaceInfo() {
        return GolfAPI.getGolfCourseApi().getRepeatDeviceInfo();
    }

    @Override
    public Single<RestHoleDistanceModel> getDistanceFromMeToHole(final LatLng myLocation, final RestHoleModel holeModel) {
        return Single.fromCallable(new Callable<RestHoleDistanceModel>() {
            @Override
            public RestHoleDistanceModel call() throws Exception {
                return TMUtil.getDistanceFromMeLocation(myLocation, holeModel);
            }
        });
    }

    @Override
    public Single<List<RestAlertModel>> getAlerts() {
        return GolfAPI.getGolfCourseApi().getAlerts();
    }

    @Override
    public Single<ResponseBody> sendLocationFix(FixModel fix) {
        return GolfAPI.getGolfCourseApi().sendLocationFix(fix);
    }

    @Override
    public Single<ResponseBody> sendFailedFixes(ArrayList<FixModel> failedFixes) {
        return GolfAPI.getGolfCourseApi().sendFailedFixes(failedFixes);
    }
}
