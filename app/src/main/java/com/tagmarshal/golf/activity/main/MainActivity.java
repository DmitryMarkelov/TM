package com.tagmarshal.golf.activity.main;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tagmarshal.golf.BuildConfig;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.BaseActivity;
import com.tagmarshal.golf.activity.DebugConsoleActivity;
import com.tagmarshal.golf.activity.gg_scoring.ScoringActivity;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.callback.RebindServiceCallback;
import com.tagmarshal.golf.dialog.DialogAdminEnterCreds;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.dialog.GolfSendMessageDialog;
import com.tagmarshal.golf.dialog.PleaseWaitDialog;
import com.tagmarshal.golf.eventbus.FirebaseGetAlerts;
import com.tagmarshal.golf.eventbus.FirmwareUpdateEvent;
import com.tagmarshal.golf.eventbus.MapBoundsEvent;
import com.tagmarshal.golf.eventbus.MovingCartEvent;
import com.tagmarshal.golf.eventbus.OfflineEvent;
import com.tagmarshal.golf.eventbus.OnSuccessReregister;
import com.tagmarshal.golf.eventbus.OnlineEvent;
import com.tagmarshal.golf.eventbus.RedownloadSlideShow;
import com.tagmarshal.golf.eventbus.ServiceOnline;
import com.tagmarshal.golf.eventbus.SetMaintenanceEvent;
import com.tagmarshal.golf.eventbus.ShowScoreEvent;
import com.tagmarshal.golf.eventbus.StopMaintenanceEvent;
import com.tagmarshal.golf.eventbus.UserInteractionEvent;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.FragmentInAcitivty;
import com.tagmarshal.golf.fragment.admin.FragmentAdmin;
import com.tagmarshal.golf.fragment.alert.FragmentAlert;
import com.tagmarshal.golf.fragment.contact.FragmentContactUs;
import com.tagmarshal.golf.fragment.cortshop.FoodMenuFragment;
import com.tagmarshal.golf.fragment.devicesetup.FragmentDeviceSetup;
import com.tagmarshal.golf.fragment.emergency.FragmentEmergency;
import com.tagmarshal.golf.fragment.groups.pager.FragmentGroupsPager;
import com.tagmarshal.golf.fragment.leaderboard.LeaderBoardFragment;
import com.tagmarshal.golf.fragment.maintenance.MaintenanceFragment;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.fragment.measuring.FragmentMeasuringUnits;
import com.tagmarshal.golf.fragment.refreshments.FragmentRefreshments;
import com.tagmarshal.golf.fragment.reportissue.FragmentReportIssue;
import com.tagmarshal.golf.fragment.roundend.FragmentRoundEnd;
import com.tagmarshal.golf.fragment.roundinfo.FragmentRoundInfo;
import com.tagmarshal.golf.fragment.scorecard.ScoreCardFragment;
import com.tagmarshal.golf.fragment.selectcourse.FragmentSelectCourse;
import com.tagmarshal.golf.manager.IntentManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.FirmwareUpdateModel;
import com.tagmarshal.golf.rest.model.GolfGeniusModel;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGolfGeniusModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.service.GolfService;
import com.tagmarshal.golf.service.cleartee.TeeRefresher;
import com.tagmarshal.golf.util.CartControlHelper;
import com.tagmarshal.golf.util.SnapToPlayHelper;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.SideMenuItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

