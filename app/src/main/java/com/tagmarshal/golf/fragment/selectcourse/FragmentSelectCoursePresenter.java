package com.tagmarshal.golf.fragment.selectcourse;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.LatLngModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentSelectCoursePresenter implements FragmentSelectCourseContract.Presenter {

    private final FragmentSelectCourseContract.Model model;
    private final FragmentSelectCourseContract.View view;
    private final CompositeDisposable disposable;

    public FragmentSelectCoursePresenter(FragmentSelectCourseContract.View view) {
        this.view = view;
        this.model = new FragmentSelectCourseModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void getCourses() {
        disposable.add(model.getCourses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.showWaitDialog(true))
                .toObservable()
                .doOnNext(view::setCourses)
                .doOnError(e -> view.getCoursesFail(e.getMessage()))
                .flatMapIterable(map -> map)
                .filter(course -> course.getPolygon() != null)
                .toList()
                .toObservable()
                .doOnTerminate(() -> view.showWaitDialog(false))
                .doOnNext(coordList -> PreferenceManager.getInstance().saveNearestCourses(coordList))
                .flatMapIterable(list -> list)
                .flatMap(course -> Observable.just(course)
                        .filter(courseModel -> {
                            ArrayList<LatLng> coordinates = new ArrayList<>();
                            for (LatLngModel latlng : courseModel.getPolygon().getCoordinate().getLatLng()) {
                                coordinates.add(new LatLng(latlng.getLat(), latlng.getLon()));

                            }
                            PolygonOptions polygon = new PolygonOptions()
                                    .addAll(coordinates)
                                    .clickable(false)
                                    .visible(false);
                            return false;
                        })).take(1)
                .subscribe(courseModel -> {
                            view.showWaitDialog(false);
                            attachToCourse(courseModel, false);
                        },
                        e -> {
                            view.onFailureRequest(e.getMessage());
                            view.showWaitDialog(false);
                        }
                )
        );
    }

    @Override
    public void attachToCourse(CourseModel selectedItem, boolean permanent) {
        String url = selectedItem.getCourseUrl();
        GolfAPI.bindBaseCourseUrl(url);

        disposable.add(Observable.just(PreferenceManager.getInstance().isDeviceVariable())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(__ -> view.showWaitDialog(true))
                .flatMap(isVariable -> {
                    String type = isVariable
                            ? "variable"
                            : PreferenceManager.getInstance().getDeviceType();
                    return registerDevice(type, Build.DISPLAY);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.isValid()) {
                                PreferenceManager.getInstance().clearGameData(url);
                                PreferenceManager.getInstance().clearCurrentCourse(url);
                                model.saveCourseConfig(response.getTag(), selectedItem, permanent);
                                view.onRegisterDeviceSuccess(url);
                                model.saveNearestCoursesConfigs(response);
                                view.onSuccessSaveConfig();
                            } else {
                                view.onRegisterDeviceFailure(response.getErrMessage());
                            }
                            view.showWaitDialog(false);
                        },
                        e -> {
                            view.showWaitDialog(false);
                            view.onRegisterDeviceFailure(e.getMessage());
                        }
                )
        );
    }

//    @SuppressLint("CheckResult")
//    @Override
//    public Observable<RestDeviceConfigModel> registerDevice(String type, String build) {
//        return model.registerDevice(type, build)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .toObservable();
//    }

    //Leon testing
    public Observable<RestDeviceConfigModel> registerDevice(String type, String build) {
        return model.registerDevice(type, build)
                .doOnSuccess(model -> {
                    // Log the object as JSON
                    String json = new Gson().toJson(model);

                    final int maxLogSize = 1000; // Keep it under 4k for safety
                    for (int i = 0; i <= json.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = Math.min((i + 1) * maxLogSize, json.length());
                        Log.d("GEOGT", json.substring(start, end));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).toObservable();
    }

    void logLongString(String tag, String message) {
        final int maxLogSize = 1000; // Keep it under 4k for safety
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = Math.min((i + 1) * maxLogSize, message.length());
            Log.d(tag, message.substring(start, end));
        }
    }
}
