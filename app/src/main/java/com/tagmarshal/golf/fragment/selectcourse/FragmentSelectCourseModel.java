package com.tagmarshal.golf.fragment.selectcourse;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CoordinateModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.DataEntryStatus;
import com.tagmarshal.golf.rest.model.Disclaimer;
import com.tagmarshal.golf.rest.model.DisclaimerStatus;
import com.tagmarshal.golf.rest.model.LatLngModel;
import com.tagmarshal.golf.rest.model.PolygonModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public class FragmentSelectCourseModel implements FragmentSelectCourseContract.Model {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public Single<List<CourseModel>> getCourses() {
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


    @Override
    public void saveCourseConfig(String tag, CourseModel selectedCourse, boolean permanent) {
        String url = selectedCourse.getCourseUrl();
        PreferenceManager.getInstance().setDeviceTag(tag, url);
        PreferenceManager.getInstance().setCourse(url);
        PreferenceManager.getInstance().setCourseName(selectedCourse.getCourseName(), url);
        PreferenceManager.getInstance().saveCurrentCourse(selectedCourse, url);
        PreferenceManager.getInstance().setTag40Setup(true);

        GolfAPI.bindBaseCourseUrl(selectedCourse.getCourseUrl());
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    @Override
    public Single<RestDeviceConfigModel> registerDevice(String type, String build) {
        return GolfAPI.getGolfCourseApi().registerDevice(type, build);
    }

    @Override
    public void saveNearestCoursesConfigs(RestDeviceConfigModel configModel) {
        PreferenceManager.getInstance().setCourseGeoFences(configModel.getGeofences());
        PreferenceManager.getInstance().setCourseHoles(configModel.getHoles());
        PreferenceManager.getInstance().setCourseGeoZones(configModel.getSections());
        PreferenceManager.getInstance().setMapBounds(configModel.getCourse());

        Disclaimer disclaimer = configModel.getCourse().getDisclaimer();
        disclaimer.setStatus(DisclaimerStatus.NONE);
        disclaimer.setDataEntryStatus(DataEntryStatus.NONE);
        disclaimer.setLastShownTime(null);
        PreferenceManager.getInstance().setDisclaimer(disclaimer);
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
}
