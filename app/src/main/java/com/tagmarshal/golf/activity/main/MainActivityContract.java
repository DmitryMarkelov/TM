package com.tagmarshal.golf.activity.main;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.rest.model.CourseConfigModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestGolfGeniusModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface MainActivityContract {

    interface Presenter {
        void startInactiveStateTimer();

        void callDrinksCart(String imei);

        void startDimScrenTimer();

        void gotAlert(String alertId);

        void startTimerSendToClubNotification(Map<String, String> body);

        void sendNotificationToClub(Map<String, String> body);

        void sendFirebaseToken(Map<String, String> token);

        void getAlerts();

        void updateFirmwareStatus(int progress, String status);

        void getGolfGeniusInfo(String round_id);

        void stopInactiveTimer();

        void checkForNewCourseEnter(LatLng location);

        void startTimerToChangeCourse(CourseModel newCourse);

        void reSaveNearestCourses();

        void changeCourse(CourseModel newCourse);

        @SuppressLint("CheckResult")
        Observable<RestDeviceConfigModel> registerDevice(String type, String build);

        void stopTimerToChangeCourse();

        void stopChangeCourseDelay();

        void startChangeCourseDelay();

        void startInRoundFalseAccDelay();

        void startInRoundTrueAccDelay();

        void startMaintenance();

        void stopMaintenance();

    }

    interface Model {
        Single<List<RestAlertModel>> getAlerts();

        Single<ResponseBody> updateFirmwareStatus(String imei, int progress, String status);

        Single<Long> startInactiveStateTimer();

        Single<Response<ResponseBody>> callDrinksCart(String imei);

        Single<ResponseBody> gotAlert(String alertId);

        Single<Long> startDimScreenTimer();

        Single<Response<ResponseBody>> sendRefreshToken(Map<String, String> token);

        Single<Response<ResponseBody>> sendMessageToClub(Map<String, String> body);

        Observable<CourseModel> getCurrentCourse();

        Observable<List<CourseModel>> getNearestCourses();

        Single<RestGolfGeniusModel> getGolfGeniusInfo(String round_id);


        void saveDeviceTag(String tag, String url);

        void saveCourse(CourseModel newCourse);

        void clearGameData(String url);

        void saveCourseConfig(CourseConfigModel courseConfigModel);


        Observable<List<RestGeoZoneModel>> getGeoZonesByUrl(CourseModel courseModel);

        Observable<List<RestGeoFenceModel>> getGeofencesByUrl(CourseModel courseModel, String deviceIMEI);

        Observable<List<RestHoleModel>> getHolesByUrl(CourseModel courseModel);

        Observable<RestBoundsModel> getCourseConfigByUrl(CourseModel courseModel);

        Observable<List<PointOfInterest>> getPointsOfInterestByUrl(CourseModel courseModel);

        Single<RestDeviceConfigModel> registerDevice(String type, String build);

        Observable<List<RestGeoZoneModel>> getGeoZones();

        Observable<List<RestGeoFenceModel>> getGeofences(String deviceIMEI);

        Observable<List<RestHoleModel>> getHoles();

        Observable<List<PointOfInterest>> getPointsOfInterest();

        Observable<RestBoundsModel> getCourseConfig(String imei);

        Single<List<CourseModel>> getCourses();
    }

    interface View {
        void showMaintenanceFragment();

        void onInactiveState();

        void onSuccessDrinksCartCall();

        void onFail(String string);

        BaseFragment openStartScreen();

        void sendFirebaseToken();

        void showPleaseWaitDialog(boolean show);

        void onGetAlerts(List<RestAlertModel> alertsList);

        void onGetGolfGeniusInfo(RestGolfGeniusModel model);

        void dimScreen(Long aLong);

        void returnDimScreen();

        void showCourseChangeAlert(CourseModel newCourse);

        void changeCourseNotificationDelayStatus(boolean isActive);

        void dissmissNewCourseDialog();

        void clearFragments();

        void openMapFragment();

        void openMapFragment(boolean clearLogo);

        void changeCurrentName(String courseName);

        void onInRoundFalseInteractionDelayTick();

        void onInRoundTrueInteractionDelayTick();

        void onReSaveSuccess();

        void onUpdateFirmwareStatus(ResponseBody responseBody, String status);

        void onUpdateFirmwareStatus(Throwable throwable, String status);
    }
}
