package com.tagmarshal.golf.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.media.AudioManager;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.DebugConsoleActivity;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.eventbus.MovingCartEvent;
import com.tagmarshal.golf.eventbus.OfflineEvent;
import com.tagmarshal.golf.eventbus.OnlineEvent;
import com.tagmarshal.golf.manager.GameManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.*;
import com.tagmarshal.golf.util.GeofenceGeometry;
import com.tagmarshal.golf.util.GeofenceHelper;
import com.tagmarshal.golf.util.GpsResponse;
import com.tagmarshal.golf.util.SnapToPlayHelper;
import com.tagmarshal.golf.util.TMUtil;
import android.media.SoundPool;
import android.media.AudioAttributes;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

public class GolfService extends Service implements GolfServiceContract.View, LocationListener {

    private IBinder mBinder;
    private boolean serviceEnabled = false;

    private LocationManager locationService;

    private Location location;
    private Location previousLocation;

    private ArrayList<GolfCallback.GolfServiceListener> callbacks = new ArrayList<>();

    private GolfServicePresenter presenter;

    private double lat, lon = -1;

    private float latestAccuracy = -1f;

    private final String tag = "GolfService";

    private PowerManager.WakeLock wakeLock;
    private PowerManager mgr;
    private Location latestLocationWithRules;

    //Updated Geofence Requirements
    private LocationListener locationListener;
    private OnNmeaMessageListener nmeaListener;
    private SoundPool soundPool;
    private int buzzerSoundId;
    private Handler handler = new Handler();
    private Runnable soundRunnable;
    private boolean isPlayingAlertSound = false;
    GpsResponse gpsValidLocationResponse = null; //Used to store the response from the GPS processing, to be refactored

    @SuppressLint("MissingPermission")
    public void enableService() {
        if (serviceEnabled) {
            return;
        }
        
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1) // Number of simultaneous sounds
                .setAudioAttributes(audioAttributes)
                .build();

        // Load the alert buzzer sound
        buzzerSoundId = soundPool.load(this, R.raw.buzzer_sound, 1);
        float volume = 0.5f; // Adjust volume between 0.0 and 1.0, should be 0.5f