public class MainActivity extends BaseActivity implements DialogAdminEnterCreds.OnAdminEnterCredsListener,
        MainActivityContract.View, GolfCallback.GolfServiceListener {

    @BindView(R.id.back)
    View mAppBar;

    @BindView(R.id.appbar)
    ConstraintLayout realAppBar;

    @BindView(R.id.menu)
    ImageView mMenu;

    @BindView(R.id.top_margin)
    View topMargin;

    @BindView(R.id.drawer)
    DrawerLayout mDrawer;

    @BindView(R.id.club_logo_iv)
    ImageView clubLogoIv;

    @BindView(R.id.logo_place)
    ImageView logoPlaceIv;

    @BindView(R.id.call_drinks_cart_item)
    SideMenuItem drinksCart;

    @BindView(R.id.score_item)
    SideMenuItem scoreItem;

    @BindView(R.id.leaderboard_item)
    SideMenuItem leaderItem;

    @BindView(R.id.food_and_bevs)
    SideMenuItem foodAndBevItem;

    @BindView(R.id.enable_speakers)
    SideMenuItem enableSpeakers;

    @BindView(R.id.disable_speakers)
    SideMenuItem disableSpeakers;

    @BindView(R.id.contact)
    SideMenuItem contactUsItem;

    @BindView(R.id.emergency)
    SideMenuItem emergencyItem;

    @BindView(R.id.map_item)
    SideMenuItem mapItem;

    @BindView(R.id.change_course)
    SideMenuItem changeCourseItem;

    double lastX = 0;
    double lastY = 0;
    double lastZ = 0;
    boolean hasAcc = false;
    boolean moved = false;
    private boolean golfServiceConnected = false;
    private boolean logoAdded;
    private boolean pauseActivityStarted;
    private boolean logoIsTouch, menuIsTouch, doubleClick;
    private long touchLogo, touchMenu;
    private BaseFragment fragment;
    private MainActivityPresenter presenter;
    private PleaseWaitDialog waitDialog;
    private GolfService golfService;
    private boolean inRound = false;
    private boolean groupSafety = false;
    private PendingIntent alarmIntent;
    private AlarmManager alarmMgr;
    private Date offlineDate = null;
    private BroadcastReceiver mNetworkReceiver;
    private Intent golfServiceIntent;
    private ArrayList<RebindServiceCallback> callbacks = new ArrayList<>();
    private boolean isCourseSaved;
    private GolfAlertDialog courseRefreshDialog;
    private boolean isCheckForZoneActive;
    private GolfAlertDialog openNewCourseDialog;
    private boolean isChangeCourseDelayActivated = true;
    private String currentCourseName = "";
    private boolean isBinded = false;
    private boolean clearedData = false;
    final Handler dimScreenHandler = new Handler();
    final Runnable dimScreenRunnable = () -> setKeepScreenOn(false);
    FragmentAlert fragmentAlert = null;
    Unbinder unbinder;
    DownloadManager downloadManager = null;
    Disposable inRoundFalseAccDisposable;
    SensorManager sensorManager;
    Sensor sensor;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onBindingDied(ComponentName name) {
            Log.d(getClass().getName(), "onBindingDied: executed");
            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onBindingDied: executed");
            golfServiceConnected = false;
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(getClass().getName(), "onNullBinding: executed");
            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onNullBinding: executed");
            golfServiceConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            GolfService.GolfServiceBinder binder = (GolfService.GolfServiceBinder) service;
            golfService = binder.getService();

            EventBus.getDefault().postSticky(new ServiceOnline());

            getGolfService().bindCallback(MainActivity.this);
            for (RebindServiceCallback callback : callbacks) {
                callback.rebind();
            }
            Log.i(getClass().getName(), "onServiceConnected: connectionCreated - next step openStartScreen");
            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onServiceConnected: connectionCreated - next step openStartScreen");
            if (getSupportFragmentManager().getFragments().isEmpty()) {
                openStartScreen();
            }

            isBinded = true;
            golfServiceConnected = true;

            getGolfService().enableService();
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            golfServiceConnected = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                golfService.stopForeground(Service.STOP_FOREGROUND_DETACH);
            } else {
                golfService.stopService(golfServiceIntent);
            }
            golfService = null;
            Log.d("Taaaaaaaaaaaag", "service disconnected");
        }


    };
    private boolean isCreated = false;
    private int curBrightnessValue;
    private float xAcc, yAcc, zAcc = 100f;

    public MainActivity() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        deviceInteraction();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        deviceInteraction();
        waitDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            writeToFile("app-start: " + SystemClock.uptimeMillis());
        } catch (Exception e) {
        }
        unbinder = ButterKnife.bind(this);
        golfServiceIntent = new Intent(getApplicationContext(), GolfService.class);
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        hideSystemUI();
                    }
                });
        presenter = new MainActivityPresenter(this, this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (xAcc == 100f) {
                    xAcc = sensorEvent.values[0];
                    yAcc = sensorEvent.values[1];
                    zAcc = sensorEvent.values[2];
                }

                if (Math.abs(xAcc - sensorEvent.values[0]) > 3f ||
                        Math.abs(yAcc - sensorEvent.values[1]) > 3f ||
                        Math.abs(zAcc - sensorEvent.values[2]) > 3f) {
                    xAcc = sensorEvent.values[0];
                    yAcc = sensorEvent.values[1];
                    zAcc = sensorEvent.values[2];
                    deviceInteraction();
                }

                // check if the device has moved to to register a fix
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    double x = sensorEvent.values[0];
                    double y = sensorEvent.values[1];
                    double z = sensorEvent.values[2];

                    if (x != 0 || y != 0 | z != 0)
                        hasAcc = true;

                    if ((x - lastX) >= 1 || (x - lastX) <= -1
                            || (y - lastY) >= 1 || (y - lastY) <= -1
                            || (z - lastZ) >= 1 || (z - lastZ) <= -1)
                        moved = true;

                    lastX = sensorEvent.values[0];
                    lastY = sensorEvent.values[1];
                    lastZ = sensorEvent.values[2];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        waitDialog = new PleaseWaitDialog(this);

        openNewCourseDialog = new GolfAlertDialog(this);
        openNewCourseDialog.setTitle(getString(R.string.info_popup));
        openNewCourseDialog.setOkText(getString(R.string.yes));
        openNewCourseDialog.setBottomBtnText(getString(R.string.no));
        openNewCourseDialog.setCheckBoxVisibility(false);


        courseRefreshDialog = new GolfAlertDialog(this);
        courseRefreshDialog.setTitle(getString(R.string.info_popup));
        courseRefreshDialog.setCancelable(false);
        courseRefreshDialog.setMessage(getString(R.string.please_refresh_course));
        courseRefreshDialog.hideBottomButton();
        courseRefreshDialog.setOkText(getString(R.string.okay));
        courseRefreshDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                courseRefreshDialog.dissmiss();
            }

            @Override
            public void onBottomBtnClick() {

            }
        });

        if (PreferenceManager.getInstance().getCurrentCourseModel() != null) {
            isCourseSaved = PreferenceManager.getInstance().getCurrentCourseModel().getPolygon() != null;
        } else {
            isCourseSaved = false;
        }
        isCheckForZoneActive = true;

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                TMUtil.setTopMargin(mAppBar.getHeight());

                if (TMUtil.isTablet(getApplicationContext())) {
                    topMargin.getLayoutParams().height = TMUtil.getTopMargin();
                    topMargin.requestLayout();
                }
                if (!getSupportFragmentManager().getFragments().isEmpty()) {
                    View view = getSupportFragmentManager().getFragments().get(0).getView();
                    if (view != null) {
                        View mTopBar = view.findViewById(R.id.top_margin);
                        if (mTopBar != null) {
                            mTopBar.getLayoutParams().height = TMUtil.getTopMargin();
                            mTopBar.requestLayout();

                        }
                    }
                    mAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                if (fragment != null) {
                    addFragment(fragment);
                    fragment = null;
                }
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        FirebaseCrashlytics.getInstance().setUserId(TMUtil.getDeviceIMEI());

        setContactUsEnabled(false);
        setEmergencyEnabled(false);
        setGroupInfoEnabled(false);
        setMapEnabled(false);
        setSpeakersEnabled(false);
        String appVersion = "App: " + BuildConfig.VERSION_NAME;
        ((TextView) findViewById(R.id.tv_app_version)).setText(appVersion);

        View debugConsoleTrigger = findViewById(R.id.debug_console_trigger);

        debugConsoleTrigger.setOnLongClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DebugConsoleActivity.class);
            startActivity(intent);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                CartControlHelper.isCartControlLoggingActive = true;
            }, 500); // Adjust the delay as needed
            return true;
        });

        RestBoundsModel mapBounds = PreferenceManager.getInstance().getMapBounds();
        if (mapBounds != null) {
            String courseCode = mapBounds.getCourseCode();
            if (courseCode != null) {
                courseCode = "Code: " + courseCode;
                ((TextView) findViewById(R.id.tv_course_code)).setText(courseCode);
            } else {
                findViewById(R.id.tv_course_code).setVisibility(View.GONE);
            }

            Log.d("SNAP", "MainActivity mapBounds.getSnapToPlay = " + mapBounds.getSnapToPlay());
            if (mapBounds.getSnapToPlay() != null) { //Check if the snapToPlay object is not null, if not apply the hasMusic setting
                SnapToPlayHelper.hasMusic = mapBounds.getSnapToPlay().hasMusic();
            }

        } else {
            findViewById(R.id.tv_course_code).setVisibility(View.GONE);
        }

