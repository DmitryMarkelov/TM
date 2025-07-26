package com.tagmarshal.golf.service;

import com.google.android.gms.maps.model.LatLng;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

public interface GolfServiceContract {

    interface View {
        void readyToSendLocation();

        void readyToGetPaceInfo();

        void onUpdateLocation();

        void onSentLocation();

        void readyToGetAlertAndGeoFences(long time);

        void onGetPaceInfo(RestInRoundModel paceInfo);

        void onGetHoleDistanceFromPoint(RestHoleDistanceModel distanceModel);

        void onGetDistanceFromMeToHole(RestHoleDistanceModel distanceModel);

        void onGetAlerts(List<RestAlertModel> alertsList);

        void onSendFail();
    }

    interface Presenter {
        void startHourUpdatePaceInfo();

        void startSendLocationTimer();

        void stopSendLocationTimer();

        void startUpdatingLocationTimer();

        void getGeofences(String imei);

        void stopUpdatingLocationTimer();

        void stopPaceInfoTimer();

        void startRetrieveAlertsTimer();

        void stopRetrieveAlertsTimer();

        void startPaceInfoTimer();

        void getDistanceFromMeToHole(LatLng myLocation, RestHoleModel holeModel);

        void sendLocationFix(FixModel fixModel);

        void getAlerts();

        void getPaceInfo();

        void onDestroy();
    }

    interface Model {
        Observable<Long> startHourUpdatePaceInfo();

        Observable<Long> startSendLocationTimer();

        Observable<Long> startPaceInfoTimer();

        Observable<Long> startUpdatingLocationTimer();

        Observable<Long> startGetAlertsTimer();

        Single<List<RestGeoFenceModel>> getGeofences(String imei);

        Observable<RestInRoundModel> getPaceInfo();

        Single<RestHoleDistanceModel> getDistanceFromMeToHole(LatLng myLocation, RestHoleModel holeModel);

        Single<List<RestAlertModel>> getAlerts();

        Single<ResponseBody> sendLocationFix(FixModel fixModel);

        Single<ResponseBody> sendFailedFixes(ArrayList<FixModel> failedFixes);
    }
}
