package com.tagmarshal.golf.refresher;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.CustomJobIntentService;

import com.tagmarshal.golf.util.TMUtil;

import timber.log.Timber;

public class GeoRefresherService extends CustomJobIntentService {
    private GeoRefresherPresenter presenter;

    @Override
    public void onCreate() {
        super.onCreate();
        if (presenter == null) {
            presenter = new GeoRefresherPresenter();
        }
        Log.d(getClass().getName(), "GeoRefresherService => started");
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        presenter = null;

        Log.d(getClass().getName(), "Service destroyed!");
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            presenter.getGeofences(TMUtil.getDeviceIMEI());
            presenter.getGeoZones();
            presenter.getMapBounds();
        } catch (Throwable e) {
            Timber.d(e);
        }
    }
}
