package com.tagmarshal.golf.fragment.selectcourse;

import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface FragmentSelectCourseContract {

    interface View {
        void setCourses(List<CourseModel> courses);

        void getCoursesFail(String message);

        void onRegisterDeviceFailure(String message);

        void onSuccessSaveConfig();

        void showWaitDialog(boolean show);

        void onFailureRequest(String message);

        void onRegisterDeviceSuccess(String url);
    }

    interface Presenter {
        void onCreate();

        void onDestroy();

        void getCourses();

        Observable<RestDeviceConfigModel> registerDevice(String type, String build);

        void attachToCourse(CourseModel selectedItem, boolean permanent);

    }

    interface Model {
        Single<List<CourseModel>> getCourses();

        Single<RestDeviceConfigModel> registerDevice(String type, String build);

        Observable<List<RestHoleModel>> getHoles();

        Observable<List<RestGeoFenceModel>> getGeofences(String imei);

        Observable<List<RestGeoZoneModel>> getGeoZones();

        void saveCourseConfig(String tag, CourseModel selectedItem, boolean permanent);

        void saveNearestCoursesConfigs(RestDeviceConfigModel coursesMap);
    }
}
