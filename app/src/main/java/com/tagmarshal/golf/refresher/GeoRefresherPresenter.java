package com.tagmarshal.golf.refresher;

import android.util.Log;

import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.FileWorkerContract;
import com.tagmarshal.golf.data.FileWorkerModel;
import com.tagmarshal.golf.eventbus.MapBoundsEvent;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class GeoRefresherPresenter implements GeoRefresherContract.Presenter, FileWorkerContract.Presenter {


    private final CompositeDisposable disposable;
    private final GeoRefresherModel model;
    private final FileWorkerModel fileModel;
    private ArrayList<Boolean> updateLogs;

    public GeoRefresherPresenter() {
        this.model = new GeoRefresherModel();
        this.disposable = new CompositeDisposable();
        this.updateLogs = new ArrayList<>();
        this.fileModel = new FileWorkerModel();
    }


    @Override
    public void getGeofences(String imei) {

        disposable.add(model.getGeofences(imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestGeoFenceModel>>() {
                    @Override
                    public void onSuccess(List<RestGeoFenceModel> geoFenceModels) {
                        PreferenceManager.getInstance().setCourseGeoFences(geoFenceModels);
                        PreferenceManager.getInstance().setGeoFailed(false);
                        Log.d(getClass().getName(), "GET GEO FENCES => SUCCESS");
                        configUpdated(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        configUpdated(false);
                        PreferenceManager.getInstance().setGeoFailed(true);
                        Log.d(getClass().getName(), "GET GEO FENCES => FAIL");

                    }
                }));
    }

    @Override
    public void getGeoZones() {
        disposable.add(model.getGeoZones()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestGeoZoneModel>>() {
                    @Override
                    public void onSuccess(List<RestGeoZoneModel> geoZones) {
                        PreferenceManager.getInstance().setCourseGeoZones(geoZones);
                        PreferenceManager.getInstance().setGeoFailed(false);
                        Log.d(getClass().getName(), "GET GEO ZONES => SUCCESS");
                        configUpdated(true);

                    }

                    @Override
                    public void onError(Throwable e) {
                        configUpdated(false);
                        PreferenceManager.getInstance().setGeoFailed(true);
                        Log.d(getClass().getName(), "GET GEO ZONES => Fail");


                    }
                }));
    }

    @Override
    public void getMapBounds() {
        disposable.add(model.getMapBounds(TMUtil.getDeviceIMEI())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(i -> !i.isEmpty())
                .map(i -> i.get(0))
                .subscribe(
                        item -> {
                            PreferenceManager.getInstance().setMapBounds(item);
                            PreferenceManager.getInstance().setGeoFailed(true);
                            PreferenceManager.getInstance().clearLogoData();
                            Log.d(getClass().getName(), "GET MAP BOUNDS => SUCCESS");
                            configUpdated(true);


                        },
                        e -> {
                            configUpdated(false);
                            PreferenceManager.getInstance().setGeoFailed(false);
                            Log.d(getClass().getName(), "GET MAP BOUNDS => FAIL");
                        }
                )
        );
    }

    private void configUpdated(boolean isSuccess) {
        updateLogs.add(isSuccess);
        if (updateLogs.size() == 3) {
            for (Boolean success : updateLogs) {
                isSuccess = success;
                if (!isSuccess) return;
            }
            EventBus.getDefault().postSticky(new MapBoundsEvent());
            writeToFile(LogFileConstants.config_updated, TMUtil.getTimeUTC(System.currentTimeMillis()), String.valueOf(TMUtil.getBatteryLevel()), String.valueOf(TMUtil.isOnline()), String.valueOf(TMUtil.isActive()));
            updateLogs.clear();
        }
    }


    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive) {
        disposable.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(()->{
                    Timber.d("LOG COMPLETED");
                }, Timber::d)
        );;
    }

    @Override
    public void onDestroy() {
    }
}
