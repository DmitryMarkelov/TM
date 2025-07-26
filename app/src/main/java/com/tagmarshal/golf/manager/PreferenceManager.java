package com.tagmarshal.golf.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.data.Maintenance;
import com.tagmarshal.golf.eventbus.EventMeasureUnit;
import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.Disclaimer;
import com.tagmarshal.golf.rest.model.FirmwareUpdateModel;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.GolfGeniusModel;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.SaveScoreModel;
import com.tagmarshal.golf.util.CartControlHelper;
import com.tagmarshal.golf.util.GeofenceHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PreferenceManager {

    public static final String UNIT_METRIC_METER = "meter";
    public static final String UNIT_METRIC_YARD = "yard";

    public static final String DEVICE_TYPE_MOBILE = "normal";
    public static final String DEVICE_TYPE_CART = "cart";

    public static final String RATED_ROUND = "rated_round";
    private static final String CURRENT_PLAYHOLE = "current_playhole";
    private static final String IS_SCORE_ACTIVE = "is_score_active";
    private static final String IS_LEADER_ACRIVE = "is_leader_active";
    private static final String IS_GEO_FAILED = "is_geo_failed";
    private static final String PACE_INFO = "pace_info";
    private static final String CURRENCY = "currency";
    private static final String KITCHEN_TIME = "kitchen_active_time_days";
    private static final String RETURN_DEVICE = "return_device";
    private static final String NEAREST_COURSES = "nearest_courses";
    private static final String CURRENT_COURSE = "current_course";
    private static final String ADVERTISEMENT_CONFIG = "advertisement_course";
    private static final String DEVICE_INCHES = "device_inches";
    private static final String MAINTENANCE = "maintenance";
    private static final String ADVERTISEMENT_LAST_FETCH = "advertisement_last_fetch";

    private final String DEVICE_TAG = "device_tag";
    private final String DEVICE_TYPE = "device_type";
    private final String DEVICE_UNIT_METRIC = "device_unit_metric";
    private final String FIRST_LAUNCH = "first_launch";
    private final String COURSE = "course";
    private final String COURSE_NAME = "course_name";
    private final String COURSE_TEE_START_TIME = "course_tee_start_time";
    private final String COURSE_GROUP_ID = "course_group_id";
    private final String COURSE_HOLES = "course_holes";
    private final String COURSE_START_HOLE = "course_start_hole";
    private final String COURSE_MAP_BOUNDS = "course_map_bounds";
    private final String DEVICE_VARIABLE = "device_variable";
    private final String COURSE_GEOFENCES = "course_geofences";
    private final String COURSE_GEOZONES = "course_geozones";
    private final String TAG_40_SETUP = "tag_40_setup";
    private final String ORIENTATION = "orientation";
    private final String MAP_VERSION = "map_verison";
    private final String COURSE_LOGO = "course_logo";
    private final String FAILED_FIXES = "failed_fixes";
    private final String SPEAKER_MAC = "speaker_mac";
    private final String AUDIO_ID = "audio_id";
    private final String FIRMWARE_UPDATE = "firmware_update";
    private final String ORDER_ITEMS = "order_items";
    private final String DISCLAIMER = "disclaimer";
    private final String DISCLAIMERTIME = "disclaimer_time";
    private final String DISCLAIMERDISMISSED = "disclaimer_dismissed";
    private final String DISCLAIMERACCEPTED = "disclaimer_accepted";

    public enum DISCLAIMER_STATUS {accepted, dismissed}

    private final String CART_CONTROL_MAC = "cart_control_mac";


    public final static int ORIENTATION_NORMAL = 1;
    public final static int ORIENTATION_REVERSE = 9;

    private final String ROUND_ID = "round_id";
    private final String LAST_ROUNDID = "last_round_id";

    // Golf Genius
    private final String GOLF_GENIUS = "golf_genius";
    private final String FAILED_SCORES = "failed_scores";
    private final String SCORE_LAST_HOLE_INDEX = "score_hole_index";

    private final SharedPreferences sharedPreferences;

    private static PreferenceManager preferenceManager;

    public static void init(Context context) {
        if (preferenceManager == null)
            preferenceManager = new PreferenceManager(context);
    }

    public PreferenceManager(Context context) {
        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceManager getInstance() {
        return preferenceManager;
    }

    public void setDeviceTag(String tag, String url) {
        sharedPreferences.edit().putString(DEVICE_TAG, tag).apply();
        sharedPreferences.edit().putString(url + "_" + DEVICE_TAG, tag).apply();
    }

    public void setOrientation(int orientation) {
        sharedPreferences.edit().putInt(ORIENTATION, orientation).apply();
    }

    public int getOrientation() {
        return sharedPreferences.getInt(ORIENTATION, 1);
    }

    public void clearGameData(String url) {
        setStartHole(1);
        setCourseGroupId(null);
        setCourse(null);
        setDeviceTag(null, url);
        setCourseTeeStartTime(null);
        setCourseName(null, url);
        setCourseHoles(null);
        setMapBounds(null);
        setCourseGeoFences(null);
        setCourseGeoZones(null);
        setPaceInfo(null);

    }

    public void clearCurrentCourse(String url) {
        saveCurrentCourse(null, url);
    }


    private void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();

    }

    public void clearZoneInfo() {
        setCourseGeoFences(null);
        setCourseGeoZones(null);
        setMapBounds(null);
    }

    public boolean isConfigNull(String config) {
        String url = PreferenceManager.getInstance().getCourse();
        return sharedPreferences.getString(url + "_" + config, null) == null;
    }

    public List<RestGeoFenceModel> getGeoFences() {
        String url = PreferenceManager.getInstance().getCourse();
        if (url != null) {
            String stringGeofences = sharedPreferences.getString(url + "_" + COURSE_GEOFENCES, null);

            if (stringGeofences == null) {
                stringGeofences = sharedPreferences.getString(COURSE_GEOFENCES, null);
            }

            Gson gson = new Gson();
            return gson.fromJson(stringGeofences, new TypeToken<List<RestGeoFenceModel>>() {
            }.getType());
        } else {
            return null;
        }
    }

    public List<RestGeoZoneModel> getGeoZones() {
        String url = PreferenceManager.getInstance().getCourse();
        if (url != null) {
            String stringGeoZones = sharedPreferences.getString(url + "_" + COURSE_GEOZONES, null);

            if (stringGeoZones == null) {
                stringGeoZones = sharedPreferences.getString(COURSE_GEOZONES, null);
            }

            Gson gson = new Gson();
            return gson.fromJson(stringGeoZones, new TypeToken<List<RestGeoZoneModel>>() {
            }.getType());
        } else {
            return null;
        }
    }

    public void setCourseGeoZones(List<RestGeoZoneModel> geoZones) {
        String url = PreferenceManager.getInstance().getCourse();
        if (geoZones == null) {
            sharedPreferences.edit().putString(COURSE_GEOZONES, null).apply();
            sharedPreferences.edit().putString(url + "_" + COURSE_GEOZONES, null).apply();
            return;
        }

        String geofencesString;
        try {
            Gson gson = new GsonBuilder().create();
            geofencesString = gson.toJson(geoZones);
            sharedPreferences.edit().putString(COURSE_GEOZONES, geofencesString).apply();
            sharedPreferences.edit().putString(url + "_" + COURSE_GEOZONES, geofencesString).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCourseGeoFences(List<RestGeoFenceModel> geoFences) {
        String url = PreferenceManager.getInstance().getCourse();
        if (geoFences == null) {
            sharedPreferences.edit().putString(COURSE_GEOFENCES, null).apply();
            sharedPreferences.edit().putString(url + "_" + COURSE_GEOFENCES, null).apply();
            GeofenceHelper.geofenceList.clear(); //Allows the GeofenceHelper to be updated with the new geofences
            CartControlHelper.geofenceList.clear();
            GeofenceHelper.allowGeofenceReload = true;
            return;
        }

        String geofencesString;
        try {
            Gson gson = new GsonBuilder().create();
            geofencesString = gson.toJson(geoFences);
            sharedPreferences.edit().putString(COURSE_GEOFENCES, geofencesString).apply();
            sharedPreferences.edit().putString(url + "_" + COURSE_GEOFENCES, geofencesString).apply();
            GeofenceHelper.geofenceList.clear(); //Allows the GeofenceHelper to be updated with the new geofences
            CartControlHelper.geofenceList.clear();
            GeofenceHelper.allowGeofenceReload = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Date getDisclaimerAcceptedTime() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        String date = sharedPreferences.getString(DISCLAIMERTIME, null);
        return dateFormat.parse(date);
    }

    public void setDisclaimerAcceptedTime(String date) {
        sharedPreferences.edit().putString(DISCLAIMERTIME, date).apply();
    }

    public boolean isDisclaimerDismissed() {
        return sharedPreferences.getBoolean(DISCLAIMERDISMISSED, false);
    }

    public void acceptDisclaimer() {
        sharedPreferences.edit().putBoolean(DISCLAIMERACCEPTED, true).apply();
    }

    public boolean isDisclaimerAccepted() {
        return sharedPreferences.getBoolean(DISCLAIMERACCEPTED, false);
    }

    public void setDisclaimerDismissed(boolean isDismissed) {
        sharedPreferences.edit().putBoolean(DISCLAIMERDISMISSED, isDismissed).apply();
    }

    public void setDisclaimer(Disclaimer disclaimer) {
        try {
            Gson gson = new GsonBuilder().create();
            sharedPreferences.edit().putString(DISCLAIMER, gson.toJson(disclaimer)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Disclaimer getDisclaimer() {
        Gson gson = new GsonBuilder().create();
        String disclaimer = sharedPreferences.getString(DISCLAIMER, null);
        Log.d("GEOGT", "Disclaimer:" + disclaimer);
        return gson.fromJson(disclaimer, new TypeToken<Disclaimer>() {
        }.getType());
    }

    public void setCourseGroupId(String groupId) {
        sharedPreferences.edit().putString(COURSE_GROUP_ID, groupId).apply();
    }

    public void setCourseHoles(List<RestHoleModel> holes) {
        String url = PreferenceManager.getInstance().getCourse();
        if (holes == null) {
            sharedPreferences.edit().putString(COURSE_HOLES, null).apply();
            sharedPreferences.edit().putString(url + "_" + COURSE_HOLES, null).apply();
            return;
        }

        String json = null;
        try {
            Gson gson = new GsonBuilder().create();
            json = gson.toJson(holes);
            sharedPreferences.edit().putString(COURSE_HOLES, json).apply();
            sharedPreferences.edit().putString(url + "_" + COURSE_HOLES, json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RestHoleModel> getHoles() {
        String url = PreferenceManager.getInstance().getCourse();
        if (url != null) {
            String stringHoles = sharedPreferences.getString(url + "_" + COURSE_HOLES, null);

            if (stringHoles == null) {
                stringHoles = sharedPreferences.getString(COURSE_HOLES, null);
            }

            Gson gson = new Gson();
            return gson.fromJson(stringHoles, new TypeToken<List<RestHoleModel>>() {
            }.getType());
        } else {
            return null;
        }

    }

    public void setMapBounds(RestBoundsModel boundModel) {
        String url = PreferenceManager.getInstance().getCourse();
        if (url != null) {
            if (boundModel == null) {
                sharedPreferences.edit().putString(COURSE_MAP_BOUNDS, null).apply();
                sharedPreferences.edit().putString(url + "_" + COURSE_MAP_BOUNDS, null).apply();
            } else {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(boundModel);
                sharedPreferences.edit().putString(COURSE_MAP_BOUNDS, json).apply();
                sharedPreferences.edit().putString(url + "_" + COURSE_MAP_BOUNDS, json).apply();
            }
        }
        GeofenceHelper.geofenceList.clear(); //Allows the GeofenceHelper to be updated with new geofences and thresholds
        CartControlHelper.geofenceList.clear();
        GeofenceHelper.allowGeofenceReload = true;
    }

    public void setCourse(String course) {
        sharedPreferences.edit().putString(COURSE, course).apply();
    }

    public void setCourseTeeStartTime(String startTeeTime) {
        sharedPreferences.edit().putString(COURSE_TEE_START_TIME, startTeeTime).apply();
    }

    public void setCourseName(String course, String url) {
        sharedPreferences.edit().putString(COURSE_NAME, course).apply();
        sharedPreferences.edit().putString(url + "_" + COURSE_NAME, course).apply();
    }

    public void setFirstLaunch(boolean firstLaunch) {
        sharedPreferences.edit().putBoolean(FIRST_LAUNCH, firstLaunch).apply();
    }

    public void setTag40Setup(boolean tag40Setup) {
        sharedPreferences.edit().putBoolean(TAG_40_SETUP, tag40Setup).apply();
    }

    public void setStartHole(int hole) {
        sharedPreferences.edit().putInt(COURSE_START_HOLE, hole).apply();
    }

    public int getScoreLastHoleIndex() {
        return sharedPreferences.getInt(SCORE_LAST_HOLE_INDEX, 0);
    }

    public void setScoreLastHoleIndex(int scoreLastHoleIndex) {
        try {
            sharedPreferences.edit().putInt(SCORE_LAST_HOLE_INDEX, scoreLastHoleIndex).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearData(Context context) {
        deleteRecursive(Objects.requireNonNull(GolfApplication.context.getExternalCacheDir()));
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Glide.get(context).clearDiskCache();
                return null;
            }
        };

        sharedPreferences.edit().clear().apply();
    }


    public RestBoundsModel getMapBounds() {
        String url = PreferenceManager.getInstance().getCourse();

        if (url != null) {
            String mapBounds = sharedPreferences.getString(url + "_" + COURSE_MAP_BOUNDS, null);

            if (mapBounds == null) {
                mapBounds = sharedPreferences.getString(COURSE_MAP_BOUNDS, null);
            }

            Gson gson = new Gson();
            return gson.fromJson(mapBounds, new TypeToken<RestBoundsModel>() {
            }.getType());
        }
        return null;
    }

    public void setDeviceVariable(boolean variable) {
        sharedPreferences.edit().putBoolean(DEVICE_VARIABLE, variable).apply();
    }

    public void setDeviceType(String deviceType) {
        sharedPreferences.edit().putString(DEVICE_TYPE, deviceType).apply();
    }

    public void setDeviceUnitMetric(String deviceUnitMetric) {
        boolean sendEvent = !deviceUnitMetric.equals(getDeviceUnitMetric());
        sharedPreferences.edit().putString(DEVICE_UNIT_METRIC, deviceUnitMetric).apply();
        if (sendEvent) EventBus.getDefault().post(new EventMeasureUnit());
    }

    public String getDeviceTag() {
        return sharedPreferences.getString(DEVICE_TAG, null);
    }

    public String getDeviceTag(String url) {
        return sharedPreferences.getString(url + "_" + DEVICE_TAG, null);
    }

    public String getCourseGroupId() {
        return sharedPreferences.getString(COURSE_GROUP_ID, null);
    }

    public String getDeviceType() {
        return sharedPreferences.getString(DEVICE_TYPE, DEVICE_TYPE_MOBILE);
    }


    public int getStartHole() {
        int startHole = sharedPreferences.getInt(COURSE_START_HOLE, 1);
        if (startHole < 1) {
            setStartHole(1);
            startHole = 1;
        }
        return startHole;
    }

    public boolean isDeviceVariable() {
        return sharedPreferences.getBoolean(DEVICE_VARIABLE, false);
    }

    public String getCourse() {
        return sharedPreferences.getString(COURSE, null);
    }

    public String getCourseName() {
        return sharedPreferences.getString(COURSE_NAME, null);
    }

    public String getCourseTeeStartTime() {
        return sharedPreferences.getString(COURSE_TEE_START_TIME, null);
    }

    public String getDeviceUnitMetric() {
        return sharedPreferences.getString(DEVICE_UNIT_METRIC, UNIT_METRIC_METER);
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true);
    }

    public boolean isTag40Setup() {
        return sharedPreferences.getBoolean(TAG_40_SETUP, false);
    }

    public boolean isDeviceMobile() {
        return getDeviceType().equals(DEVICE_TYPE_MOBILE);

    }

    public boolean isRoundRated() {
        return sharedPreferences.getBoolean(RATED_ROUND, false);

    }

    public void setRatedRound(Boolean isRatedRound) {
        sharedPreferences.edit().putBoolean(RATED_ROUND, isRatedRound).apply();
    }

    public void setCurrentPlayHole(String currentHole) {
        sharedPreferences.edit().putString(CURRENT_PLAYHOLE, currentHole).apply();

    }

    public String getCurrentPlayhole() {
        return sharedPreferences.getString(CURRENT_PLAYHOLE, "");
    }

    public void setIsScoreActive(boolean scoreActive) {
        sharedPreferences.edit().putBoolean(IS_SCORE_ACTIVE, scoreActive).apply();

    }

    public Boolean getIsScoreActive() {
        return sharedPreferences.getBoolean(IS_SCORE_ACTIVE, false);
    }

    public void setIsLeaderBoardActive(boolean leaderActive) {
        sharedPreferences.edit().putBoolean(IS_LEADER_ACRIVE, leaderActive).apply();

    }

    public Boolean getIsLeaderBoardActive() {
        return sharedPreferences.getBoolean(IS_LEADER_ACRIVE, false);
    }

    public void setGeoFailed(boolean isFail) {
        sharedPreferences.edit().putBoolean(IS_GEO_FAILED, isFail).apply();

    }

    public boolean isGeoZonesFailed() {
        return sharedPreferences.getBoolean(IS_GEO_FAILED, false);
    }

    public String getRoundID() {
        return sharedPreferences.getString(ROUND_ID, null);
    }

    public void setRoundID(String roundID) {
        sharedPreferences.edit().putString(ROUND_ID, roundID).apply();
    }

    public void setPaceInfo(RestInRoundModel paceInfo) {
        Gson gson = new Gson();
        sharedPreferences.edit().putString(PACE_INFO, gson.toJson(paceInfo)).apply();
    }

    public RestInRoundModel getPaceInfo() {
        Log.d("GEOGT", "PaceInfo()");
        Gson gson = new Gson();
        return gson.fromJson(sharedPreferences.getString(PACE_INFO, null), new TypeToken<RestInRoundModel>() {
        }.getType());
    }

    public void saveLastRoundID(String roundID) {
        sharedPreferences.edit().putString(LAST_ROUNDID, roundID).apply();
    }

    public String getLastRoundID() {
        return sharedPreferences.getString(LAST_ROUNDID, null);
    }

    public void setCurrency(String currency) {
        sharedPreferences.edit().putString(CURRENCY, currency).apply();

    }

    public String getCurrency() {
        return sharedPreferences.getString(CURRENCY, "");
    }

    public void saveKitchenTime(List<ActiveTimeDaysItemModel> kitchen) {
        Gson gson = new Gson();
        sharedPreferences.edit().putString(KITCHEN_TIME, gson.toJson(kitchen)).apply();
    }

    public List<ActiveTimeDaysItemModel> getKitchenTime() {
        Gson gson = new Gson();
        return gson.fromJson(sharedPreferences.getString(KITCHEN_TIME, "[]"), new TypeToken<List<ActiveTimeDaysItemModel>>() {
        }.getType());
    }

    public void setReturnDeviceStatus(boolean returnDeviceActiveStatus) {
        sharedPreferences.edit().putBoolean(RETURN_DEVICE, returnDeviceActiveStatus).apply();
    }

    public boolean getReturnDeviceStatus() {
        return sharedPreferences.getBoolean(RETURN_DEVICE, false);
    }

    public void saveNearestCourses(List<CourseModel> coordList) {
        Gson gson = new Gson();
        sharedPreferences.edit().putString(NEAREST_COURSES, gson.toJson(coordList)).apply();
        nearSave();
    }

    public void nearSave() {
        sharedPreferences.edit().putBoolean("course_saved", true).apply();
    }

    public boolean nearSaved() {
        return sharedPreferences.getBoolean("course_saved", false);
    }

    public List<CourseModel> getNearestCourses() {
        Gson gson = new Gson();
        return gson.fromJson(sharedPreferences.getString(NEAREST_COURSES, ""), new TypeToken<List<CourseModel>>() {
        }.getType());
    }

    public void saveCurrentCourse(CourseModel selectedCourse, String url) {
        Gson gson = new Gson();
        sharedPreferences.edit().putString(CURRENT_COURSE, gson.toJson(selectedCourse)).apply();
        sharedPreferences.edit().putString(url + "_" + CURRENT_COURSE, gson.toJson(selectedCourse)).apply();
    }

    public CourseModel getCurrentCourseModel() {
        Gson gson = new Gson();
        return gson.fromJson(sharedPreferences.getString(CURRENT_COURSE, ""), new TypeToken<CourseModel>() {
        }.getType());
    }

    public void setAdvertisement(List<AdvertisementModel> advertisements) {
        Gson gson = new Gson();
        sharedPreferences.edit().putString(ADVERTISEMENT_CONFIG, gson.toJson(advertisements)).apply();
    }

    public List<AdvertisementModel> getAdvertisements() {
        return new Gson().fromJson(
                sharedPreferences.getString(ADVERTISEMENT_CONFIG, ""), new TypeToken<List<AdvertisementModel>>() {
                }.getType()
        );
    }

    public void setAdvertisementsLastFetch(long datetime) {
        sharedPreferences.edit().putLong(ADVERTISEMENT_LAST_FETCH, datetime).apply();
    }

    public long getAdvertisementsLastFetch() {
        return sharedPreferences.getLong(ADVERTISEMENT_LAST_FETCH, 0);
    }

    public void setDeviceInches(double deviceInches) {
        sharedPreferences.edit().putFloat(DEVICE_INCHES, (float) deviceInches).apply();
    }

    public double getDeviceInches() {
        return sharedPreferences.getFloat(DEVICE_INCHES, 0);
    }

    public String getMapVersion() {
        if (sharedPreferences.getString(MAP_VERSION, null) == null) {
            clearMapData();
        }
        return sharedPreferences.getString(MAP_VERSION, String.valueOf(System.currentTimeMillis()));
    }

    public String getCourseLogo() {
        if (sharedPreferences.getString(COURSE_LOGO, null) == null) {
            clearLogoData();
        }
        return sharedPreferences.getString(COURSE_LOGO, String.valueOf(System.currentTimeMillis()));
    }

    public void clearLogoData() {
        sharedPreferences.edit().putString(COURSE_LOGO, String.valueOf(System.currentTimeMillis()));
    }

    public void clearMapData() {
        sharedPreferences.edit().putString(MAP_VERSION, String.valueOf(System.currentTimeMillis())).apply();
    }

    public void saveMaintenance(Maintenance time) {
        Gson gson = new GsonBuilder().create();
        String timeString = gson.toJson(time);
        sharedPreferences.edit().putString(MAINTENANCE, timeString).apply();
    }

    public void clearMaintenance() {
        sharedPreferences.edit().remove(MAINTENANCE).apply();
        Log.d("gagasgasga", "CASE CLEAR");

    }

    public Maintenance getMaintenance() {
        return new Gson().fromJson(
                sharedPreferences.getString(MAINTENANCE, ""), new TypeToken<Maintenance>() {
                }.getType()
        );
    }

    public void saveFirmwareUpdate(FirmwareUpdateModel firmwareUpdateModel) {
        String firmwareUpdateString = null;
        if (firmwareUpdateModel != null) {
            Gson gson = new GsonBuilder().create();
            firmwareUpdateString = gson.toJson(firmwareUpdateModel);
        }
        sharedPreferences.edit().putString(FIRMWARE_UPDATE, firmwareUpdateString).apply();
    }

    public FirmwareUpdateModel getFirmwareUpdate() {
        return new Gson().fromJson(
                sharedPreferences.getString(FIRMWARE_UPDATE, null), new TypeToken<FirmwareUpdateModel>() {
                }.getType()
        );
    }

    // Golf Genius
    public void saveGolfGenius(GolfGeniusModel model) {
        GolfGeniusModel oldModel = getGolfGenius();
        if (oldModel == null || !oldModel.getRoundId().equals(model.getRoundId())) {
            setScoreLastHoleIndex(0);
        }

        Gson gson = new GsonBuilder().create();
        String ggString = gson.toJson(model);
        sharedPreferences.edit().putString(GOLF_GENIUS, ggString).apply();
    }

    // Golf Genius
    public void completeGolfGenius() {
        GolfGeniusModel model = getGolfGenius();
        if (model != null) {
            model.setComplete(true);
            Gson gson = new GsonBuilder().create();
            String ggString = gson.toJson(model);
            sharedPreferences.edit().putString(GOLF_GENIUS, ggString).apply();
        }
    }

    public GolfGeniusModel getGolfGenius() {
        return new Gson().fromJson(
                sharedPreferences.getString(GOLF_GENIUS, ""), new TypeToken<GolfGeniusModel>() {
                }.getType()
        );
    }

    public void saveFailedScores(List<SaveScoreModel> saveScoreModels) {
        Gson gson = new Gson();
        sharedPreferences.edit().putString(FAILED_SCORES, gson.toJson(saveScoreModels)).apply();
    }

    public void saveFailedScore(SaveScoreModel model) {
        boolean found = false;
        List<SaveScoreModel> failedList = getGolfFailedScores();

        for (int i = 0; i < failedList.size(); i++) {
            SaveScoreModel saveScoreModel = failedList.get(i);
            if (saveScoreModel.getHole() == model.getHole() && saveScoreModel.getRoundId().equals(model.getRoundId())) {
                failedList.set(i, model);
                found = true;
                break;
            }
        }

        if (!found) failedList.add(model);

        Gson gson = new GsonBuilder().create();
        String ggString = gson.toJson(failedList);
        sharedPreferences.edit().putString(FAILED_SCORES, ggString).apply();
    }

    public List<SaveScoreModel> getGolfFailedScores() {
        return new Gson().fromJson(
                sharedPreferences.getString(FAILED_SCORES, "[]"), new TypeToken<List<SaveScoreModel>>() {
                }.getType()
        );
    }

    public void addFailedFix(FixModel model) {
        List<FixModel> failedList = new Gson().fromJson(
                sharedPreferences.getString(FAILED_FIXES, "[]"), new TypeToken<List<FixModel>>() {
                }.getType()
        );

        if (failedList.size() >= 500)
            failedList.remove(0);

        failedList.add(model);

        Gson gson = new GsonBuilder().create();
        String string = gson.toJson(failedList);
        sharedPreferences.edit().putString(FAILED_FIXES, string).apply();
    }

    public ArrayList<FixModel> getFailedFixes() {
        ArrayList<FixModel> list = new Gson().fromJson(
                sharedPreferences.getString(FAILED_FIXES, "[]"),
                new TypeToken<ArrayList<FixModel>>() {
                }.getType()
        );
        Collections.reverse(list);
        return list;
    }

    public void clearFailedFixes() {
        sharedPreferences.edit().putString(FAILED_FIXES, "[]").apply();
    }

    public void clearOrderItems() {
        sharedPreferences.edit().putString(ORDER_ITEMS, null).apply();
    }

    public void updateOrderItem(OrderItem item) {
        List<OrderItem> existingOrderItems = getOrderItems();
        for (int x = 0; x < existingOrderItems.size(); x++) {
            if (existingOrderItems.get(x).getId().equalsIgnoreCase(item.getId())) {
                existingOrderItems.set(x, item);
                break;
            }
        }
        clearOrderItems();
        setOrderItems(existingOrderItems);
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        Gson gson = new Gson();
        List<OrderItem> existingOrderItems = getOrderItems();
        if (existingOrderItems == null)
            existingOrderItems = new ArrayList<>();
        for (OrderItem orderitem : orderItems) {
            existingOrderItems.add(orderitem);
        }
        String json = gson.toJson(existingOrderItems);
        sharedPreferences.edit().putString(ORDER_ITEMS, json).apply();
    }

    public List<OrderItem> getOrderItems() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ORDER_ITEMS, null);
        return json == null ? new ArrayList<>() : gson.fromJson(json, new TypeToken<List<OrderItem>>() {
        }.getType());
    }

    public void removeItem(OrderItem orderItem) {
        Gson gson = new Gson();
        List<OrderItem> existingOrderItems = getOrderItems();
        for (int x = 0; x < existingOrderItems.size(); x++) {
            if (existingOrderItems.get(x).getId().equalsIgnoreCase(orderItem.getId())) {
                existingOrderItems.remove(x);
            }
        }
        String json = gson.toJson(existingOrderItems);
        sharedPreferences.edit().putString(ORDER_ITEMS, json).apply();
    }

    public void updateQuantity(OrderItem orderItem, int quantity) {
        Gson gson = new Gson();
        List<OrderItem> existingOrderItems = getOrderItems();
        for (OrderItem item : existingOrderItems) {
            if (item.getId().equalsIgnoreCase(orderItem.getId())) {
                item.setQuantity(quantity);
            }
        }
        String json = gson.toJson(existingOrderItems);
        sharedPreferences.edit().putString(ORDER_ITEMS, json).apply();
    }

    public void setCartControlMac(String cartControlMac) {
        sharedPreferences.edit().putString(CART_CONTROL_MAC, cartControlMac).apply();
    }

    public String getCartControlMac() {
        return sharedPreferences.getString(CART_CONTROL_MAC, "");
    }

    public void setSpeakerMac(String speakerMac) {
        sharedPreferences.edit().putString(SPEAKER_MAC, speakerMac).apply();
    }

    public String getSpeakerMac() {
        return sharedPreferences.getString(SPEAKER_MAC, "");
    }

    public void setAudioID(String audioID) {
        sharedPreferences.edit().putString(AUDIO_ID, audioID).apply();
    }

    public String getAudioID() {
        return sharedPreferences.getString(AUDIO_ID, "");
    }
}
