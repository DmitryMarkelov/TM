package com.tagmarshal.golf.activity.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;
import com.tagmarshal.golf.data.Maintenance;
import com.tagmarshal.golf.errors.TagNotFoundException;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CourseConfigModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.LatLngModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestGolfGeniusModel;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivityPresenter implements MainActivityContract.Presenter {

    private final MainActivityContract.View view;
    private final MainActivityContract.Model model;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable inactiveTimerDisposable;
    private Disposable callDrinksDisposable;
    private Disposable gotAlertDisposable;
    private Disposable sendMessageToClubTimer;
    private Disposable dimScreenTimer;
    private Disposable timerChangeCourse;
    private Disposable changeCourseNotificationDelayTimer;
    private Disposable inRoundFalseAccDisposable;
    private Disposable inRoundTrueAccDisposable;
    private Disposable maintenanceDisposable;
    private Context context;

    public MainActivityPresenter(MainActivityContract.View view, Context context) {
        this.view = view;
        model = new MainActivityModel(context);
        this.context = context;
    }

    @Override
    public void startInactiveStateTimer() {
        if (inactiveTimerDisposable != null && !inactiveTimerDisposable.isDisposed()) {
            inactiveTimerDisposable.dispose();
        }
        inactiveTimerDisposable = model.startInactiveStateTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        view.onInactiveState();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ERROR", e.toString());
                    }
                });
        disposables.add(inactiveTimerDisposable);
    }

    @Override
    public void startDimScrenTimer() {
        view.returnDimScreen();
        if (dimScreenTimer != null && !dimScreenTimer.isDisposed()) {
            dimScreenTimer.dispose();
        }
        dimScreenTimer = model.startDimScreenTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::dimScreen,
                        Throwable::getMessage
                );
        disposables.add(dimScreenTimer);
    }

    @SuppressLint("CheckResult")
    @Override
    public void gotAlert(String alertId) {
        gotAlertDisposable = model.gotAlert(alertId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
        disposables.add(gotAlertDisposable);
    }

    @Override
    public void sendFirebaseToken(Map<String, String> token) {
        disposables.add(model.sendRefreshToken(token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        __ -> Log.d(getClass().getName(), "firebase token sent from mainActivity"),
                        e -> Log.d(getClass().getName(), "firebase fail from main " + e.getMessage())
                )
        );
    }

    @Override
    public void updateFirmwareStatus(int progress, String status) {
        disposables.add(model.updateFirmwareStatus(TMUtil.getDeviceIMEI(), progress, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> view.onUpdateFirmwareStatus(s, status),
                        e -> view.onUpdateFirmwareStatus(e, status))
        );
    }

    @Override
    public void getAlerts() {
        disposables.add(model.getAlerts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestAlertModel>>() {
                    @Override
                    public void onSuccess(List<RestAlertModel> restAlertModels) {
                        view.onGetAlerts(restAlertModels);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @Override
    public void getGolfGeniusInfo(String round_id) {
        disposables.add(model.getGolfGeniusInfo(round_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RestGolfGeniusModel>() {
                    @Override
                    public void onSuccess(RestGolfGeniusModel golfGenius) {
                        golfGenius.setRoundId(round_id);
                        view.onGetGolfGeniusInfo(golfGenius);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }


    @Override
    public void startTimerToChangeCourse(CourseModel newCourse) {
        timerChangeCourse = Observable.timer(1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        s -> changeCourse(newCourse),
                        e -> Log.d(getClass().getName(), e.getMessage())
                );
        disposables.add(timerChangeCourse);
    }

    @Override
    public void reSaveNearestCourses() {
        disposables.add(model.getCourses()
                .subscribeOn(Schedulers.io())
                .toObservable()
                .flatMapIterable(map -> map)
                .filter(course -> course.getPolygon() != null)
                .toList()
                .toObservable()
                .doOnNext(coordList -> {
                    PreferenceManager.getInstance().saveNearestCourses(coordList);
                    Log.d("courses list", " SAVED");
                })
                .switchMap(i -> getCoursesConfigs())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> view.onReSaveSuccess(),
                        e -> Log.d("error", e.getMessage()))
        );
    }

    public Observable<Map<String, CourseConfigModel>> getCoursesConfigs() {
        return model.getNearestCourses()
                .flatMapIterable(courseModel -> courseModel)
                .filter(course -> !course.getCourseName().equals(PreferenceManager.getInstance().getCurrentCourseModel().getCourseName()))
                .flatMap(this::getCourseMapData)
                .toMap(courseModel -> courseModel.getCourseModel().getCourseName(), courseModel -> courseModel)
                .toObservable()
                .subscribeOn(Schedulers.io());
    }

    private Observable<CourseConfigModel> getCourseMapData(CourseModel course) {
        return Observable.zip(
                model.getGeoZonesByUrl(course)
                        .onErrorReturnItem(new ArrayList<>()),
                model.getGeofencesByUrl(course, TMUtil.getDeviceIMEI())
                        .onErrorReturnItem(new ArrayList<>()),
                model.getHolesByUrl(course)
                        .onErrorReturnItem(new ArrayList<>()),
                model.getCourseConfigByUrl(course)
                        .onErrorReturnItem(new RestBoundsModel()),
                model.getPointsOfInterestByUrl(course)
                        .onErrorReturnItem(new ArrayList<>()),
                (restGeoZoneModels, restGeoFenceModels, restHoleModels, restBoundsModel, pointOfInterests) ->
                        new CourseConfigModel(restGeoZoneModels, restGeoFenceModels, restHoleModels, restBoundsModel, pointOfInterests, course))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void changeCourse(CourseModel newCourse) {
        String _tag = PreferenceManager.getInstance().getDeviceTag(newCourse.getCourseUrl());
        if(_tag != null) {
            // 1st
            GolfAPI.bindBaseCourseUrl(newCourse.getCourseUrl());
            model.clearGameData(newCourse.getCourseUrl());
            model.saveDeviceTag(_tag, newCourse.getCourseUrl());
            view.clearFragments();

            // 2nd
            model.saveCourse(newCourse);

            // 3rd
            model.saveCourseConfig(new CourseConfigModel(
                    null,
                    null,
                    null,
                    null,
                    newCourse
            ));

            stopTimerToChangeCourse();
            stopChangeCourseDelay();
            ((Activity) context).finish();
        } else {
            disposables.add(registerDevice(PreferenceManager.getInstance().getDeviceType(), Build.DISPLAY)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(__ -> view.showPleaseWaitDialog(true))
                    .flatMap(response -> {
                        model.saveCourseConfig(new CourseConfigModel(
                                null,
                                null,
                                null,
                                null,
                                newCourse
                        ));

                        GolfAPI.bindBaseCourseUrl(newCourse.getCourseUrl());
                        model.clearGameData(newCourse.getCourseUrl());
                        model.saveDeviceTag(response.getTag(), newCourse.getCourseUrl());
                        view.clearFragments();

                        model.saveCourse(newCourse);

                        return Observable.just(response);
                    })
                    .doOnTerminate(() -> {
                        view.showPleaseWaitDialog(false);
                        view.dissmissNewCourseDialog();
                    })
                    .subscribe(
                            onSuccess -> {
                                model.saveCourseConfig(new CourseConfigModel(
                                        null,
                                        null,
                                        null,
                                        null,
                                        newCourse
                                ));

                                view.sendFirebaseToken();
                                stopTimerToChangeCourse();
                                stopChangeCourseDelay();
                                ((Activity) context).finish();
                            },
                            e -> {
                                stopTimerToChangeCourse();
                                stopChangeCourseDelay();
                                Timber.tag(getClass().getName()).d(e);
                                if (e instanceof TagNotFoundException) {
                                    view.onFail(e.getMessage());
                                }
                                ((Activity) context).finish();
                            },
                            () -> Timber.tag(getClass().getName()).d("action finished")
                    )
            );
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public Observable<RestDeviceConfigModel> registerDevice(String type, String build) {
        return model.registerDevice(type, build)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable();
    }

    @Override
    public void stopTimerToChangeCourse() {
        if (timerChangeCourse != null && !timerChangeCourse.isDisposed()) {
            timerChangeCourse.dispose();
        }
    }

    @Override
    public void checkForNewCourseEnter(LatLng location) {
        disposables.add(model.getCurrentCourse()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(currentCourse -> checkInCourse(location, currentCourse) ? null : currentCourse)
                .flatMap(courseModel -> model.getNearestCourses()
                        .flatMapIterable(
                                item -> item
                        )
                        .filter(courseModels -> !courseModel.equals(courseModels)))
                .filter(courseModel -> checkInCourse(location, courseModel))
                .take(1)
                .subscribe(
                        view::showCourseChangeAlert,
                        e -> {
                            if (e instanceof NullPointerException) {
//
                            } else {
                                view.onFail(e.getMessage());
                            }
                        }
                ));

    }

    private boolean checkInCourse(LatLng location, CourseModel courseModel) {
        if(courseModel.getPolygon() == null)
            return false;
        ArrayList<LatLng> coordinates = new ArrayList<>();
        for (LatLngModel latlng : courseModel.getPolygon().getCoordinate().getLatLng()) {
            coordinates.add(new LatLng(latlng.getLat(), latlng.getLon()));

        }
        PolygonOptions polygon = new PolygonOptions()
                .addAll(coordinates)
                .clickable(false)
                .visible(false);
        return PolyUtil.containsLocation(location.latitude, location.longitude, polygon.getPoints(), false);
    }

    @Override
    public void callDrinksCart(String imei) {
        callDrinksDisposable = model.callDrinksCart(imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        view.showPleaseWaitDialog(true); // This should be on the main thread also
                    }
                })
                .subscribeWith(
                        new DisposableSingleObserver<Response<ResponseBody>>() {
                            @Override
                            public void onSuccess(Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    view.onSuccessDrinksCartCall();
                                } else {
                                    try {
                                        view.onFail(response.errorBody().string());
                                    } catch (Exception e) {
                                        view.onFail(e.getMessage());
                                    }
                                }
                            }

                            //
                            @Override
                            public void onError(Throwable e) {
                                view.onFail(e.toString());
                            }
                        });
        disposables.add(callDrinksDisposable);
    }

    @Override
    public void startTimerSendToClubNotification(Map<String, String> body) {
        if (sendMessageToClubTimer != null && !sendMessageToClubTimer.isDisposed()) {
            return;
        }
        sendMessageToClubTimer = Observable.timer(15, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> Log.d(getClass().getName(), "timerToSendClubNotification => started"))
                .subscribe(
                        __ -> sendNotificationToClub(body),
                        throwable -> Log.d(getClass().getName(), throwable.getMessage())
                );
        disposables.add(sendMessageToClubTimer);
    }

    @Override
    public void sendNotificationToClub(Map<String, String> body) {
        disposables.add(
                model.sendMessageToClub(body)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                __ -> Log.d(getClass().getName(), "sendClubNotification => success"),
                                throwable -> Log.d(getClass().getName(), "sendClubNotification => fail =" + throwable.getMessage())
                        )
        );
    }

    @Override
    public void startChangeCourseDelay() {
        stopChangeCourseDelay();
        //todo change to 10
        changeCourseNotificationDelayTimer = Observable.timer(10, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        __ -> view.changeCourseNotificationDelayStatus(true),
                        e -> Log.d(getClass().getName(), "startChangeCourseDelay: " + e.getMessage())
                );
        disposables.add(changeCourseNotificationDelayTimer);
    }

    private void disposeAccDelays() {
        if (inRoundFalseAccDisposable != null && !inRoundFalseAccDisposable.isDisposed())
            inRoundFalseAccDisposable.dispose();

        if (inRoundTrueAccDisposable != null && !inRoundTrueAccDisposable.isDisposed())
            inRoundTrueAccDisposable.dispose();
    }

    @Override
    public void startInRoundFalseAccDelay() {
        if (inRoundFalseAccDisposable != null && !inRoundFalseAccDisposable.isDisposed()) {
            inRoundFalseAccDisposable.dispose();
        }
        //todo replace 5 sec. new rule
        startInRoundTrueAccDelay();
        inRoundFalseAccDisposable = Single.timer(TMUtil.isQuestDevice() ? 1 : 5, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        view.onInRoundFalseInteractionDelayTick();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
        disposables.add(inRoundFalseAccDisposable);
    }

    @Override
    public void startInRoundTrueAccDelay() {
        if (inRoundTrueAccDisposable != null && !inRoundTrueAccDisposable.isDisposed()) {
            inRoundTrueAccDisposable.dispose();
        }
        inRoundTrueAccDisposable = Single.timer(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        view.onInRoundTrueInteractionDelayTick();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
        disposables.add(inRoundTrueAccDisposable);
    }

    @Override
    public void startMaintenance() {
        Maintenance maintenance = PreferenceManager.getInstance().getMaintenance();
        Log.d("gagasgasga", "STARTED CASE");
        Log.d("gagasgasga", "CASE" + String.valueOf(maintenance != null));

        if (maintenance != null) {

            if (maintenance.getToTime() != 0 && maintenance.getToTime() < System.currentTimeMillis()) {
                Log.d("taaaaaaaaag", " FIRST CASE");
                PreferenceManager.getInstance().clearMaintenance();
            } else if (maintenance.getFromTime() < System.currentTimeMillis() || maintenance.getFromTime() == System.currentTimeMillis()) {
                Log.d("taaaaaaaaag", "SECOND CASE");
                view.showMaintenanceFragment();
            } else if (maintenance.getFromTime() > System.currentTimeMillis()) {
                Log.d("taaaaaaaaag", "THIRD CASE");
                if (maintenanceDisposable != null && !maintenanceDisposable.isDisposed()) {
                    maintenanceDisposable.dispose();
                    Log.d("taaaaaaaaaaag", "DISPOSED CASE");
                }
                disposables.add(maintenanceDisposable = Observable.timer(maintenance.getFromTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> startMaintenance(), throwable -> {
                            Log.d("gagasgasga", "CASE" + throwable.getMessage());
                        }));
            } else {
                Log.d("gagasgasga", "CASE ELSE");

            }
        }
    }

    @Override
    public void stopMaintenance() {
        if (maintenanceDisposable != null && !maintenanceDisposable.isDisposed()) {
            maintenanceDisposable.dispose();
        }
        PreferenceManager.getInstance().clearMaintenance();
        view.openStartScreen();
    }

    @Override
    public void stopChangeCourseDelay() {
        if (changeCourseNotificationDelayTimer != null && !changeCourseNotificationDelayTimer.isDisposed()) {
            changeCourseNotificationDelayTimer.dispose();
        }
    }

    @Override
    public void stopInactiveTimer() {
        if (inactiveTimerDisposable != null && !inactiveTimerDisposable.isDisposed()) {
            inactiveTimerDisposable.dispose();
        }
    }

    public void dispose() {
        if (inactiveTimerDisposable != null && !inactiveTimerDisposable.isDisposed()) {
            inactiveTimerDisposable.dispose();
            inactiveTimerDisposable = null;
        }
        if (dimScreenTimer != null && !dimScreenTimer.isDisposed()) {
            dimScreenTimer.dispose();
        }
        disposeAccDelays();
        disposables.dispose();
    }
}
