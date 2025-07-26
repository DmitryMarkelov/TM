package com.tagmarshal.golf.service;

import android.annotation.SuppressLint;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.FileWorkerContract;
import com.tagmarshal.golf.data.FileWorkerModel;
import com.tagmarshal.golf.eventbus.FirebaseGetAlerts;
import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.CartControlHelper;
import com.tagmarshal.golf.util.GeofenceHelper;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class GolfServicePresenter implements GolfServiceContract.Presenter, FileWorkerContract.Presenter {

    private final String tag = "GolfServicePresenter";

    private final GolfServiceContract.View view;
    private final GolfServiceContract.Model model = new GolfServiceModel();

    private final CompositeDisposable disposables =
            new CompositeDisposable();
    private final PowerManager mgr;
    private FileWorkerModel fileModel;

    private Disposable sendLocationDisposable;
    private Disposable updatingLocationDisposable;
    private Disposable paceInfoDisposable;

    public GolfServicePresenter(GolfServiceContract.View view, PowerManager mgr) {
        this.view = view;
        this.mgr = mgr;
        this.fileModel = new FileWorkerModel();
    }

    @SuppressLint("CheckResult")
    @Override
    public void startSendLocationTimer() {
        if (sendLocationDisposable != null) {
            Log.d(tag, "Send location timer already started");
            if (!sendLocationDisposable.isDisposed())
                sendLocationDisposable.dispose();
            sendLocationDisposable = null;
        }

        Log.d(tag, "Send location timer has been started");

        sendLocationDisposable = model.startSendLocationTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.readyToSendLocation();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(tag, "Send location timer error = " + e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposables.add(sendLocationDisposable);
    }

    @Override
    public void stopSendLocationTimer() {
        if (sendLocationDisposable != null && !sendLocationDisposable.isDisposed()) {
            sendLocationDisposable.dispose();
            disposables.remove(sendLocationDisposable);
            sendLocationDisposable = null;
        }
    }

    @Override
    public void startUpdatingLocationTimer() {
        if (updatingLocationDisposable != null) return;

        updatingLocationDisposable = model.startUpdatingLocationTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.onUpdateLocation();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposables.add(updatingLocationDisposable);
    }

    @Override
    public void startHourUpdatePaceInfo() {
        disposables.add(
                model.startHourUpdatePaceInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                __ -> {
                                    getPaceInfo();
                                    Log.d(getClass().getName(), "HOUR_UPDATE");
                                },
                                e -> Log.d(getClass().getName(), e.getMessage())
                        )
        );
    }

    @Override
    public void getGeofences(String imei) {
        if (PreferenceManager.getInstance().getGeoFences() != null) {
            return;
        }
        disposables.add(model.getGeofences(imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestGeoFenceModel>>() {
                    @Override
                    public void onSuccess(List<RestGeoFenceModel> geoFenceModels) {
                        PreferenceManager.getInstance().setCourseGeoFences(geoFenceModels);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @Override
    public void stopUpdatingLocationTimer() {
        if (updatingLocationDisposable != null && !updatingLocationDisposable.isDisposed()) {
            updatingLocationDisposable.dispose();
            disposables.remove(updatingLocationDisposable);
            updatingLocationDisposable = null;
        }
    }


    @Override
    public void stopPaceInfoTimer() {
        if (paceInfoDisposable != null) {
            if (!paceInfoDisposable.isDisposed())
                paceInfoDisposable.dispose();

            disposables.remove(paceInfoDisposable);
            paceInfoDisposable = null;
        }
    }

    @Override
    public void startRetrieveAlertsTimer() {
        disposables.add(model.startGetAlertsTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.readyToGetAlertAndGeoFences(aLong);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void stopRetrieveAlertsTimer() {

    }

    @Override
    public void getDistanceFromMeToHole(LatLng myLocation, RestHoleModel holeModel) {
        if (myLocation != null && holeModel != null) {
            disposables.add(model.getDistanceFromMeToHole(myLocation, holeModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<RestHoleDistanceModel>() {
                        @Override
                        public void onSuccess(RestHoleDistanceModel restHoleDistanceModel) {
                            view.onGetDistanceFromMeToHole(restHoleDistanceModel);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    }));
        }
    }

    @Override
    public void sendLocationFix(FixModel fix) {
        disposables.add(model.sendLocationFix(fix)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody s) {
                        try {
                            String responseText = s.string();
                            if (responseText.equals("OK")) {
                                view.onSentLocation();
                                writeToFile(
                                        LogFileConstants.fix_sent,
                                        fix.getDate(),
                                        String.valueOf(fix.getBattery()),
                                        String.valueOf(TMUtil.isOnline()),
                                        String.valueOf(mgr.isInteractive()),
                                        String.valueOf(fix.getLat()),
                                        String.valueOf(fix.getLon()),
                                        String.valueOf(fix.getAccuracy())
                                );
                                if (!PreferenceManager.getInstance().getFailedFixes().isEmpty()) {
                                    sendFailedFixes();
                                }

                            }
                        } catch (Exception e) {
                            Log.d(tag, "locationSent=>  parse failed" + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(tag, "locationSent=>  fail " + e.getMessage());
                        writeToFile(
                                LogFileConstants.fix_failed,
                                fix.getDate(),
                                String.valueOf(fix.getBattery()),
                                String.valueOf(TMUtil.isOnline()),
                                String.valueOf(mgr.isInteractive()),
                                String.valueOf(fix.getLat()),
                                String.valueOf(fix.getLon()),
                                String.valueOf(fix.getAccuracy())
                        );
                        PreferenceManager.getInstance().addFailedFix(fix);
                        view.onSendFail();
                    }
                }));
    }

    private void sendFailedFixes() {
        Log.d(tag, "failed fixes send started");
        disposables.add(model.sendFailedFixes(
                                PreferenceManager.getInstance().getFailedFixes()
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(__ -> {
                                    Log.d(tag, "failed fixes send => success");
                                    writeToFile(
                                            LogFileConstants.fixes_sent,
                                            TMUtil.getTimeUTC(System.currentTimeMillis()),
                                            String.valueOf(TMUtil.getBatteryLevel()),
                                            String.valueOf(TMUtil.isOnline()),
                                            String.valueOf(mgr.isInteractive())
                                    );
                                    PreferenceManager.getInstance().clearFailedFixes();
                                },
                                e -> {
                                    Log.d(tag, "failed fixes => failed" + e.getMessage());
                                    writeToFile(
                                            LogFileConstants.fixes_failed,
                                            TMUtil.getTimeUTC(System.currentTimeMillis()),
                                            String.valueOf(TMUtil.getBatteryLevel()),
                                            String.valueOf(TMUtil.isOnline()),
                                            String.valueOf(mgr.isInteractive())
                                    );
                                }
                        )
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
    public void startPaceInfoTimer() {
        disposables.add(paceInfoDisposable = model.startPaceInfoTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> Log.d(getClass().getName(), "getDeviceInfoTimer => started"))
                .subscribe(
                        __ -> {
                            Log.d(getClass().getName(), "getDeviceInfoTimer => 10 seconds end");
                            getPaceInfo();

                        },
                        e -> Log.d(getClass().getName(), e.getMessage())
                )

        );
    }

    @Override
    public void getPaceInfo() {
        Log.d(getClass().getName(), "getDeviceInfo = > started");
        disposables.add(model.getPaceInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RestInRoundModel>() {
                    @Override
                    public void onNext(RestInRoundModel restInRoundModel) {
                        Log.d("GEOGT","++++++++++++++GolfServicePresenter - restInRoundModel ignoreGeofences="+restInRoundModel.isIgnoreGeofence());
                        GeofenceHelper.ignoreAllGeofences = restInRoundModel.isIgnoreGeofence();
                        if(restInRoundModel != null && restInRoundModel.isMessagesPending()) {
                            EventBus.getDefault().postSticky(new FirebaseGetAlerts());
                        }
                        view.onGetPaceInfo(restInRoundModel);
                        EventBus.getDefault().postSticky(new RoundInfoEvent(restInRoundModel));
                        writeToFile(
                                LogFileConstants.round_info,
                                TMUtil.getTimeUTC(System.currentTimeMillis()),
                                String.valueOf(TMUtil.getBatteryLevel()),
                                String.valueOf(TMUtil.isOnline()),
                                String.valueOf(mgr.isInteractive())
                        );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("GEOGT", "Error collecting pace info------------------");
                    }

                    @Override
                    public void onComplete() {
                    }
                }));
    }


    @Override
    public void onDestroy() {
        Log.d("SERVICE", "SERVICE DESTROYED");
        disposables.dispose();
        fileModel = null;
    }

    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive) {
        if (fileModel != null) {
            disposables.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        Timber.d("LOG COMPLETED");
                    }, Timber::d)
            );
        }
    }

    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive, String lat, String lon, String accuracy) {
        if (fileModel != null) {
            if (tag.equals(LogFileConstants.fix_inaccurate)) {
                disposables.add(fileModel.writeInAccuracyToFile(tag, time, battery, isOnline, inActive, lat, lon, accuracy)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Timber.d("LOG COMPLETED");
                        }, Timber::d)
                );
            } else {
                disposables.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive, lat, lon, accuracy)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Timber.d("LOG COMPLETED");
                        }, Timber::d)
                );
            }
        }

    }
}
