package com.tagmarshal.golf.activity.splash;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Single;

public class SplashActivityModel implements SplashActivityContract.Model {
    @Override
    public Single<RestDeviceConfigModel> registerDevice(String type, String build) {
        return GolfAPI.getGolfCourseApi().registerDevice(type, build);
    }

    @Override
    public void saveCourseConfig(String tag, CourseModel selectedItem, boolean permanent) {
        String url = selectedItem.getCourseUrl();
        PreferenceManager.getInstance().setDeviceTag(tag, url);
        PreferenceManager.getInstance().setCourse(url);
        PreferenceManager.getInstance().setCourseName(selectedItem.getCourseName(), url);
        PreferenceManager.getInstance().saveCurrentCourse(selectedItem, url);
        PreferenceManager.getInstance().setTag40Setup(true);

        GolfAPI.bindBaseCourseUrl(selectedItem.getCourseUrl());
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    @Override
    public void saveNearestCoursesConfigs(RestDeviceConfigModel configModel) {
        PreferenceManager.getInstance().setCourseGeoFences(configModel.getGeofences());
        PreferenceManager.getInstance().setCourseHoles(configModel.getHoles());
        PreferenceManager.getInstance().setCourseGeoZones(configModel.getSections());
        PreferenceManager.getInstance().setMapBounds(configModel.getCourse());
    }

    @Override
    public Single<RestInRoundModel> checkInRound() {
        return GolfAPI.getGolfCourseApi().getDeviceInfo();
    }
}
