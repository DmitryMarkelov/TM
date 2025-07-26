package com.tagmarshal.golf.activity.splash;

import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface SplashActivityContract {

    interface View {
        void onGetInRoundInfo(RestInRoundModel inRoundModel);

        void onGetInRoundInfoFailure(String message);

        void onSuccessSaveConfig();

        void onSaveConfigFailure(String message);
    }

    interface Model {
        Single<RestInRoundModel> checkInRound();

        Single<RestDeviceConfigModel> registerDevice(String type, String build);

        void saveCourseConfig(String tag, CourseModel selectedItem, boolean permanent);

        void saveNearestCoursesConfigs(RestDeviceConfigModel coursesMap);
    }

    interface Presenter {
        void checkInRound();

        Observable<RestDeviceConfigModel> registerDevice(String type, String build);

        void getCourseConfig(CourseModel course);
    }
}
