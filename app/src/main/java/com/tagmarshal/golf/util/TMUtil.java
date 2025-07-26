package com.tagmarshal.golf.util;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.DeviceType;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.TMDevice;
import com.tagmarshal.golf.view.TMTextView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class TMUtil {


    private static boolean debug = true;
    private static boolean globalIsLate = false;


    private static int topMargin;

    public static void setTopMargin(int margin) {
        topMargin = margin;
    }

    public static int getTopMargin() {
        return topMargin;
    }

    public static int getPxFromDp(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                context.getResources().getInteger(dp),
                context.getResources().getDisplayMetrics()
        );
    }

    private static LruCache<Integer, BitmapDescriptor> cache;

    private static LruCache<Integer, BitmapDescriptor> getCache() {
        if (cache == null) {
            cache = new LruCache<Integer, BitmapDescriptor>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8));
        }
        return cache;
    }


    public static void iniLogs() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/logFileToSendUs.txt");
            Runtime.getRuntime().exec("logcat -f" + file.getAbsolutePath());
        } catch (Exception e) {

            Log.d("tab", e.getMessage());
        }
    }

    public static boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) GolfApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } else {
            return false;
        }
    }

    public static boolean isActive() {
        PowerManager mgr = (PowerManager) GolfApplication.context.getSystemService(Context.POWER_SERVICE);
        if (mgr != null)
            return mgr.isInteractive();
        else
            return false;
    }

    @SuppressLint("MissingPermission")
    public static void vibrateAndMakeNoiseDevice(Context context) {
//        return;
////        try {
////            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////            Ringtone r = RingtoneManager.getRingtone(context, notification);
////            if(r.getTitle(context).equals("Default (None)") || r.getTitle(context).equalsIgnoreCase("ringtones")) {
////                r = RingtoneManager.getRingtone(context, Uri.parse("content://media/internal/audio/media/29"));
////            }
////            r.play();
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////
////        // Vibrate for 500 milliseconds
////        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE));
////        } else {
////            v.vibrate(750);
////        }
    }

    public static int getNavigationHeight(WindowManager windowManager) {
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;

        return realHeight - displayHeight;
    }

    //THERE IS STATIC IMEI
    @SuppressLint("MissingPermission")
    public static String getDeviceIMEI() {
        if(true) return "352264101073787"; // test device
        if (debug) return "864890031335291"; //full game
        if (debug) return "864890031335283"; //half game

        String deviceUniqueIdentifier = null;
        try {
            TelephonyManager tm = (TelephonyManager) GolfApplication.context.getSystemService(Context.TELEPHONY_SERVICE);

            if (Build.VERSION.SDK_INT >= 26) {
                if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                    deviceUniqueIdentifier = tm.getMeid();
                } else if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                    deviceUniqueIdentifier = tm.getImei();
                }
            } else {
                deviceUniqueIdentifier = tm.getDeviceId();
            }
            if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length() || deviceUniqueIdentifier.isEmpty()) {
                deviceUniqueIdentifier = Settings.Secure.getString(GolfApplication.context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        } catch (SecurityException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return deviceUniqueIdentifier;
    }

    public static boolean isCharging(Context context) {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = context.registerReceiver(null, filter);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
                return true;
            }
        } catch (Exception e) {
            //
        }

        try {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.isCharging();
        } catch (Exception e) {
            //
        }
        return false;
    }

    private static String getDeviceModel() {
        return Build.MODEL;
    }

    public static int getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = GolfApplication.context.registerReceiver(null, ifilter);
        try {
            float batteryPct = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            return (int) batteryPct;
        } catch (Throwable e) {
            return 1;
        }
    }

    public static String getHourTimeFromSecs(int secs) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String hours = sdf.format(new Date(secs * 1000));
        return hours;
    }

    public static String getHourTimeFromSecs(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(time);
    }

    public static String getMinutesTimeFromSecs(int secs) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String minutes = sdf.format(new Date(secs * 1000));
        return minutes;
    }

    public static String getMinutesFromSecs(int secs) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String minutes = sdf.format(new Date(secs * 1000));
        return minutes;
    }

    public static String getHourAndSecondsTimeFromSecs(int secs) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String time = sdf.format(new Date(secs * 1000));
        return time;
    }

    public static int covertSecToMinutes(int secs) {
        return secs / 60;
    }

    public static String convertSecsToMinutesString(int secs) {
        int minutes = secs / 60;
        int seconds = secs % 60;
        String time = (minutes) + ":" + (seconds > 9 ? seconds : ("0" + seconds));
        return time;
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        BitmapDescriptor result = getCache().get(vectorResId);
        if (result == null) {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            result = BitmapDescriptorFactory.fromBitmap(bitmap);
            getCache().put(vectorResId, result);
        }

        return result;
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, float zoom) {
        BitmapDescriptor result = getCache().get(vectorResId);
        if (result == null) {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
            vectorDrawable.setBounds(0, 0, (int) (getHoleMarkerSize(context)), (int) (getHoleMarkerSize(context) / 1.3));
            Bitmap bitmap = Bitmap.createBitmap((int) (getHoleMarkerSize(context)), (int) (getHoleMarkerSize(context) / 1.3), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            result = BitmapDescriptorFactory.fromBitmap(bitmap);
            getCache().put(vectorResId, result);
        }
        getCache().put(vectorResId, result);
        return result;

    }

    public static float getHoleMarkerSize(Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, context.getResources().getInteger(R.integer.marker_size), context.getResources().getDisplayMetrics());
    }

    public static BitmapDescriptor getDistanceMarker(Context context, String distance) {
        if (distance == null || distance.isEmpty()) {
            distance = "empty_distance";
        }
        BitmapDescriptor result = getCache().get(distance.hashCode());
        if (result == null) {
            View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.distance_layout, null);
            TMTextView tv_marker_text = (TMTextView) view.findViewById(R.id.distance);
            tv_marker_text.setText(distance);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            result = BitmapDescriptorFactory.fromBitmap(bitmap);
            getCache().put(distance.hashCode(), result);
        }
        return result;
    }

    public static BitmapDescriptor getMyLocationMarker(Context context, String tag, boolean isLate) {
        if (tag == null || tag.isEmpty()) {
            tag = "empty";
        }
        BitmapDescriptor result = getCache().get(tag.hashCode());
        if (result == null || isLate != globalIsLate) {
            View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker, null);
            TMTextView tv_marker_text = (TMTextView) view.findViewById(R.id.me_tag_map);
            tv_marker_text.setText(tag);
            int color = 0;
            if(!isLate) {
                color = ContextCompat.getColor(context, R.color.green);
            }else{
                color = ContextCompat.getColor(context, R.color.red);
            }

            try {
                GradientDrawable bgShape = (GradientDrawable) tv_marker_text.getBackground().getConstantState().newDrawable().mutate();
                bgShape.setColor(color);
                tv_marker_text.setBackground(bgShape);
            } catch(Throwable e){
                Timber.d(e);
            }

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            result = BitmapDescriptorFactory.fromBitmap(bitmap);
            getCache().put(tag.hashCode(), result);
        }
        globalIsLate = isLate;
        return result;
    }

    public static BitmapDescriptor getHoleMarker(Context context, String hole) {
        if (hole == null || hole.isEmpty()) {
            hole = "empty_hole";
        }

        return BitmapDescriptorFactory.fromBitmap(textAsBitmap(hole, context.getResources().getDimensionPixelOffset(R.dimen.text_size_4), Color.WHITE));
    }

    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setFakeBoldText(true);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public static LatLng getDistanceMarkerPosition(LatLng latLng1, LatLng latLng2) {
        float bearing = getBearing(latLng1, latLng2);

        double distanceBetween = getDistanceByBounds(latLng1, latLng2) /
                (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER) ? 1 : 1.0936133);

        double finalDistance;

        if (distanceBetween < 60) {
            finalDistance = distanceBetween / 2;
        } else {
            finalDistance = 50; //in meters
        }

        LatLng latLng = getDestination(latLng1, bearing, finalDistance);
        return latLng;
    }

    public static LatLng getDestination(LatLng startLoc, float bearing, double depth) {
        LatLng latLng = null;

        double radius = 6371000.0; // earth's mean radius in km
        double lat1 = Math.toRadians(startLoc.latitude);
        double lng1 = Math.toRadians(startLoc.longitude);
        double brng = Math.toRadians(bearing);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(depth / radius) + Math.cos(lat1) * Math.sin(depth / radius) * Math.cos(brng));
        double lng2 = lng1 + Math.atan2(Math.sin(brng) * Math.sin(depth / radius) * Math.cos(lat1), Math.cos(depth / radius) - Math.sin(lat1) * Math.sin(lat2));
        lng2 = (lng2 + Math.PI) % (2 * Math.PI) - Math.PI;

        if (lat2 == 0 || lng2 == 0) {
            latLng = new LatLng(0, 0);
        } else {
            latLng = new LatLng(Math.toDegrees(lat2), Math.toDegrees(lng2));
        }

        return latLng;
    }

    public static String getTimeUTC(long timeStamp) {
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();

            int tzt = tz.getOffset(System.currentTimeMillis());

            timeStamp -= tzt;

            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public static int getLeftMinutes(String time) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat simpleDateFormatCurrent = new SimpleDateFormat("HH:mm");

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            String currentTime = simpleDateFormatCurrent.format(System.currentTimeMillis());

            Date currentDate = simpleDateFormat.parse(currentTime);

            //GOAL TIME
            Date date = simpleDateFormat.parse(time);

            return Integer.valueOf(String.valueOf((date.getTime() - currentDate.getTime()) / 60000));
        } catch (Exception e) {
            return 0;
        }
    }

    public static void smoothlyChangeMarkerPosition(final Marker marker, LatLng latLng) {
        if (marker == null || (marker.getPosition().latitude == latLng.latitude && marker.getPosition().longitude == latLng.longitude)) {
            return;
        }
        final LatLng startPosition = marker.getPosition();
        final double distance = getDistanceByBounds(marker.getPosition(), latLng) /
                (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER) ? 1 : 1.0936133);
        final float bearing = getBearing(marker.getPosition(), latLng);
        final int durationInMs = 500;
        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(durationInMs);
        valueAnimator.setDuration(durationInMs);
        valueAnimator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                return (int) (v * 1000) / 1000.0f;
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = (float) (valueAnimator.getAnimatedValue()) / durationInMs;
                LatLng destination = getDestination(startPosition, bearing, distance * fraction);
                marker.setPosition(destination);
            }
        });
        valueAnimator.start();
    }

    public static float getBearing(LatLng startPoint, LatLng endPoint) {
        double longitude1 = startPoint.longitude;
        double longitude2 = endPoint.longitude;
        double latitude1 = toRadians(startPoint.latitude);
        double latitude2 = toRadians(endPoint.latitude);
        double longDiff = toRadians(longitude2 - longitude1);
        double y = sin(longDiff) * cos(latitude2);
        double x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff);

        return (float) ((toDegrees(atan2(y, x)) + 360) % 360);
    }

    private static final int WORLD_PX_WIDTH = 256;
    private static final double LN2 = 0.6931471805599453;

    public static float getBoundsZoomLevel(LatLngBounds bounds, float mapWidthPx, float multiplier) {
        LatLng ne = bounds.northeast;
        LatLng sw = bounds.southwest;

        double latFraction = (latRad(ne.latitude) - latRad(sw.latitude)) / Math.PI;

        double lngDiff = ne.longitude - sw.longitude;
        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;

        float bearing = getBearing(ne, sw);

        float angle = 0;

        angle = bearing % 90;
        if (angle > 45) {
            angle = 45 - (angle - 45);
        }

        multiplier = angle * (0.0115f);

        double latZoom = zoom(mapWidthPx, WORLD_PX_WIDTH, (float) latFraction);
        double lngZoom = zoom(mapWidthPx, WORLD_PX_WIDTH, (float) lngFraction);

        double result = latZoom > lngZoom ? lngZoom : latZoom;

        return (float) result - multiplier;
    }

    private static double latRad(double lat) {
        double sin = sin(lat * Math.PI / 180d);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2d;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2d;
    }

    private static double zoom(float mapPx, float worldPx, float fraction) {
        return Math.log(mapPx / worldPx / fraction) / LN2;
    }

    private static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    //this function calculates the distance in meters.
    public static double getDistanceByBounds(LatLng startPoint, LatLng endPoint) {
        double earthRadiusM = 6371e3;

        double dLat = degreesToRadians(startPoint.latitude - endPoint.latitude);
        double dLon = degreesToRadians(startPoint.longitude - endPoint.longitude);

        double lat1 = degreesToRadians(startPoint.latitude);
        double lat2 = degreesToRadians(endPoint.latitude);

        double a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return (earthRadiusM * c) * (
                PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER) ? 1 : 1.0936133);
    }

    public static RestHoleDistanceModel getRangeFinderFromMyLocation(LatLng myPoint, LatLng tapPoint,
                                                                     RestHoleModel holeModel) {
        String distanceFromMeToTap =
                String.valueOf((int) TMUtil.getDistanceByBounds(myPoint, tapPoint));
        String distanceFromCenterToTap =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getCenterPointLatLng(), tapPoint));
        String distanceFromMeToFront =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getFrontPointLatLng(), tapPoint));
        String distanceFromMeToBack =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getBackPointLatLng(), tapPoint));

        RestHoleDistanceModel distanceModel = new RestHoleDistanceModel();
        distanceModel.setMyLocation(distanceFromMeToTap);
        distanceModel.setCenter(distanceFromCenterToTap);
        distanceModel.setBack(distanceFromMeToBack);
        distanceModel.setFront(distanceFromMeToFront);

        return distanceModel;
    }

    public static RestHoleDistanceModel getRangeFinderFromTeeLocation(LatLng tapPoint,
                                                                      RestHoleModel holeModel) {
        String distanceFromTeeToTap =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getStartPointLatLng(), tapPoint));
        String distanceFromCenterToTap =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getCenterPointLatLng(), tapPoint));
        String distanceFromMeToFront =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getFrontPointLatLng(), tapPoint));
        String distanceFromMeToBack =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getBackPointLatLng(), tapPoint));

        RestHoleDistanceModel distanceModel = new RestHoleDistanceModel();
        distanceModel.setTee(distanceFromTeeToTap);
        distanceModel.setCenter(distanceFromCenterToTap);
        distanceModel.setBack(distanceFromMeToBack);
        distanceModel.setFront(distanceFromMeToFront);

        return distanceModel;
    }

    public static RestHoleDistanceModel getDistanceFromTeeLocation(RestHoleModel holeModel) {
        String distanceFromCenterToTap =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getCenterPointLatLng(),
                        holeModel.getStartPointLatLng()));

        String distanceFromMeToFront =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getFrontPointLatLng(),
                        holeModel.getStartPointLatLng()));

        String distanceFromMeToBack =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getBackPointLatLng(),
                        holeModel.getStartPointLatLng()));

        RestHoleDistanceModel distanceModel = new RestHoleDistanceModel();
        distanceModel.setCenter(distanceFromCenterToTap);
        distanceModel.setBack(distanceFromMeToBack);
        distanceModel.setFront(distanceFromMeToFront);

        return distanceModel;
    }

    public static RestHoleDistanceModel getDistanceFromMeLocation(LatLng myLocation, RestHoleModel holeModel) {
        String distanceFromCenterToMe =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getCenterPointLatLng(),
                        myLocation));

        String distanceFromFrontToMe =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getFrontPointLatLng(),
                        myLocation));

        String distanceFromBackToMe =
                String.valueOf((int) TMUtil.getDistanceByBounds(holeModel.getBackPointLatLng(),
                        myLocation));

        RestHoleDistanceModel distanceModel = new RestHoleDistanceModel();
        distanceModel.setCenter(distanceFromCenterToMe);
        distanceModel.setBack(distanceFromBackToMe);
        distanceModel.setFront(distanceFromFrontToMe);

        return distanceModel;
    }

    public static int timeToMilli(String timeString) {
        timeString = timeString.replaceAll("\\s+", "");
        String[] time = timeString.split(":");
        int pos = time.length - 1;
        long res = 0;
        if (pos >= 0) {
            res = res + TimeUnit.SECONDS.toMillis(Long.parseLong(time[pos]));
            pos--;
        }
        if (pos >= 0) {
            res = res + TimeUnit.MINUTES.toMillis(Long.parseLong(time[pos]));
            pos--;
        }
        if (pos >= 0) {
            res = res + TimeUnit.HOURS.toMillis(Long.parseLong(time[pos]));
            pos--;
        }
        return (int) res;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void deleteLogsFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/logFileToSendUs.txt");
        if (file.exists()) {
            file.delete();
        }
    }


    public static BitmapDescriptor createMarkerBitmapByDeviceType(TMDevice device, Context context) {
        switch (device.getType()) {
            case DeviceType.appClassic: {
                return createAppClassicMarker(device, context);
            }
            case DeviceType.appCart: {
                return createAppCartMarker(device, context);
            }
            case DeviceType.cart: {
                return createCartMarker(device, context);
            }
            case DeviceType.classic: {
                return createClassicMarker(device, context);
            }
            case DeviceType.drinks: {
                return createDrinksMarker(device, context);
            }
            case DeviceType.marshal: {
                return createMarshalsMarker(device, context);
            }
            case DeviceType.superintendent: {
                return createSuperintendentMarker(device, context);
            }
            default: {
                break;
            }
        }
        return null;
    }

    public static boolean activeTimeDaysPass(List<ActiveTimeDaysItemModel> activeTimeDays) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayFormat = new SimpleDateFormat("E");
        String day = dayFormat.format(System.currentTimeMillis()).toLowerCase();

        for(ActiveTimeDaysItemModel timeDaysItem : activeTimeDays) {
            if(timeDaysItem.getDays().contains(day)) {
                try {
                    long start = timeFormat.parse(timeDaysItem.getStartTime()).getTime();
                    long end = timeFormat.parse(timeDaysItem.getEndTime()).getTime();

                    String nowString = timeFormat.format(System.currentTimeMillis());
                    long now = timeFormat.parse(nowString).getTime();

                    if(start <= now && end >= now) {
                        return true;
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        }
        return false;
    }

    private static BitmapDescriptor createSuperintendentMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.superintended, null);
        TMTextView tv_marker_text = view.findViewById(R.id.tag);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }

    private static BitmapDescriptor createDrinksMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.drinks, null);
        TMTextView tv_marker_text = view.findViewById(R.id.tag);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }


    private static BitmapDescriptor createClassicMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker, null);
        TMTextView tv_marker_text = view.findViewById(R.id.me_tag_map);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }

    private static BitmapDescriptor createMarshalsMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marshal, null);
        TMTextView tv_marker_text = view.findViewById(R.id.tag);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }

    private static BitmapDescriptor createAppClassicMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.app_classic, null);
        TMTextView tv_marker_text = view.findViewById(R.id.tag);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }

    private static BitmapDescriptor createAppCartMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.app_cart, null);
        TMTextView tv_marker_text = view.findViewById(R.id.tag);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }

    private static BitmapDescriptor createCartMarker(TMDevice device, Context context) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cart, null);
        TMTextView tv_marker_text = view.findViewById(R.id.tag);
        tv_marker_text.setText(device.getTag());

        return BitmapDescriptorFactory.fromBitmap(createBitmapFromLayout(view));
    }


    private static Bitmap createBitmapFromLayout(View tv) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(spec, spec);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(tv.getMeasuredWidth(), tv.getMeasuredWidth(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate((-tv.getScrollX()), (-tv.getScrollY()));
        tv.draw(c);
        tv.invalidate();
        return b;
    }

    public static boolean isQuestDevice() {
        return getDeviceName().equals(DeviceConstants.CUBOT_QUEST);
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
