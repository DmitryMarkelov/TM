package com.tagmarshal.golf.fragment.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

public interface FragmentMapContract {

    interface View {
        void makeNotification();

        void onGetCourseBackground(Bitmap image);
        
        void onGetFlags(Bitmap image);

        void onGetHoleInfo(RestHoleModel holeModel, int position);

        void onGetMapBounds(RestBoundsModel bounds);

        void onGetGeoFences(List<RestGeoFenceModel> geoFenceModels);

        void onGetTeeTimes(List<RestTeeTimesModel> teeTimesList);

        void onBitmapFromUrl(PointOfInterest point, BitmapDescriptor icon);

        void onGetRoundInfo(RestInRoundModel roundModel);

        void onGetHoleDistanceFromTee(RestHoleDistanceModel distanceModel);

        void onGetHoleDistanceFromPoint(RestHoleDistanceModel distanceModel);

        void onGetHoleDistanceFromPointAndMyLocation(RestHoleDistanceModel distanceModel);

        void onGetAllHoles(List<RestHoleModel> holes);

        void onCheckGeoFences();

        void onCountDownTimerToStartGameTick();

        void onSlideShowDismiss();

        void onFail(String message);

        void drawPointsOfInterest(List<PointOfInterest> pointOfInterests);

        void clearHoleDistances();

        void onGetGeoZones();
    }

    interface Model {
        Single<ResponseBody> sendLogsToSupport(SupportLogModel supportLogs);

        Observable<Long> startNotificationTimer();

        Single<List<CourseModel>> getCourses();

        Single<List<RestHoleModel>> getHoleInfo(int hole);

        Single<List<RestBoundsModel>> getMapBounds(String imei);

        Single<List<RestTeeTimesModel>> getTeeTimes();

        Single<RestInRoundModel> getRoundInfo();

        Single<List<RestHoleDistanceModel>> getHoleDistanceInfo(int hole);

        Single<RestHoleDistanceModel> getHoleDistanceFromPoint(LatLng tapLocation,
                                                               RestHoleModel holeModel);

        Single<RestHoleDistanceModel> getHoleDistanceFromPointAndMyLocation(LatLng myLocation,
                                                                            LatLng tapLocation,
                                                                            RestHoleModel holeModel);

        Single<List<RestGeoFenceModel>> getGeoFences(String imei);

        Single<ResponseBody> gotAlert(String alertId);

        Observable<PictureDrawable> getPictureDrawable(String url, Context context);

        Observable<List<RestHoleModel>> getAllHoles();

        Observable<Long> startCheckGeoFences();

        Observable<Long> startCountDownTimerToStartGame();

        Single<ResponseBody> sendLocationFix(FixModel fix);

        Observable<List<PointOfInterest>> getPointsOfInterest();

        Observable<Drawable>getImage(String image, Context context);

        Observable<List<AdvertisementModel>> getAdvertisements();

        Single<List<RestGeoZoneModel>> getGeoZones();
    }

    interface Presenter {

        void sendSupportLogs(String roundId);

        void getCourses();

        void onCreate();

        void onDestroy();

        void getBoundsForMap();

        void getCourseBackground(String baseUrl, Context context);

        void getAdvertisements(Context context);

        void getBitmapDescriptionFromUrl(PointOfInterest item, Context context);

        void getHoleInfo(int hole);

        void getGeoFences(String imei);

        void getGeoZones();

        void startCheckGeoFences();

        void startCountDownTimerToStartGame();

        void stopCountDownTimerToStartGame();

        void getTeeTimes();

        void getRoundInfo();

        void getHoleDistanceFromTee(int hole);

        void getHoleDistanceFromPoint(LatLng tapLocation,
                                      RestHoleModel holeModel);

        void getHoleDistanceFromPointAndMyLocation(LatLng myLocation,
                                                   LatLng tapLocation,
                                                   RestHoleModel holeModel);

        void gotAlert(String alertId);

        void getAllHoles();

        void sendLocationFix(FixModel fix);

        void getPointsOfInterest();

        void checkDistancesFromTappedToCurrent(String currentDistaneToHole, LatLng location, RestHoleModel currentModel);

        void stopSlideShowDismissTimer();

        void startSlideShowDismissTimer();

        void startMakingNotification();
        
        void stopMakingNotification();
    }
}
