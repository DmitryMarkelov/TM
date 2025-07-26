package com.tagmarshal.golf.service.firebase;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tagmarshal.golf.constants.FirebaseConstants;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.Maintenance;
import com.tagmarshal.golf.eventbus.FirebaseGetAlerts;
import com.tagmarshal.golf.eventbus.FirebaseTeeRefresh;
import com.tagmarshal.golf.eventbus.GeoFenceEvent;
import com.tagmarshal.golf.eventbus.GeoFencesUpdateEvent;
import com.tagmarshal.golf.eventbus.MapBoundsEvent;
import com.tagmarshal.golf.eventbus.OnSuccessReregister;
import com.tagmarshal.golf.eventbus.RedownloadSlideShow;
import com.tagmarshal.golf.eventbus.SetMaintenanceEvent;
import com.tagmarshal.golf.eventbus.StopMaintenanceEvent;
import com.tagmarshal.golf.eventbus.UserInteractionEvent;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.refresher.GeoRefresherService;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class FirebaseMessageService extends FirebaseMessagingService implements FirebaseContract.View {
    private final String TAG = getClass().getName();
    private FirebasePresenter presenter;
    private PowerManager mgr;

    @Override
    public void onCreate() {
        super.onCreate();
        mgr = (PowerManager) getApplication().getApplicationContext().getSystemService(Context.POWER_SERVICE);
        presenter = new FirebasePresenter(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().get("action") == null || remoteMessage.getData().get("ignore") != null)
            return;
        String action = remoteMessage.getData().get("action");
        switch (action) {

            case FirebaseConstants.update_map: {
                presenter.refreshMap();
                break;
            }

            case FirebaseConstants.get_alerts: {
                EventBus.getDefault().postSticky(new FirebaseGetAlerts());
                break;
            }

            case FirebaseConstants.get_config: {
                ComponentName comp = new ComponentName(getApplicationContext().getPackageName(),
                        GeoRefresherService.class.getName());
                GeoRefresherService.enqueueWork(getApplicationContext(), comp, 50, new Intent());
                break;
            }

            case FirebaseConstants.send_logs: {
                if (remoteMessage.getData().get("limit") != null) {
                    presenter.sendSupportLogs(remoteMessage.getData().get("limit"));
                } else {
                    presenter.sendSupportLogs();
                }
                break;
            }

            case FirebaseConstants.update_tee_time: {
                EventBus.getDefault().postSticky(new FirebaseTeeRefresh());
                break;
            }

            case FirebaseConstants.get_advertisements: {
                presenter.getAdvertisements();
                break;
            }

            case FirebaseConstants.re_register: {
                presenter.reregister(remoteMessage.getData().get("unit"), remoteMessage.getData().get("type"), Build.DISPLAY);
                break;
            }

            case FirebaseConstants.get_courses: {
                presenter.getCourses();
                break;
            }

            case FirebaseConstants.get_slides: {
                EventBus.getDefault().postSticky(new RedownloadSlideShow());
                break;
            }

            case FirebaseConstants.set_maintenance: {
                String toTime = remoteMessage.getData().get("to_time");
                PreferenceManager.getInstance().saveMaintenance(new Maintenance(remoteMessage.getData().get("from_time"), toTime));
                EventBus.getDefault().postSticky(new SetMaintenanceEvent());
                break;
            }

            case FirebaseConstants.stop_maintenance: {
                EventBus.getDefault().postSticky(new StopMaintenanceEvent());
                break;
            }

            case FirebaseConstants.get_holes: {
                presenter.getHoles();
                break;
            }

            case FirebaseConstants.download_firmware: {
                String fileName = remoteMessage.getData().get("file");
                presenter.downloadFirmware(fileName);
                break;
            }

            case FirebaseConstants.get_geofences: {
                String ids = remoteMessage.getData().get("ids");
                Gson gson = new Gson();
                ArrayList<String> idList = gson.fromJson(ids, new TypeToken<List<String>>() {
                }.getType());
                if (idList != null && idList.size() > 0) {
                    presenter.getGeofenceById(idList);
                }
            }

            case FirebaseConstants.delete_geofences: {
                String ids = remoteMessage.getData().get("ids");
                Gson gson = new Gson();
                ArrayList<String> idList = gson.fromJson(ids, new TypeToken<List<String>>() {
                }.getType());
                if (idList != null && idList.size() > 0) {
                    List<RestGeoFenceModel> geoFenceModels = PreferenceManager.getInstance().getGeoFences();
                    if (geoFenceModels != null && geoFenceModels.size() > 0) {
                        for (String id : idList) {
                            for (RestGeoFenceModel model : geoFenceModels) {
                                if (id.equals(model.getId())) {
                                    geoFenceModels.remove(model);
                                    break;
                                }
                            }
                        }
                        EventBus.getDefault().postSticky(new GeoFencesUpdateEvent());
                    }
                    PreferenceManager.getInstance().setCourseGeoFences(geoFenceModels);
                }
            }
        }
        if (!TMUtil.isQuestDevice()) {
            EventBus.getDefault().postSticky(new UserInteractionEvent());
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {

            Map<String, String> tokenBody = new HashMap<>();
            tokenBody.put("token", token);
            presenter.sendRefreshToken(tokenBody);
        } else {
            onError("Do not have phone permission to refresh Firebase Token. Please give permission on splash screen and restart the app ");
        }
    }

    @Override
    public void onGeoFenceGet() {
        EventBus.getDefault().postSticky(new GeoFenceEvent());
    }

    @Override
    public void onSuccessRefreshToken() {
        presenter.writeToFile(LogFileConstants.token_updated,
                TMUtil.getTimeUTC(System.currentTimeMillis()),
                String.valueOf(TMUtil.getBatteryLevel()),
                String.valueOf(TMUtil.isOnline()),
                String.valueOf(mgr.isInteractive())
        );
        Log.d(TAG, "token sent successful");
    }

    @Override
    public void onError(String message) {
        Timber.d(message);
        FirebaseCrashlytics.getInstance().log(message);
    }

    @Override
    public void onSuccessMapRefresh() {
        EventBus.getDefault().postSticky(new MapBoundsEvent());
    }

    @Override
    public void onSuccessReregister() {
        EventBus.getDefault().postSticky(new OnSuccessReregister());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