        // Initialize audio alert Handler
        handler = new Handler();
        soundRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlayingAlertSound) {
                    soundPool.play(buzzerSoundId, volume,volume, 0, 0, 1);
                    handler.postDelayed(this, 2000); // 1000ms = 1 second interval
                }
            }
        };
        
        locationService = (LocationManager) GolfApplication.context.getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            nmeaListener = new OnNmeaMessageListener() {

                public void onNmeaMessage(String message, long timestamp) {
                    
                    GpsResponse response = GeofenceHelper.ProcessGpsData(message, timestamp);
                    gpsValidLocationResponse = response;

                    if (response.ignore)
                    {
                        if (response.location != null)
                        {
                            location = null; //Ignore this location
                        }
                    } else
                    {
                        if (GeofenceHelper.allowGeofenceReload) {
                            configureGeofences();
                            CartControlHelper.configureCartControlGeofences();
                            GeofenceHelper.allowGeofenceReload = false;
                        }

                        gpsValidLocationResponse = response;

                        location = response.location;
                        latestAccuracy = location.getAccuracy();
                        lat = location.getLatitude();
                        lon = location.getLongitude();

                        if (response.inGeofence || CartControlHelper.inCartControlGeofence)
                        {
                            Boolean audible = false;
                            if (response.inGeofence) {
                                audible = response.audibleAlert;
                            } else {
                                audible = CartControlHelper.breachCartGeofenceIsAudible;
                            }
                            
                            if (response.enteredGeofence || CartControlHelper.inCartControlGeofence) //will be true once when entering geofence
                            {
                                //Entered Geofence
                                if (audible && !GeofenceHelper.ignoreAllGeofences) {
                                    if (!isPlayingAlertSound) startPlayingAlertSound();
                                }
                            } else
                            {   //In Geofence
                                if (audible && !GeofenceHelper.ignoreAllGeofences)
                                {
                                    if (!isPlayingAlertSound) startPlayingAlertSound();
                                } else
                                {
                                    //silence
                                    try {
                                        stopPlayingAlertSound();
                                    } catch (Exception e) {
                                        Log.d("GEOGT", "Error stopping sound: " + e.getMessage());
                                    }
                                }
                            }
                        };

                        if (response.inGeofence == false && CartControlHelper.inCartControlGeofence == false) 
                        {
                            if (response.leftGeofence) //event current being worked on
                            {
                            };

                            //silence
                            try {
                                stopPlayingAlertSound();
                            } catch (Exception e) {
                                Log.d("GEOGT", "Error stopping sound: " + e.getMessage());
                            }
                        }

                        DecimalFormat df = new DecimalFormat("#.######");
                        Log.d("GEOGT","Process:"+df.format(response.location.getLatitude())+","+df.format(response.location.getLongitude())+" (HP:"+ response.hdop+" VP:"+response.vdop+") Speed:"+response.location.getSpeed() + " SnrTop3Ave:"+df.format(GeofenceHelper.averageTop3SNR));
                    }
                }
            };
        }

        mgr = (PowerManager) getApplication().getApplicationContext().getSystemService(Context.POWER_SERVICE);
        presenter = new GolfServicePresenter(this, mgr);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationService.addNmeaListener(nmeaListener);
            }
        }

        serviceEnabled = true;
        presenter.startUpdatingLocationTimer();
        presenter.startSendLocationTimer();
        presenter.startRetrieveAlertsTimer();
        presenter.startHourUpdatePaceInfo();

        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tagMarshal::MyWakeLock");
        wakeLock.acquire();
    }

    // Start consistent sound intervals
    public void startPlayingAlertSound() {
        isPlayingAlertSound = true;
        handler.post(soundRunnable);
    }

    // Stop the sound loop
    public void stopPlayingAlertSound() {
        isPlayingAlertSound = false;
        //handler.post(soundRunnable);
        //handler.removeCallbacks(soundRunnable);
    }

    private void configureGeofences()
    {
        if (!GeofenceHelper.geofenceList.isEmpty()) return; //This only updates the list of Geofences once. Clear the list to allow it to refresh.
        if (PreferenceManager.getInstance().getGeoFences() == null) {
            return;
        }

        var geoFences = PreferenceManager.getInstance().getGeoFences();

        //For testing only
        Gson gson = new Gson();
        String json;
        var restBounds = PreferenceManager.getInstance().getMapBounds();
        json = gson.toJson(restBounds);
        Log.d("GEOGT",json);
        
        for (RestGeoFenceModel geofence: geoFences) {
            json= gson.toJson(geofence);
            Log.d("GEOGT",json);
            
            if (geofence.isCartControlActive() == false && geofence.isActive()) {  //Removed && geofence.isVisible() to allow for invisible geofences
                Log.d("GEOGT","Non-Cart & Active:"+json);
                var geometryCoordinates = geofence.getGeometry().getLatLngCoordinates();
                com.tagmarshal.golf.util.GeofenceGeometry newGeofence = new GeofenceGeometry();
                newGeofence.setAudible(geofence.playSound());
                newGeofence.setNotify(true); //Always notify and show dialogs
                newGeofence.setActive(geofence.isActive());
                newGeofence.setID(geofence.getId());
                for (LatLng latLng : geometryCoordinates) {
                    newGeofence.addPoint(latLng.latitude, latLng.longitude);
                }
                GeofenceHelper.geofenceList.add(newGeofence);
            }
        }

        //If thresholds are available and valid, call the setThresholds method
        try {
            RestBoundsModel mapBounds = PreferenceManager.getInstance().getMapBounds();
            if (mapBounds.getFixHdopThreshold() != 0)
            {
                GeofenceHelper.setThresholds(mapBounds.getFixHdopThreshold(), mapBounds.getFixSnrThreshold(), mapBounds.getGeoHdopThreshold(), mapBounds.getGeoSnrThreshold());
                Log.d("GEOGT", "------------- Thresholds set"+mapBounds.getFixHdopThreshold()+","+mapBounds.getFixSnrThreshold()+","+mapBounds.getGeoHdopThreshold()+","+mapBounds.getGeoSnrThreshold()+" ------------");
                if (GeofenceHelper.mockingLocation) {
                    GeofenceHelper.setThresholds(2.5,12,2.2,12);
                }
            }
        } catch (Exception e) {
            //Log.d("GEOGT", "Error setting threshholds: " + e.getMessage());
        }
    }

    private void startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getPackageName();
        String channelName = "GPS Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2222, notification);
    }

    public void restartSendLocationTimer() {
        presenter.startSendLocationTimer();
    }

    public void disableService() {
        if (!serviceEnabled) {
            Log.d(tag, "Service already stopped");
            return;
        }

        Log.d(tag, "callbacks cleared");
        callbacks.clear();
        presenter.onDestroy();
        wakeLock.release();
        serviceEnabled = false;
        presenter.stopUpdatingLocationTimer();
        presenter.stopPaceInfoTimer();
        presenter.stopRetrieveAlertsTimer();
        presenter.stopSendLocationTimer();
        
        disableLocationListener();
        stopPlayingAlertSound();
        handler.removeCallbacks(soundRunnable);
    }

    public void disableLocationListener()
    {
        if (locationService != null) {
            if (nmeaListener != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locationService.removeNmeaListener(nmeaListener);
                }
            }
            if (locationListener != null) {
                locationService.removeUpdates(locationListener);
            }
        }

        nmeaListener = null;
        locationListener = null;
        locationService = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new GolfServiceBinder();
            return mBinder;

        } else {
            return mBinder;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public boolean allowToSendFixes = true;

    @SuppressLint("MissingPermission")
    @Override
    public void readyToSendLocation() {

        if (location == null || getLastKnownMyLocationLatLng() == null) {
            Log.w(tag, "readyToSendLocation => location is null");
            return;
        }

        if (!allowToSendFixes) {
            presenter.writeToFile(LogFileConstants.fix_disallowed,
                    TMUtil.getTimeUTC(System.currentTimeMillis()),
                    String.valueOf(TMUtil.getBatteryLevel()),
                    String.valueOf(TMUtil.isOnline()),
                    String.valueOf(mgr.isInteractive()),
                    String.valueOf(lat),
                    String.valueOf(lon),
                    location.getAccuracy() + " old (" + latestAccuracy + ")"
            );
        }

        if (!allowToSendFixes && latestAccuracy < location.getAccuracy())
            return;

        try {
            for (GolfCallback.GolfServiceListener callback : callbacks) {
                try {
                    callback.onFocusBetweenMeAndHole();
                } catch (Exception e) {
                    Log.d(tag, "Call back error:" + e.getMessage());
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        double differenceDistance = 0;
        if (previousLocation != null)
        {
            differenceDistance = TMUtil.getDistanceByBounds(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()), new LatLng(lat, lon));
        } else
        {
            differenceDistance = 10; //Defaults to allow initial fix until previousLocation has been initialized
        }

        if (differenceDistance > 5) {
            if (location.getAccuracy() <= 6.0 && mgr.isInteractive()) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                if (!PreferenceManager.getInstance().isDeviceMobile()) {
                    EventBus.getDefault().postSticky(new MovingCartEvent());
                }
                if (TMUtil.getDeviceIMEI() != null) {
                    FixModel fix = new FixModel(
                            lat,
                            lon,
                            location.getAccuracy(),
                            location.getSpeed(),
                            location.getAccuracy(),
                            GeofenceHelper.averageTop3SNR,
                            TMUtil.getBatteryLevel(),
                            TMUtil.getTimeUTC(System.currentTimeMillis())
                    );
                    previousLocation = location;
                    
                    //Test logging Leon
                    Gson gson = new Gson();
                    String json;
                    
                    json = gson.toJson(fix);
                    
                    Log.d("GEOGT", "Fix:"+json);
                    
                    presenter.sendLocationFix(fix);
                } else {
                    Toast.makeText(this, "Please restart application and grant all permissions", Toast.LENGTH_SHORT).show();
                }
            } else if (location.getAccuracy() <= 10.0 && !mgr.isInteractive()) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                if (!PreferenceManager.getInstance().isDeviceMobile()) {
                    EventBus.getDefault().postSticky(new MovingCartEvent());
                }
                Log.d(tag, "readyToSendLocation =>  new Latitude is " + lat + "; new Longitude is " + lon);
                if (TMUtil.getDeviceIMEI() != null) {
                    FixModel fix = new FixModel(
                            lat,
                            lon,
                            location.getAccuracy(),
                            location.getSpeed(),
                            location.getAccuracy(),
                            GeofenceHelper.averageTop3SNR,
                            TMUtil.getBatteryLevel(),
                            TMUtil.getTimeUTC(System.currentTimeMillis())
                    );
                    previousLocation = location;

                    //Test logging Leon
                    Gson gson = new Gson();
                    String json;

                    json = gson.toJson(fix);
                    
                    presenter.sendLocationFix(fix);
                } else {
                    Toast.makeText(this, "Please restart application and grant all permissions", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(tag, "readyToSendLocation => location accuracy > 6.0");
                presenter.writeToFile(LogFileConstants.fix_inaccurate,
                        TMUtil.getTimeUTC(System.currentTimeMillis()),
                        String.valueOf(TMUtil.getBatteryLevel()),
                        String.valueOf(TMUtil.isOnline()),
                        String.valueOf(mgr.isInteractive()),
                        String.valueOf(lat),
                        String.valueOf(lon),
                        String.valueOf(location.getAccuracy())
                );
            }
        } else {
            Log.d(tag, "readyToSendLocation => location hasn't changed");
            presenter.writeToFile(LogFileConstants.fix_duplicate,
                    TMUtil.getTimeUTC(System.currentTimeMillis()),
                    String.valueOf(TMUtil.getBatteryLevel()),
                    String.valueOf(TMUtil.isOnline()),
                    String.valueOf(mgr.isInteractive()),
                    String.valueOf(lat),
                    String.valueOf(lon),
                    String.valueOf(location.getAccuracy())
            );
        }
    }


    public void bindCallback(GolfCallback.GolfServiceListener callback) {
        Log.d(tag, "bindCallback => callback has been bounded");
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("fas", " callbacks cleared");
        callbacks.clear();
        return super.onUnbind(intent);
    }

    public void unbindCallback(GolfCallback.GolfServiceListener callback) {
        Log.d(tag, "unbindCallback => callback has been unbounded");
        callbacks.remove(callback);
    }

    @Override
    public void readyToGetPaceInfo() {
        presenter.getPaceInfo();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callbacks.clear();
        startForeground();
        enableService();
    }

    @Override
    public void onUpdateLocation() {
        location = getLastKnownMyLocation();

        Timber.d("update location called");
        if (location == null) return;

        //if (!allowToSendFixes && latestAccuracy < location.getAccuracy())
        //    return;

        latestLocationWithRules = new Location(location);

        try {
            for (GolfCallback.GolfServiceListener callback : callbacks) {
                callback.onLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()), location.getAccuracy(), GeofenceHelper.currentlyInGeofence);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        presenter.getDistanceFromMeToHole(getLastKnownMyLocationLatLng(), GameManager.getHole(GameManager.getCurrentPlayHole()));
    }


    @Override
    public void onSentLocation() {
        Log.d(tag, "onSentLocation => location fix has been sent");
        if (serviceEnabled) {
            presenter.stopPaceInfoTimer();
            presenter.startPaceInfoTimer();
            EventBus.getDefault().postSticky(new OnlineEvent());
        }
    }

    public void refreshPaceInfo() {
        presenter.stopPaceInfoTimer();
        presenter.startPaceInfoTimer();
    }

    @Override
    public void onSendFail() {
        EventBus.getDefault().postSticky(new OfflineEvent(new Date()));
    }

    @Override
    public void readyToGetAlertAndGeoFences(long time) {
        if (time % 5 == 0)
            if (TMUtil.getDeviceIMEI() != null) {
                if(location != null) {
                    FixModel fix = new FixModel(
                            lat,
                            lon,
                            location.getAccuracy(),
                            location.getSpeed(),
                            location.getAccuracy(),
                            GeofenceHelper.averageTop3SNR,
                            TMUtil.getBatteryLevel(),
                            TMUtil.getTimeUTC(System.currentTimeMillis())
                    );
                    presenter.sendLocationFix(fix);
                }
                presenter.getGeofences(TMUtil.getDeviceIMEI()); // tbc
            } else {
                Toast.makeText(this, "Please restart application and grant all permissions", Toast.LENGTH_SHORT).show();
            }
    }

    public void getForceDistanceFromMeToHole() {
        presenter.getDistanceFromMeToHole(getLastKnownMyLocationLatLng(), GameManager.getHole(GameManager.getCurrentPlayHole()));
    }

    @SuppressLint("MissingPermission")
    public Location getLastKnownMyLocation() {
        return location;

//        Criteria criteria = new Criteria();
//        //Use FINE or COARSE (or NO_REQUIREMENT) here
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setPowerRequirement(Criteria.POWER_HIGH);
//        criteria.setAltitudeRequired(false);
//        criteria.setSpeedRequired(false);
//        criteria.setCostAllowed(true);
//        criteria.setBearingRequired(true);
//
//        //API level 9 and up
//        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
//        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
//        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
//        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
//        try {
//            return locationService.getLastKnownLocation(locationService.getBestProvider(criteria, true));
//        } catch (Throwable e) {
//            return null;
//        }
    }


    public LatLng getLastKnownLocationLatLngWithRules() {
        location = getLastKnownMyLocation();

        if (location == null) return null;

        if (!allowToSendFixes && latestAccuracy < location.getAccuracy())
            return new LatLng(latestLocationWithRules.getLatitude(), latestLocationWithRules.getLongitude());
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public LatLng getLastKnownMyLocationLatLng() {
        if (location == null)
            return null;
        else
            return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onGetPaceInfo(RestInRoundModel paceInfo) {
        Log.d(tag, "onGetPaceInfo => pace info response has been received");
        PreferenceManager.getInstance().setPaceInfo(paceInfo);
        Iterator<GolfCallback.GolfServiceListener> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            try {
                GolfCallback.GolfServiceListener callback = iterator.next();
                callback.onGetPaceInfo(paceInfo);
            } catch (Exception e) {
                Log.d(tag, "Call back error:" + e.getMessage());
            }
        }
    }

    @Override
    public void onGetHoleDistanceFromPoint(RestHoleDistanceModel distanceModel) {
        Iterator<GolfCallback.GolfServiceListener> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            try {
                GolfCallback.GolfServiceListener callback = iterator.next();
                callback.onGetHoleDistanceFromPoint(distanceModel);
            } catch (Exception e) {
                Log.d(tag, "Call back error:" + e.getMessage());
            }
        }
    }

    @Override
    public void onGetDistanceFromMeToHole(RestHoleDistanceModel distanceModel) {
        Iterator<GolfCallback.GolfServiceListener> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            try {
                GolfCallback.GolfServiceListener callback = iterator.next();
                callback.onCanUpdateDistanceFromMeToCurrentHole(distanceModel);
            } catch (Exception e) {
                Log.d(tag, "Call back error:" + e.getMessage());
            }
        }
    }

    @Override
    public void onGetAlerts(List<RestAlertModel> alertsList) {
        Iterator<GolfCallback.GolfServiceListener> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            try {
                GolfCallback.GolfServiceListener callback = iterator.next();
                callback.onGetAlerts(alertsList);
            } catch (Exception e) {
                Log.d(tag, "Call back error:" + e.getMessage());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public class GolfServiceBinder extends Binder {
        public GolfService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GolfService.this;
        }

    }

    @Override
    public void onDestroy() {
        disableService();
        soundPool.release();
        handler.removeCallbacks(soundRunnable);
        super.onDestroy();
    }
}
