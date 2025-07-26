package com.tagmarshal.golf.activity.main;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CoordinateModel;
import com.tagmarshal.golf.rest.model.CourseConfigModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.LatLngModel;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.PolygonModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestGolfGeniusModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.util.TMUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class MainActivityModel implements MainActivityContract.Model {


    private final Context context;

    public MainActivityModel(Context context) {
        this.context = context;
    }

    @Override
    public Single<List<RestAlertModel>> getAlerts() {
        return GolfAPI.getGolfCourseApi().getAlerts();
    }

    @Override
    public Single<ResponseBody> updateFirmwareStatus(String imei, int progress, String status) {
        return GolfAPI.getGolfApi().updateDownloadStatus(imei, progress, status);
    }

    @Override
    public Single<Long> startInactiveStateTimer() {
        if (PreferenceManager.getInstance().isDeviceMobile()) {
            return Single.timer(60, TimeUnit.SECONDS);
        } else {
            return Single.timer(120, TimeUnit.SECONDS);
        }
    }

    @Override
    public Single<Long> startDimScreenTimer() {
        if (PreferenceManager.getInstance().isDeviceMobile()) {
            return Single.timer(5, TimeUnit.MINUTES);
        } else {
            return Single.timer(120, TimeUnit.SECONDS);
        }
    }


    @Override
    public Single<Response<ResponseBody>> sendRefreshToken(Map<String, String> token) {
        return GolfAPI.getGolfCourseApi().sendFirebaseRefreshToken(token);
    }

    @Override
    public Single<Response<ResponseBody>> callDrinksCart(String imei) {
        return GolfAPI.getGolfCourseApi().callDrinksCart(imei);
    }

    @Override
    public Single<ResponseBody> gotAlert(String alertId) {
        return GolfAPI.getGolfCourseApi().sendAcknowledgeAlerts(alertId);
    }

    @Override
    public Single<RestGolfGeniusModel> getGolfGeniusInfo(String round_id) {
        return GolfAPI.getGolfCourseApi().getGolfGeniusInfo(round_id);
    }

    @Override
    public Single<Response<ResponseBody>> sendMessageToClub(Map<String, String> body) {
        return GolfAPI.getGolfCourseApi().sendMessage(body);
    }

    @Override
    public Observable<CourseModel> getCurrentCourse() {
        if (PreferenceManager.getInstance().getCurrentCourseModel() != null) {
            return Observable.just(PreferenceManager.getInstance().getCurrentCourseModel());
        } else {
            return null;
        }
    }

    @Override
    public Observable<List<CourseModel>> getNearestCourses() {
        return Observable.just(PreferenceManager.getInstance().getNearestCourses());
    }

    @Override
    public void saveDeviceTag(String tag, String url) {
        PreferenceManager.getInstance().setDeviceTag(tag, url);
    }

    @Override
    public void saveCourse(CourseModel newCourse) {
        PreferenceManager.getInstance().setCourseName(newCourse.getCourseName(), newCourse.getCourseUrl());
        PreferenceManager.getInstance().setCourse(newCourse.getCourseUrl());
        PreferenceManager.getInstance().saveCurrentCourse(newCourse, newCourse.getCourseUrl());
    }

    @Override
    public void clearGameData(String url) {
        PreferenceManager.getInstance().clearGameData(url);
    }


    @Override
    public void saveCourseConfig(CourseConfigModel currentCourseConfig) {
        PreferenceManager.getInstance().setCourseHoles(currentCourseConfig.getHoles());
        PreferenceManager.getInstance().setMapBounds(currentCourseConfig.getMapConfig());
        PreferenceManager.getInstance().setCourseGeoFences(currentCourseConfig.getGeoFence());
        PreferenceManager.getInstance().setCourseGeoZones(currentCourseConfig.getGeoZones());
        if(currentCourseConfig.getMapConfig() != null) {
            PreferenceManager.getInstance().saveKitchenTime(currentCourseConfig.getMapConfig().getKitchen());
        } else {
            PreferenceManager.getInstance().saveKitchenTime(null);
        }
    }


    @Override
    public Observable<List<RestHoleModel>> getHoles() {
        return GolfAPI.getGolfCourseApi().getAllHolesObservable()
                .flatMapIterable(item -> item)
                .toSortedList((first, second) -> first.getOrder() - second.getOrder())
                .map(sortedList -> {
                    for (int i = 0; i < sortedList.size(); i++) {
                        sortedList.get(i).setOrder(i + 1);
                    }
                    return sortedList;
                })
                .toObservable();
    }


    @Override
    public Observable<List<RestGeoFenceModel>> getGeofences(String imei) {
        return GolfAPI.getGolfCourseApi().getGeoFences(imei)
                .toObservable();
    }

    @Override
    public Observable<List<RestGeoZoneModel>> getGeoZones() {
        return GolfAPI.getGolfCourseApi().getGeoZones()
                .toObservable();
    }

    @Override
    public Observable<List<PointOfInterest>> getPointsOfInterest() {
        return GolfAPI.getGolfCourseApi().getPointsOfInterest();
    }


    @Override
    public Observable<RestBoundsModel> getCourseConfig(String imei) {
        return GolfAPI.getGolfCourseApi().getMapBounds(imei)
                .map(list -> list.get(0))
                .toObservable();
    }

    @Override
    public Observable<List<RestGeoZoneModel>> getGeoZonesByUrl(CourseModel courseModel) {
        return GolfAPI.getGolfCourseApi().getGeoZonesByUrl("https://" + courseModel.getCourseUrl() + "/app/getzones/");
    }

    @Override
    public Observable<List<RestGeoFenceModel>> getGeofencesByUrl(CourseModel courseModel, String deviceIMEI) {
        return GolfAPI.getGolfCourseApi().getGeoFencesByUrl("https://" + courseModel.getCourseUrl() + "/getgeofences/imei/" + deviceIMEI + "/");
    }

    @Override
    public Observable<List<RestHoleModel>> getHolesByUrl(CourseModel courseModel) {
        return GolfAPI.getGolfCourseApi().getCourseHolesByUrl("https://" + courseModel.getCourseUrl() + "/app/getallholeinfo/");
    }

    @Override
    public Observable<RestBoundsModel> getCourseConfigByUrl(CourseModel courseModel) {
        return GolfAPI.getGolfCourseApi().getCourseConfig("https://" + courseModel.getCourseUrl() + "/app/courseconfig/" + TMUtil.getDeviceIMEI() + "/").map(list -> list.get(0));
    }

    @Override
    public Observable<List<PointOfInterest>> getPointsOfInterestByUrl(CourseModel courseModel) {
        return GolfAPI.getGolfCourseApi().getPointsOfInterestByUrl("https://" + courseModel.getCourseUrl() + "/app/pointsofinterest/");
    }

    @Override
    public Single<RestDeviceConfigModel> registerDevice(String type, String build) {
        return GolfAPI.getGolfCourseApi().registerDevice(type, build);
    }


    @Override
    public Single<List<CourseModel>> getCourses() {
        Gson gson = new Gson();
        return GolfAPI.getGolfApi().getCourses(TMUtil.getDeviceIMEI())
                .map(responseBodyResponse -> {
                    List<CourseModel> courseList = new ArrayList<>();
                    JsonReader reader = gson.newJsonReader(responseBodyResponse.body().charStream());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginArray();
                        CourseModel course = new CourseModel();
                        course.setCourseName(reader.nextString());
                        course.setCourseUrl(reader.nextString());
                        if (reader.peek() != JsonToken.NULL && reader.peek() != JsonToken.END_ARRAY) {
                            reader.beginObject();
                            PolygonModel polygon = new PolygonModel();
                            while (reader.hasNext()) {
                                String name = reader.nextName();
                                if (name.equals("type")) {
                                    polygon.setType(reader.nextString());
                                } else if (name.equals("coordinates")) {
                                    CoordinateModel coordinateModel = new CoordinateModel();
                                    List<LatLngModel> latLngList = new ArrayList<>();
                                    reader.beginArray();
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        reader.beginArray();
                                        LatLngModel latLng = new LatLngModel();
                                        latLng.setLon(reader.nextDouble());
                                        latLng.setLat(reader.nextDouble());
                                        reader.endArray();
                                        latLngList.add(latLng);
                                    }
                                    reader.endArray();
                                    reader.endArray();
                                    coordinateModel.setLatLng(latLngList);
                                    polygon.setCoordinate(coordinateModel);
                                }
                            }

                            reader.endObject();
                            course.setPolygon(polygon);

                        } else if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                        }
                        courseList.add(course);
                        reader.endArray();

                    }
                    reader.endArray();
                    return courseList;
                });
    }

}