//        startLockTask();
        curBrightnessValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);

        Intent myService = new Intent(this, TeeRefresher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, myService, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        isCreated = true;
        presenter.startMaintenance();
        deviceInteraction();

        if (downloadManager == null)
            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        FirmwareUpdateModel firmwareUpdateModel = PreferenceManager.getInstance().getFirmwareUpdate();
        if (firmwareUpdateModel != null) {
            if (firmwareUpdateModel.getStatus() == FirmwareUpdateModel.DOWNLOADING) {
                long downloadID = firmwareUpdateModel.getDownloadID();
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadID));
                if (cursor.getCount() > 0) {
                    new DownloadFirmware().execute(downloadID);
                } else {
                    startDownload();
                }
            } else if (firmwareUpdateModel.getStatus() == FirmwareUpdateModel.INSTALLED) {
                String display = Build.DISPLAY;
                presenter.updateFirmwareStatus(100, display);
            }
        }

        CartControlHelper.setMainActivity(this);
        //snapToPlayStartupConfiguration();
        setSnapToPlayMenuItems();
    }

    void snapToPlayStartupConfiguration() {
        Log.d("SNAP", "snapToPlayStartupConfiguration audioID = " + PreferenceManager.getInstance().getAudioID());
        SnapToPlayHelper.powerContext = getBaseActivity().getApplicationContext(); //Initialize power context to allow speakers tb turned on and off

        if ("pairmode".equals(PreferenceManager.getInstance().getAudioID())) {
            Log.d("SNAP", "Pairmode detected");
            try {
                SnapToPlayHelper.pairSpeakersIfAvailable();
            } catch (Exception e) {
            }
            PreferenceManager.getInstance().setAudioID("");
        }

        setSnapToPlayMenuItems();
    }

    void writeToFile(String tag) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);


            String time = TMUtil.getTimeUTC(System.currentTimeMillis());
            String log = tag + " : " + time;
            writer.append(log);
            writer.append("\n");

            writer.close();
            fileOutputStream.close();
        } catch (Throwable e) {
            // Error writing to file
        }
    }

    public boolean moved() {
        return !hasAcc || moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public void deviceInteraction() {
        presenter.startInactiveStateTimer();
        if (PreferenceManager.getInstance().getDeviceType().equals(PreferenceManager.DEVICE_TYPE_CART)) {
            setKeepScreenOn(true);
            dimScreenHandler.removeCallbacks(dimScreenRunnable);
            dimScreenHandler.postDelayed(dimScreenRunnable, 1000);
        }

        if (golfService == null || !isBinded) {
            customBindService();
        }
        if (golfService != null) {
            golfService.allowToSendFixes = true;
            Log.d("FSSSSSSSSS", " ALLOW SEND FIXES = TRUE");
        }

        returnDimScreen();
        if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.inactivity_frame))
                    .commitAllowingStateLoss();
        }

        if (!inRound) {
            presenter.startInRoundFalseAccDelay();
        } else {
            presenter.startInRoundTrueAccDelay();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRedownloadSlides(RedownloadSlideShow event) {
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOnlineEvent(OnlineEvent event) {
        enableNetworkButtons(true);
        offlineDate = null;
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineEvent(OfflineEvent event) {
        enableNetworkButtons(false);
        offlineDate = event.getSetDate();
        EventBus.getDefault().removeStickyEvent(event);
    }


    private void enableNetworkButtons(boolean enable) {
        emergencyItem.setActive(enable);
        contactUsItem.setActive(enable);
        drinksCart.setActive(enable);
        foodAndBevItem.setActive(enable);
        changeCourseItem.setActive(enable);
        leaderItem.setActive(enable);
    }

    @Override
    public void showMaintenanceFragment() {
        hideMapButton();
        backToRootFragmentOrAddRootFragmentIfNotExistsNoResume(MaintenanceFragment.class);
    }

    public void hideMapButton() {
        mapItem.setVisibility(View.GONE);
    }

    public void showMapButton() {
        if (mapItem != null) {
            mapItem.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            return true;

        } else {
            super.onKeyDown(keyCode, event);
            return true;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCartMoving(MovingCartEvent event) {
        presenter.startInactiveStateTimer();
        presenter.startDimScrenTimer();

        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlertsEvent(FirebaseGetAlerts event) {
        presenter.getAlerts();
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMaintenanceGet(SetMaintenanceEvent event) {
        presenter.startMaintenance();
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMaintenanceStop(StopMaintenanceEvent event) {
        presenter.stopMaintenance();
        EventBus.getDefault().removeStickyEvent(event);
    }

    public void addRebindCallback(RebindServiceCallback callback) {
        callbacks.add(callback);
    }

    public void removeRebindCallback(RebindServiceCallback callback) {
        callbacks.remove(callback);
    }

    @Override
    protected void onDestroy() {
        dimScreenHandler.removeCallbacks(dimScreenRunnable);
        if (golfServiceConnected && mConnection != null) {
            if (getGolfService() != null) {
                getGolfService().unbindCallback(this);
                getGolfService().disableService();
                stopService(golfServiceIntent);
//                golfService.stopSelf();
                if (isBinded) {
                    unbindService();
                }
                Log.d("DESTROY SERVICE", "SERVICE DESTROYED");
            }

        }
        callbacks.clear();
        presenter.dispose();
        unbinder.unbind();
        unbinder = null;
        Log.d("DESTROY SERVICE", "PRESENTER DISPOSED");
        super.onDestroy();
    }

    private void unbindService() {
        isBinded = false;
        try {
            unbindService(mConnection);
        } catch (Throwable e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) != null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.inactivity_frame))
                    .commitAllowingStateLoss();
        if (isCreated) {
            presenter.startInactiveStateTimer();
            presenter.startDimScrenTimer();
            deviceInteraction();
        }
        if (isCreated && (getSupportFragmentManager().getFragments() == null || getSupportFragmentManager().getFragments().isEmpty())) {
            if (!golfServiceConnected || getGolfService() == null) {
                Log.i(getClass().getName(), "onResume: golfService == null");
                FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onResume: golfService == null");
                customBindService();
            } else {
                if (golfServiceConnected) {
                    Log.i(getClass().getName(), "onResume: openStartScreen - golfServiceConnected = true");
                    FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onResume: openStartScreen - golfServiceConnected = true");
                }
                if (getGolfService() == null) {
                    Log.i(getClass().getName(), "onResume: openStartScreen - getGolfService = null");
                    FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onResume: openStartScreen - getGolfService = null");
                }
                Log.i(getClass().getName(), "onResume: else openStartScreen ");
                FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "onResume: else openStartScreen ");
                openStartScreen();
            }
        }

    }

    private void customBindService() {
        if (golfServiceIntent == null) {
            golfServiceIntent = new Intent(getApplicationContext(), GolfService.class);
        }

        if (!isBinded) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(golfServiceIntent);
//            } else {
//                startService(golfServiceIntent);
//            }
            bindService(golfServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void dimScreen(Long aLong) {
        changeScreenBrightness(0);
    }

    @Override
    public void returnDimScreen() {
        changeScreenBrightness(255);
    }

    private void changeScreenBrightness(int screenBrightnessValue) {
        Context context = getApplicationContext();
        boolean settingsCanWrite = hasWriteSettingsPermission(context);
        if (settingsCanWrite) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessValue);
        } else {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            if (screenBrightnessValue == 0)
                lp.screenBrightness = 0;
            else
                lp.screenBrightness = 1;
            getWindow().setAttributes(lp);
        }
    }

    private boolean hasWriteSettingsPermission(Context context) {
        try {
            return Settings.System.canWrite(context);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    protected void onPause() {
        if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) != null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.inactivity_frame))
                    .commitAllowingStateLoss();
        presenter.stopInactiveTimer();
        deviceInteraction();
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) != null && getSupportFragmentManager().findFragmentById(R.id.inactivity_frame).isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.inactivity_frame))
                    .commitAllowingStateLoss();
        } else if (getSupportFragmentManager().findFragmentByTag(FragmentMap.class.getName()) != null &&
                getSupportFragmentManager().findFragmentByTag(FragmentMap.class.getName()).isVisible()) {
        } else if (getSupportFragmentManager().findFragmentByTag(FragmentAdmin.class.getName()) != null &&
                getSupportFragmentManager().findFragmentByTag(FragmentAdmin.class.getName()).isVisible() &&
                ((FragmentAdmin) getSupportFragmentManager().findFragmentByTag(FragmentAdmin.class.getName())).getIsNullModel()) {
            backToRootFragmentOrAddRootFragmentIfNotExists(FragmentGroupsPager.class);
        } else {
            FragmentMap map = null;
            if (getSupportFragmentManager().findFragmentByTag(FragmentAlert.class.getName()) != null &&
                    getSupportFragmentManager().findFragmentByTag(FragmentAlert.class.getName()).isVisible()) {
                map = (FragmentMap) getSupportFragmentManager().findFragmentByTag(FragmentMap.class.getName());
            }
            super.onBackPressed();
            if (map != null) {
                map.clearHighPriorAlert();
            }
        }
    }

    /**
     * UI Listeners
     */
    @OnClick(R.id.emergency)
    void onEmergencyClick() {
        fragment = FragmentEmergency.getInstance(groupSafety);
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.refreshments)
    void onRefreshmentsClick() {
        fragment = new FragmentRefreshments();
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.measure)
    void onMeasureClick() {
        fragment = new FragmentMeasuringUnits();
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.report)
    void onReportClick() {
        fragment = new FragmentReportIssue();
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.contact)
    void onContactClick() {
        fragment = new FragmentContactUs();
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.group)
    void onGroupClick() {
        fragment = FragmentRoundInfo.getInstance(offlineDate);
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.call_drinks_cart_item)
    void onCallDrinksCartClick() {
        showCallDrinksCartDialog();
        mDrawer.closeDrawers();
    }

    private void showCallDrinksCartDialog() {
        GolfAlertDialog dialog = new GolfAlertDialog(this)
                .setCancelable(false)
                .setOkText(getString(R.string.confirm))
                .setBottomBtnText(getString(R.string.cancel))
                .setIcon(R.drawable.ic_info)
                .setMessage(getString(R.string.do_you_want_to_call_cart))
                .setTitle(getString(R.string.call_drinks_cart));

        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                presenter.callDrinksCart(TMUtil.getDeviceIMEI());
            }

            @Override
            public void onBottomBtnClick() {
                dialog.dissmiss();
            }
        });

        dialog.show();
    }

    public void refreshCourse() {
        if (PreferenceManager.getInstance().getCurrentCourseModel() != null) {
            isCourseSaved = PreferenceManager.getInstance().getCurrentCourseModel().getPolygon() != null;
        } else {
            isCourseSaved = false;
        }
    }

    public void enableCheckingForNewZone(boolean enable) {
        isCheckForZoneActive = enable;

    }

    @OnClick(R.id.map_item)
    void onMapClick() {
        mDrawer.closeDrawers();
        backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
    }

    @OnClick(R.id.score_item)
    void onScoreClick() {
        Intent intent = new Intent(MainActivity.this, ScoringActivity.class);
        startActivityForResult(intent, 1);
        mDrawer.closeDrawers();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            addScoreCardFragment(new ScoreCardFragment());
            showScore(false);
            EventBus.getDefault().postSticky(new ShowScoreEvent(false));
        }
    }

    @OnClick(R.id.leaderboard_item)
    void onLeaderBoardClick() {
        fragment = new LeaderBoardFragment();
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.food_and_bevs)
    void onFooAndBevsClick() {
        fragment = new FoodMenuFragment();
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.change_course)
    void onChangeCourseClick() {
        fragment = FragmentSelectCourse.newInstance(true);
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.enable_speakers)
    void onEnableSpeakersClick() {
        showSnapToPlayDialogs();
        SideMenuItem enableSpeakers = findViewById(R.id.enable_speakers);
        SideMenuItem disableSpeakers = findViewById(R.id.disable_speakers);
        enableSpeakers.setVisibility(View.GONE);
        disableSpeakers.setVisibility(View.VISIBLE);
        mDrawer.closeDrawers();
    }

    @OnClick(R.id.disable_speakers)
    void onDisableSpeakersClick() {
        SnapToPlayHelper.turnSnapToPlayPowerOff();
        SideMenuItem enableSpeakers = findViewById(R.id.enable_speakers);
        SideMenuItem disableSpeakers = findViewById(R.id.disable_speakers);
        enableSpeakers.setVisibility(View.VISIBLE);
        disableSpeakers.setVisibility(View.GONE);
        PreferenceManager.getInstance().setAudioID("");
        mDrawer.closeDrawers();
    }

    public void setSnapToPlayMenuItems() {
        SideMenuItem enableSpeakers = findViewById(R.id.enable_speakers);
        SideMenuItem disableSpeakers = findViewById(R.id.disable_speakers);

        if (PreferenceManager.getInstance().getSpeakerMac() == "") {
            //SnapToPlay not installed/not paired
            enableSpeakers.setVisibility(View.GONE);
            disableSpeakers.setVisibility(View.GONE);
        } else {
            if (PreferenceManager.getInstance().getAudioID() == "") {
                //SnapToPlay installed but not enabled
                enableSpeakers.setVisibility(View.VISIBLE);
                disableSpeakers.setVisibility(View.GONE);
            } else {
                //SnapToPlay installed and bluetooth enabled
                enableSpeakers.setVisibility(View.GONE);
                disableSpeakers.setVisibility(View.VISIBLE);
            }
        }

        enableSpeakers.setVisibility(View.GONE);
        disableSpeakers.setVisibility(View.GONE);
    }

    private void showSnapToPlayDialogs() {
        String audioID = SnapToPlayHelper.setNewAudioID();
        TextView bluetoothTextView = findViewById(R.id.bluetooth_text);
        bluetoothTextView.setText("TAGMARSHAL_" + audioID);
        ConstraintLayout snapToPlayDialogs = findViewById(R.id.bottom_snaptoplay_dialogs);
        if (snapToPlayDialogs != null) {
            snapToPlayDialogs.setVisibility(View.VISIBLE);
        }
        try {
            if (SnapToPlayHelper.currentActiveAudioID != audioID) {
                new Thread(() -> {
                    try {
                        SnapToPlayHelper.turnSnapToPlayAudioOn();
                    } catch (Exception e) {
                    }
                }).start();
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("SNAP", "Exception in showSnapToPlayDialogs:" + e.getMessage());
        }
    }

    @OnClick(R.id.admin_item)
    void onAdminClick() {
        DialogAdminEnterCreds dialog = new DialogAdminEnterCreds(this);
        dialog.setOnCredsCheckListener(this);
        dialog.show();

        mDrawer.closeDrawers();
    }

    @OnTouch({R.id.club_logo_iv, R.id.menu})
    boolean onLogoOrMenuClick(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (view.getId() == R.id.club_logo_iv)
                    logoIsTouch = true;
                else
                    menuIsTouch = true;

                if (logoIsTouch && menuIsTouch) {
                    doubleClick = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (view.getId() == R.id.club_logo_iv) {
                    touchLogo = System.currentTimeMillis();
                    logoIsTouch = false;
                } else {
                    touchMenu = System.currentTimeMillis();
                    menuIsTouch = false;
                }

                if (!doubleClick) {
                    onMenuClick();
                } else {
                    if (!logoIsTouch && !menuIsTouch) {
                        doubleClick = false;
                        detectDoubleTouch();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void onSuccessDrinksCartCall() {
        showPleaseWaitDialog(false);
        GolfAlertDialog dialog = new GolfAlertDialog(this)
                .hideBottomButton()
                .setCancelable(false)
                .setOkText(getString(R.string.ok_no_problem))
                .setIcon(R.drawable.ic_info)
                .setMessage(getString(R.string.request_sent))
                .setTitle(getString(R.string.call_drinks_cart));

        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                dialog.dissmiss();
            }

            @Override
            public void onBottomBtnClick() {
                new GolfSendMessageDialog(getParent()).show();
            }
        });

        dialog.show();
    }


    @OnClick(R.id.appbar)
    void onMenuClick() {
//        showSystemUI();
        if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
            mDrawer.closeDrawer(Gravity.RIGHT);
        } else {
            mDrawer.openDrawer(Gravity.RIGHT);
        }
    }

    /**
     * Implementations of MainActivity.class
     */
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        EventBus.getDefault().postSticky(new UserInteractionEvent());

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deviceInteraction(UserInteractionEvent event) {
        if (isCreated) {
            deviceInteraction();
        }
        EventBus.getDefault().removeStickyEvent(event);
    }


    @Override
    public void onSuccess() {
        addFragment(new FragmentAdmin());
        fragment = null;
    }

    @Override
    public void onFailure(String message) {
        fragment = null;
    }

    @Override
    public void onFail(String message) {
        if (!MainActivity.this.isFinishing()) {
            showPleaseWaitDialog(false);
        }
    }

    @Override
    public void onInactiveState() {

        openInactiveScreen();

    }

    private void openInactiveScreen() {
        if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.inactivity_frame, new FragmentInAcitivty(), "INACTIVE")
                    .commitAllowingStateLoss();
        }
    }

    private void openBlackScreen() {
        if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.inactivity_frame, FragmentInAcitivty.getInstance(true), "INACTIVE")
                    .commitAllowingStateLoss();
        } else if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) != null &&
                getSupportFragmentManager().findFragmentById(R.id.inactivity_frame).isVisible()) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.inactivity_frame, FragmentInAcitivty.getInstance(true), "INACTIVE")
                    .commitAllowingStateLoss();

        }
    }

    /**
     * Methods of MainActivity.class
     */
    private void detectDoubleTouch() {
        if (Math.abs(touchLogo - touchMenu) < 500 && touchMenu != 0 && touchLogo != 0) {
            addFragment(new FragmentAdmin());
        }
    }

    @Override
    public BaseFragment openStartScreen() {
        BaseFragment baseFragment;

        if (PreferenceManager.getInstance().getMaintenance() != null &&
                (PreferenceManager.getInstance().getMaintenance().getFromTime() < System.currentTimeMillis())) {
            backToRootFragmentOrAddRootFragmentIfNotExistsNoResume(MaintenanceFragment.class);
            return null;
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if (PreferenceManager.getInstance().getCourse() != null) {
            GolfAPI.bindBaseCourseUrl(PreferenceManager.getInstance().getCourse());
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);

            sendFirebaseToken();
        }

        RestInRoundModel inRoundModel =
                (RestInRoundModel) getIntent().getSerializableExtra(IntentManager.ACTIVITY_INTENT_ROUND_MODEL);

        if (inRoundModel != null && inRoundModel.isComplete() && !inRoundModel.isNineHole() &&
                !inRoundModel.isInRound() && !inRoundModel.isNullModel()) {
            baseFragment = new FragmentRoundEnd();
            ((FragmentRoundEnd) baseFragment).bindRoundModel(inRoundModel);
        } else if (getIntent().getBooleanExtra(IntentManager.ACTIVITY_INTENT_IN_ROUND, false)) {
            baseFragment = new FragmentMap();
        } else if (PreferenceManager.getInstance().isFirstLaunch()) {
            baseFragment = new FragmentDeviceSetup();
        } else {
            if (PreferenceManager.getInstance().getDeviceTag() != null &&
                    PreferenceManager.getInstance().getCourse() != null) {

                if (PreferenceManager.getInstance().getCourseGroupId() != null) {
                    baseFragment = new FragmentMap();
                } else {
                    baseFragment = new FragmentGroupsPager();
                }
            } else {
                if (PreferenceManager.getInstance().getCourse() != null &&
                        PreferenceManager.getInstance().getDeviceTag() != null) {
                    if (PreferenceManager.getInstance().getCourseGroupId() != null) {
                        baseFragment = new FragmentMap();
                    } else {
                        baseFragment = new FragmentGroupsPager();
                    }
                } else {
                    FirebaseMessaging.getInstance().setAutoInitEnabled(false);
                    PreferenceManager.getInstance().clearGameData("");

                    baseFragment = FragmentSelectCourse.newInstance(false);
                }
            }
        }

        if (baseFragment instanceof FragmentMap ||
                baseFragment instanceof FragmentGroupsPager) {
            loadClubLogo();
        }

        addFragment(baseFragment, false);
        return baseFragment;
    }

    @Override
    public void sendFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(getClass().getName(), "getInstanceId failed", task.getException());
                        return;
                    }
                    Map<String, String> token = new HashMap<>();
                    token.put("token", task.getResult().getToken());
                    presenter.sendFirebaseToken(token);
                });
    }

    public void setClearedData(boolean isCleared) {
        clearedData = isCleared;
    }


    public void setKeepScreenOn(boolean enable) {
        getWindow().getDecorView().setKeepScreenOn(enable);
    }

    public void showPleaseWaitDialog(boolean show) {
        if (show) {
            waitDialog.show();
        } else {
            waitDialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReregisterEvent(OnSuccessReregister event) {
        setKeepScreenOn(!PreferenceManager.getInstance().getDeviceType().equals(PreferenceManager.DEVICE_TYPE_CART));
        EventBus.getDefault().removeStickyEvent(event);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirmwareUpdateEvent(FirmwareUpdateEvent event) {
        startDownload();
        EventBus.getDefault().removeStickyEvent(event);
    }

    private void startDownload() {
        if (downloadManager == null)
            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        FirmwareUpdateModel firmwareUpdateModel = PreferenceManager.getInstance().getFirmwareUpdate();
        String fileName = firmwareUpdateModel.getFileName() + ".zip";

        String url = "https://central.tagmarshal.golf/firmware/" + fileName;
        File file = new File(Environment.getExternalStorageDirectory(), "/Pictures/firmware.zip");

        if (file.exists())
            file.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) // Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true); // Set if download is allowed on roaming network

        long downloadID = downloadManager.enqueue(request); // enqueue puts the download request in the queue.

        firmwareUpdateModel.setDownloadID(downloadID);
        PreferenceManager.getInstance().saveFirmwareUpdate(firmwareUpdateModel);

        new DownloadFirmware().execute(downloadID);
    }

    public void showAppbar(boolean show) {
        if (realAppBar != null) {
            if (show) {
                realAppBar.setVisibility(View.VISIBLE);
            } else {
                realAppBar.setVisibility(View.GONE);
            }
        }
    }

    public void setEmergencyEnabled(boolean enable) {
        ((SideMenuItem) findViewById(R.id.emergency)).setActive(enable);
    }

    public void setContactUsEnabled(boolean enable) {
        ((SideMenuItem) findViewById(R.id.contact)).setActive(enable);
    }

    public void setGroupInfoEnabled(boolean enable) {
        ((SideMenuItem) findViewById(R.id.group)).setActive(enable);
    }

    public void setMapEnabled(boolean enable) {
        ((SideMenuItem) findViewById(R.id.map_item)).setActive(enable);
    }

    public void setSpeakersEnabled(boolean enable) {
        ((SideMenuItem) findViewById(R.id.enable_speakers)).setActive(enable);
    }

    public void loadClubLogo() {
        try {
            if (logoAdded || PreferenceManager.getInstance().getCourse() == null && clubLogoIv == null) {
                FirebaseCrashlytics.getInstance().log("logo not added.");
                if (logoAdded) {
                    FirebaseCrashlytics.getInstance().log("Logo is lodded");
                } else if (PreferenceManager.getInstance().getCourse() == null) {
                    FirebaseCrashlytics.getInstance().log("getCourse is null");
                } else if (clubLogoIv == null) {
                    FirebaseCrashlytics.getInstance().log("clublogoIV is null");
                }
                return;
            }


            String url = PreferenceManager.getInstance().getMapBounds().getLogo();
            Glide.with(getApplicationContext()).load(url).signature(new ObjectKey(PreferenceManager.getInstance().getCourseLogo())).apply(RequestOptions.fitCenterTransform()).into(clubLogoIv);
            logoPlaceIv.setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void clearLogo() {
        PreferenceManager.getInstance().clearLogoData();
    }

    public GolfService getGolfService() {
        return golfService;
    }

    @Override
    public void onGetPaceInfo(RestInRoundModel inRoundModel) {
        PreferenceManager.getInstance().setRoundID(inRoundModel.getId());
        if (inRoundModel.isInRound()) {
            if (getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentById(R.id.inactivity_frame))
                        .commitAllowingStateLoss();
            }
        }

        if (inRoundModel.isInRound() && !inRound) {
            backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
            PreferenceManager.getInstance().setRatedRound(false);
        }

        if (inRound != inRoundModel.isInRound()) {
            inRound = inRoundModel.isInRound();
            deviceInteraction();
        }

        // Get Golf Genius info
        if (inRoundModel.isInRound() && inRoundModel.isGolfGenius()) {
            GolfGeniusModel golfGeniusModel = PreferenceManager.getInstance().getGolfGenius();
            if (golfGeniusModel != null && golfGeniusModel.getRoundId().equals(inRoundModel.getId())) {
                boolean show = !golfGeniusModel.isComplete();
                showScore(show);
                showLeaderBoard(true);
                EventBus.getDefault().postSticky(new ShowScoreEvent(show));

            } else {
                PreferenceManager.getInstance().saveFailedScores(new ArrayList<>());
                presenter.getGolfGeniusInfo(inRoundModel.getId());
                showScore(false);
                showLeaderBoard(false);
                EventBus.getDefault().postSticky(new ShowScoreEvent(false));
            }
        }
    }

    @Override
    public void onGetHoleDistanceFromPoint(RestHoleDistanceModel distanceModel) {

    }

    @Override
    public void onGetAlerts(List<RestAlertModel> alertsList) {
        if (!isResumed) return;

        if (alertsList.size() == 0) return;
        RestAlertModel alert = alertsList.get(0);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentAlert.class.getName());
        if (fragment != null && fragment.isVisible() && fragmentAlert != null) {
            fragmentAlert.updateAlert(alert);
            fragmentAlert.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                @Override
                public void onOkClick() {
                    presenter.gotAlert(alert.getId());
                }

                @Override
                public void onBottomBtnClick() {
                    GolfSendMessageDialog golfSendMessageDialog = new GolfSendMessageDialog(MainActivity.this);
                    golfSendMessageDialog.show();
                }
            });
        } else {
            fragmentAlert = new FragmentAlert();
            fragmentAlert.setOkText(getString(R.string.ok_no_problem));
            fragmentAlert.setWhatText(getString(R.string.request_assistance));
            fragmentAlert.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                @Override
                public void onOkClick() {
                    presenter.gotAlert(alert.getId());
                }

                @Override
                public void onBottomBtnClick() {
                    GolfSendMessageDialog golfSendMessageDialog = new GolfSendMessageDialog(MainActivity.this);
                    golfSendMessageDialog.show();
                }
            });
            fragmentAlert.bindAlert(alert);
            addFragment(fragmentAlert);
        }
    }

    @Override
    public void onGetGolfGeniusInfo(RestGolfGeniusModel golfGenius) {
        ArrayList<GolfGeniusModel.Score> scores = new ArrayList<>();
        List<GolfGeniusModel.Player> players = golfGenius.getPlayers();

        for (int i = 0; i < golfGenius.getHoles(); i++) {
            ArrayList<String> pNumbers = new ArrayList<>();
            ArrayList<Integer> pScores = new ArrayList<>();
            ArrayList<Boolean> noScores = new ArrayList<>();

            assert players != null;
            for (GolfGeniusModel.Player player : players) {
                pNumbers.add(player.getNumber());
                pScores.add(0);
                noScores.add(false);
            }

            scores.add(new GolfGeniusModel.Score(
                    pNumbers,
                    pScores,
                    noScores,
                    golfGenius.getDescByIndex(i),
                    i + 1
            ));

        }

        GolfGeniusModel model = new GolfGeniusModel(
                golfGenius.getRoundId(),
                players,
                scores
        );

        showScore(true);
        showLeaderBoard(true);
        EventBus.getDefault().postSticky(new ShowScoreEvent(true));
        PreferenceManager.getInstance().saveGolfGenius(model);
    }

    @Override
    public void onLocationChanged(LatLng location, float accuracy, boolean inGeofence) {
        if (location != null && isCourseSaved && presenter != null) {
            presenter.checkForNewCourseEnter(location);
        }
    }

    @Override
    public void changeCurrentName(String courseName) {
        currentCourseName = courseName;
    }

    @Override
    public void onInRoundFalseInteractionDelayTick() {
        Log.d("tag", "delay tick");
        if (golfService != null && mConnection != null) {
            golfService.allowToSendFixes = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (isBinded) {
                    unbindService();
                }
                golfService.stopForeground(true);
                golfService.stopService(golfServiceIntent);
//                golfService.stopSelf();
                Log.d("tag", "stop service called ");
            } else {
                if (isBinded) {
                    unbindService();
                }
                stopService(golfServiceIntent);
            }
            golfService = null;
        }

        openBlackScreen();
        dimScreen(0L);
    }

    @Override
    public void onInRoundTrueInteractionDelayTick() {
        if (golfService != null) {
            golfService.allowToSendFixes = false;
            Log.d("inRoundTrue", "allowToSendFixes = false");
        }
    }

    @Override
    public void showCourseChangeAlert(CourseModel newCourse) {
        if (!openNewCourseDialog.isShowing() && isChangeCourseDelayActivated || !currentCourseName.equals(newCourse.getCourseName())) {
            changeCurrentName(newCourse.getCourseName());
            presenter.startTimerToChangeCourse(newCourse);
            openNewCourseDialog.setMessage("Do you want to change course to " + newCourse.getCourseName());
            openNewCourseDialog.setCheckBoxVisibility(false);
            openNewCourseDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                @Override
                public void onOkClick() {
                    presenter.changeCourse(newCourse);
                    changeCourseNotificationDelayStatus(true);

                    openNewCourseDialog.dissmiss();
                }

                @Override
                public void onBottomBtnClick() {
                    presenter.stopTimerToChangeCourse();
                    changeCourseNotificationDelayStatus(false);
                    if (openNewCourseDialog.isChecked()) {
                        presenter.stopChangeCourseDelay();
                    } else {
                        presenter.startChangeCourseDelay();
                    }
                    openNewCourseDialog.dissmiss();
                }
            });

            openNewCourseDialog.show();
        }
    }

    @Override
    public void changeCourseNotificationDelayStatus(boolean isActive) {
        isChangeCourseDelayActivated = isActive;
    }

    @Override
    public void dissmissNewCourseDialog() {
        if (openNewCourseDialog.isShowing()) {
            openNewCourseDialog.dissmiss();
        }
    }

    @Override
    public void clearFragments() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void openMapFragment() {
        loadClubLogo();
        backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
    }

    @Override
    public void openMapFragment(boolean clearedLogo) {
        backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
    }

    @Override
    public void onCanUpdateDistanceFromMeToCurrentHole(RestHoleDistanceModel distanceModel) {

    }

    @Override
    public void onFocusBetweenMeAndHole() {

    }

    private void showSendMessageDialog() {
        GolfSendMessageDialog dialog = new GolfSendMessageDialog(this);
        dialog.show();
    }

    public void showCallDrinksCart(boolean drinksActive) {
        if (drinksActive) {
            drinksCart.setVisibility(View.VISIBLE);
        } else {
            drinksCart.setVisibility(View.GONE);
        }
    }

    public void showScore(boolean scoreActive) {
        if (scoreItem != null) {
            if (scoreActive) {
                scoreItem.setVisibility(View.VISIBLE);
            } else {
                scoreItem.setVisibility(View.GONE);
            }
        }
    }

    public void startTimerSendToClubNotification(Map<String, String> body) {
        presenter.startTimerSendToClubNotification(body);
    }

    public void showLeaderBoard(boolean leaderActive) {
        if (leaderItem != null) {
            if (leaderActive) {
                leaderItem.setVisibility(View.VISIBLE);
            } else {
                leaderItem.setVisibility(View.GONE);
            }
        }
    }

    public void setGroupSafety(boolean groupSafety) {
        this.groupSafety = groupSafety;
    }

    public void showFoodAndBevs(boolean foodAndBevsActive) {
        if (foodAndBevsActive) {
            foodAndBevItem.setVisibility(View.VISIBLE);
        } else {
            foodAndBevItem.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReSaveSuccess() {
        EventBus.getDefault().postSticky(new MapBoundsEvent());
    }

    @Override
    public void onUpdateFirmwareStatus(ResponseBody responseBody, String status) {
        FirmwareUpdateModel model = PreferenceManager.getInstance().getFirmwareUpdate();
        if (model.getStatus() == FirmwareUpdateModel.INSTALLED) {
            PreferenceManager.getInstance().saveFirmwareUpdate(null);
        } else startUpdate();
    }

    @Override
    public void onUpdateFirmwareStatus(Throwable throwable, String status) {
        startUpdate();
    }

    private void startUpdate() {
        FirmwareUpdateModel model = PreferenceManager.getInstance().getFirmwareUpdate();
        if (model != null && model.getStatus() == FirmwareUpdateModel.DOWNLOAD_COMPLETE) {
            model.setStatus(FirmwareUpdateModel.INSTALLED);
            PreferenceManager.getInstance().saveFirmwareUpdate(model);
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.soten.otatest");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public void updateAppVersionWithCartControlInformation(String cartControlInformation) {
        String appVersion = "App: " + BuildConfig.VERSION_NAME;
        appVersion = appVersion + cartControlInformation;
        ((TextView) findViewById(R.id.tv_app_version)).setText(appVersion);
    }

    private class DownloadFirmware extends AsyncTask<Long, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            long downloadID = params[0];
            boolean finishDownload = false;
            int progress;
            while (!finishDownload) {
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadID));
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    switch (status) {
                        case DownloadManager.STATUS_FAILED: {
                            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                            int reason = cursor.getInt(columnReason);
                            presenter.updateFirmwareStatus(-1, "failed");
                            finishDownload = true;
                            break;
                        }
                        case DownloadManager.STATUS_PAUSED:
                            presenter.updateFirmwareStatus(-1, "paused");
                            break;
                        case DownloadManager.STATUS_PENDING:
                            presenter.updateFirmwareStatus(0, "pending");
                            break;
                        case DownloadManager.STATUS_RUNNING: {
                            final long total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            if (total >= 0) {
                                final long downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                progress = (int) ((downloaded * 100L) / total);
                                publishProgress(progress);
                            }
                            break;
                        }
                        case DownloadManager.STATUS_SUCCESSFUL: {
                            progress = 100;
                            finishDownload = true;
                            FirmwareUpdateModel model = PreferenceManager.getInstance().getFirmwareUpdate();
                            model.setProgress(progress);
                            model.setStatus(FirmwareUpdateModel.DOWNLOAD_COMPLETE);
                            PreferenceManager.getInstance().saveFirmwareUpdate(model);
                            presenter.updateFirmwareStatus(progress, "download-completed");
                            break;
                        }
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            FirmwareUpdateModel model = PreferenceManager.getInstance().getFirmwareUpdate();
            for (int i = model.getProgress(); i < 100; i += 10) {
                if (progress[0] >= i && model.getProgress() != i) {
                    model.setProgress(i);
                    PreferenceManager.getInstance().saveFirmwareUpdate(model);
                    presenter.updateFirmwareStatus(i, "downloading");
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
        }
    }
}