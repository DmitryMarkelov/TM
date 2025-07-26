package com.tagmarshal.golf.fragment.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.MapsInitializer.Renderer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.gg_scoring.ScoringActivity;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.callback.RebindServiceCallback;
import com.tagmarshal.golf.constants.GolfConstants;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.dialog.AdvertisementDialog;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.dialog.GolfSendMessageDialog;
import com.tagmarshal.golf.eventbus.ActivateGeoFenceByIdEvent;
import com.tagmarshal.golf.eventbus.CartIsMovedAd;
import com.tagmarshal.golf.eventbus.ClearTeeEvent;
import com.tagmarshal.golf.eventbus.CourseHolesEvent;
import com.tagmarshal.golf.eventbus.DeactivateGeoFenceByIdEvent;
import com.tagmarshal.golf.eventbus.EventMeasureUnit;
import com.tagmarshal.golf.eventbus.GeoFencesUpdateEvent;
import com.tagmarshal.golf.eventbus.MapBoundsEvent;
import com.tagmarshal.golf.eventbus.OfflineEvent;
import com.tagmarshal.golf.eventbus.OnlineEvent;
import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.eventbus.ServiceOnline;
import com.tagmarshal.golf.eventbus.ShowScoreEvent;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.alert.FragmentAlert;
import com.tagmarshal.golf.fragment.calibration.FragmentCalibrationMode;
import com.tagmarshal.golf.fragment.dataentry.FragmentDataEntry;
import com.tagmarshal.golf.fragment.roundend.FragmentRoundEnd;
import com.tagmarshal.golf.fragment.roundrate.RoundRateFragment;
import com.tagmarshal.golf.fragment.waiver.FragmentWaiver;
import com.tagmarshal.golf.manager.GameManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.DataEntryStatus;
import com.tagmarshal.golf.rest.model.Disclaimer;
import com.tagmarshal.golf.rest.model.DisclaimerStatus;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.rest.model.TMDevice;
import com.tagmarshal.golf.util.CartControlHelper;
import com.tagmarshal.golf.util.GeofenceHelper;
import com.tagmarshal.golf.util.SnapToPlayHelper;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.BottomLeftTimeView;
import com.tagmarshal.golf.view.TMTextView;
import com.tagmarshal.golf.view.TMTimerView;

import org.apache.commons.collections4.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class FragmentMap extends BaseFragment implements OnMapReadyCallback, FragmentMapContract.View,
        GolfCallback.GolfServiceListener, GoogleMap.OnMapClickListener, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveStartedListener, RebindServiceCallback, OnMapsSdkInitializedCallback {

    @BindView(R.id.btn_current_location)
    ConstraintLayout mCurrentLocation;

    @BindView(R.id.hole_number)
    TMTextView mHoleNumber;

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.top_margin_2)
    View mTopMargin2;

    @BindView(R.id.top_margin3)
    View mTopMargin3;

    @BindView(R.id.left_time_view)
    BottomLeftTimeView mLeftTimeView;

    @BindView(R.id.holes_container)
    ConstraintLayout mHolesContainer;

    @BindView(R.id.right_timer)
    TMTimerView mRightTimer;

    @BindView(R.id.back_distance)
    TMTextView mBackDistance;

    @BindView(R.id.tag)
    TMTextView mTag;

    @BindView(R.id.measure_unit)
    TMTextView mMeasureUnit;

    @BindView(R.id.center_distance)
    TMTextView mCenterDistance;

    @BindView(R.id.center_text)
    TMTextView mCenter;

    @BindView(R.id.front_distance)
    TMTextView mFrontDistance;

    @BindView(R.id.left_timer)
    TMTimerView mLeftTimer;

    @BindView(R.id.warning_container)
    ConstraintLayout mWarningContainer;

    @BindView(R.id.bottom_menu)
    ConstraintLayout mBottomMenu;

    @BindView(R.id.progress_bar)
    View progressBar;

    @BindView(R.id.hole_info)
    TMTextView mHolePar;

    @BindView(R.id.btn_score)
    ConstraintLayout scoreBtn;

    @BindView(R.id.score_icon)
    ImageView scoreIV;

    @BindView(R.id.offline_tv)
    TMTextView offlineTv;

    @BindView(R.id.image)
    ImageView image;

    @BindView(R.id.side_bar)
    ConstraintLayout sideBar;

    @BindView(R.id.geofence_alert)
    ConstraintLayout geofenceAlert;

    @BindView(R.id.geofence_alert_top)
    ConstraintLayout geofenceAlertTopSection;

    @BindView(R.id.geofence_alert_message)
    TextView geofenceAlertMessage;

    @BindView(R.id.btn_close_snaptoplay_bluetooth)
    Button btnCloseSnapToPlayBluetooth;

    @BindView(R.id.bottom_snaptoplay_dialogs)
    ConstraintLayout bottomSnapToPlayDialogs;

    @BindView(R.id.btn_snaptoplpay_bluetooth_continue)
    Button btnSnapToPlayBluetoothContinue;

    @BindView(R.id.activate_range_finding)
    LinearLayout activateRangeFinding;

    @BindView(R.id.range_finding_content)
    ConstraintLayout rangeFindingContent;

    @BindView(R.id.top_content)
    ConstraintLayout topContent;

    @BindView(R.id.bottom_content)
    ConstraintLayout bottomContent;

    @BindView(R.id.advertisementPager)
    ViewPager2 bannersViewPager;

    Unbinder unbinder;
    public static FragmentMap instance; //Allow Cart control to call methods in this class
    protected final ArrayList<Marker> holesMarkers = new ArrayList<>();
    protected final ArrayList<GroundOverlay> holesDots = new ArrayList<>();
    protected final ArrayList<Marker> teesMarkers = new ArrayList<>();
    private final ArrayMap<String, Polygon> geofencePolygonsList = new ArrayMap<>();
    private final ArrayMap<String, RestGeoFenceModel> geoFencesMap = new ArrayMap<>();
    private final ArrayMap<Integer, Polygon> geoZonesPolygonsList = new ArrayMap<>();
    private final Map<TMDevice, Marker> devicesMap = new HashMap<>();
    private GoogleMap mMap;
    private LatLngBounds boundOfArea;
    private LatLng tappedPoint;
    private LatLng myLocationPoint;
    private LatLng myLocationTappedPoint;
    private boolean inRound = false;
    private boolean addingPolyline;
    private boolean fiveMinutesLeftToStartGame = false;
    private boolean focusOnTheHole = false;
    private boolean halfWay = false;
    private boolean focusFromMe = true;
    private boolean startMoving = false;
    private boolean manualChangingHole = false;
    private boolean enteredRestrictedZone = false;
    private boolean globalIsLate = false;
    private boolean hasGeoAlertTriggered = false;
    private boolean hasCartBreached = false;
    private float startMovingZoom = 0f;
    private int playedSeconds = -1;
    private int oldPlayTime = -1;
    private Polyline polyline;
    private GroundOverlay tappedPointImage;
    private Marker myLocationMarker;
    private Marker distanceToFrontMarker;
    private Marker distanceToBackMarker;
    private Handler timer;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (playedSeconds != -1) {
                playedSeconds++;
                mRightTimer.setTime(TMUtil.getHourAndSecondsTimeFromSecs(playedSeconds));
            }
            timer.postDelayed(this, 1000);
        }
    };
    private FragmentMapPresenter presenter;
    private int currentGeoZoneId = 0;
    private RestHoleModel currentHoleModel;
    private FragmentAlert restrictedZoneFragmentAlert;
    private RestInRoundModel currentRoundModel;
    private boolean isRateRoundActive = false;
    private boolean isOnline = true;
    private boolean firstRequest = false;
    private String restrictedZoneId;
    private int firstRestrictedZoneEntries = 0;
    private GolfAlertDialog transactionDialog;
    private String currentDistanceToHole;
    private FragmentAlert geoFenceAlert;
    private int pressedNoHole;
    private String advertiseId = "";
    private int currentStayHole;
    private int prevIndex;
    private boolean isOutOfSequenceActive;
    private boolean lastCheckInCourse = true;
    private List<Integer> startHoles = Arrays.asList(1, 10);
    private boolean isCalibrationMode = false;
    private int calibrationPrevIndex;
    private int currentSection = 0;
    private Handler geoCountDownChecker = new Handler();
    private Disclaimer mDisclaimer;
    private Handler handler = new Handler();
    private int currentBannerIndex = 0;
    private AdvertisementBannerAdapter advertisementBannerAdapter;
    private Boolean disclaimerNeedsToShow = true;

    //method used to test showing a waiver if a certain time has passed
    //original method is isMoreThanOneHourOld to show the waiver after an hour if the round hasn't started
    public static boolean isMoreThanOneMinuteOld(long lastShownTime) {
        long currentTime = System.currentTimeMillis();
        long differenceInMillis = currentTime - lastShownTime;
        long differenceInMinutes = differenceInMillis / (1000 * 60);

        return differenceInMinutes > 1;
    }

    public static boolean isMoreThanOneHourOld(long lastShownTime) {
        long currentTime = System.currentTimeMillis();
        long differenceInMillis = currentTime - lastShownTime;
        long differenceInHours = differenceInMillis / (1000 * 60 * 60);

        return differenceInHours > 1;
    }

    //checks if 30min has passed and is used to get adverts every 30 min
    public static boolean isMoreThanThirtyMinutesOld(long lastShownTime) {
        long currentTime = System.currentTimeMillis();
        long differenceInMillis = currentTime - lastShownTime;
        long differenceInMinutes = differenceInMillis / (1000 * 60);

        return differenceInMinutes > 30;
    }

    private boolean checkFragmentAdded() {
        if (!isAdded()) {
            if (getGolfService() != null) {
                getGolfService().unbindCallback(this);
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        instance = this; //Allow Cart control to call methods in this class

        MapsInitializer.initialize(requireContext(), Renderer.LATEST, this);
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        unbinder = ButterKnife.bind(this, view);
        presenter = new FragmentMapPresenter(this);
        transactionDialog = new GolfAlertDialog(getActivity());
        transactionDialog.setOkText(getString(R.string.yes));
        transactionDialog.setTitle(getString(R.string.hole_change));
        transactionDialog.setBottomBtnText(getString(R.string.no));
        bannersViewPager.setUserInputEnabled(false);
        advertisementBannerAdapter = new AdvertisementBannerAdapter(getContext(), new ArrayList<>());
        bannersViewPager.setAdapter(advertisementBannerAdapter);
        presenter.getCourses();

        timer = new Handler();
        getBaseActivity().addRebindCallback(this);

        mMeasureUnit.setText(PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER) ?
                getString(R.string.meters) : getString(R.string.yards));
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        mTopMargin2.getLayoutParams().height = TMUtil.getTopMargin();
        mTopMargin3.getLayoutParams().height = TMUtil.getTopMargin();

        showHoles();

        mDisclaimer = PreferenceManager.getInstance().getDisclaimer();

        try {
            if (mDisclaimer == null || mDisclaimer.getStatus() != DisclaimerStatus.ACCEPTED || mDisclaimer.getDataEntryStatus() != DataEntryStatus.SUBMITTED) {
                rangeFindingContent.setVisibility(View.VISIBLE);
                topContent.setVisibility(View.GONE);
                bottomContent.setVisibility(View.GONE);
            } else {
                rangeFindingContent.setVisibility(View.GONE);
                //top content refers to the Range finding hole details
                topContent.setVisibility(View.VISIBLE);
                //bottom content refers to the Range finding content below the hole details
                bottomContent.setVisibility(View.VISIBLE);
            }

            //if (mDisclaimer != null && mDisclaimer.getStatus() == DisclaimerStatus.NONE && currentRoundModel == null) {
            if (mDisclaimer != null && disclaimerNeedsToShow) {
                mDisclaimer.setStatus(DisclaimerStatus.NONE);
                disclaimerNeedsToShow = false;
            }

            if (mDisclaimer != null && mDisclaimer.getStatus() == DisclaimerStatus.NONE) {
                Log.d("GEOGT", "mDisclaimer is not null");
                Log.d("GEOGT", "mDisclaimer.getStatus: " + mDisclaimer.getStatus());
                Log.d("GEOGT", "----- SHOWING WAIVER");

                if (!mDisclaimer.getText().isEmpty() && mDisclaimer.isActive()) {
                    showWaiverFragment();
                }
            } else {
                if (mDisclaimer == null) Log.d("GEOGT", "mDisclaimer IS NULL");
                Log.d("GEOGT", "mDisclaimer.getStatus: " + mDisclaimer.getStatus());
            }

        } catch (Exception e) {
            Log.d("GEOGT", "FragmentMap - disclaimer onCreateView() - Exception: " + e.getMessage());
        }

        setupBottomBar();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("FragmentMap", "🔄 FragmentMap.onViewCreated() called");
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), "Error during loading map. Please restart App", Toast.LENGTH_SHORT).show();
        }

        getBaseActivity().setGroupInfoEnabled(true);
        getBaseActivity().setEmergencyEnabled(true);
        getBaseActivity().setContactUsEnabled(true);
        getBaseActivity().setMapEnabled(true);
        getBaseActivity().setSpeakersEnabled(true);
        getBaseActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);

        Log.d("FragmentMap", "🔄 About to call presenter.getAdvertisements() from onViewCreated");
        setLastAdvertisementFetch();
        presenter.getAdvertisements(getContext());
        startTimer();
        bannersViewPager.setVisibility(View.GONE);
        //checkSnapToPlayState();
    }

    private void checkSnapToPlayState() {
        Log.d("SNAP", "FragmentMap - checkSnapToPlayState() - SpeakerMac: " + PreferenceManager.getInstance().getSpeakerMac() + " AudioID: " + PreferenceManager.getInstance().getAudioID());
        if (PreferenceManager.getInstance().getSpeakerMac() != "") {
            if (PreferenceManager.getInstance().getAudioID() != "") {
                //Reset audio ID to this
                if (PreferenceManager.getInstance().getAudioID() != SnapToPlayHelper.currentActiveAudioID) {
                    try {
                        new Thread(() -> {
                            try {
                                SnapToPlayHelper.turnSnapToPlayAudioOn();
                            } catch (Exception e) {
                            }
                        }).start();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @OnClick(R.id.activate_range_finding)
    void OnActivateRangeFinding() {
        if (mDisclaimer.getStatus() != null && mDisclaimer.getStatus() != DisclaimerStatus.ACCEPTED) {
            showWaiverFragment();
        } else if (mDisclaimer.getDataEntryStatus() != DataEntryStatus.SUBMITTED) {
            showDataEntryFragment();
        }
    }

    private void showWaiverFragment() {
        addFragment(this, new FragmentWaiver(), true, FragmentWaiver.class.getName());
    }

    private void showDataEntryFragment() {
        if (mDisclaimer.getFields().isEmpty()) {
            Log.d("GEOGT", "--------showDataEntryFragment fields are empty");
        } else {
            addFragment(this, new FragmentDataEntry(), true, FragmentDataEntry.class.getName());
        }
    }

    @Override
    public void onMapsSdkInitialized(MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (checkFragmentAdded()) return;
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_style));
            googleMap.getUiSettings().setCompassEnabled(false);

            if (mMap != null) {
                mMap.clear();
                teesMarkers.clear();
                holesMarkers.clear();
                mMap = null;
            }
            mMap = googleMap;

            View map = getChildFragmentManager().findFragmentById(R.id.map).getView();
            if (map != null) {
                map.setPadding(0, TMUtil.getTopMargin() / 2, 0, 0);
            }

            if (getGolfService() != null && getGolfService().getLastKnownMyLocationLatLng() != null) {
                drawMyLocation(getGolfService().getLastKnownMyLocationLatLng());
            }

            mMap.setOnCameraIdleListener(this);
            mMap.setOnCameraMoveCanceledListener(this);
            mMap.setOnMapClickListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnCameraMoveStartedListener(this);

            presenter.getBoundsForMap();
            presenter.getTeeTimes();
            try {
                getGolfService().bindCallback(this);
            } catch (Throwable exception) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
            getHole(GameManager.getStartHole(), false);

            if (GameManager.getHole(GameManager.currentHole) != null) {
                showHoleInfo(GameManager.getHole(GameManager.currentHole));
            } else {
                Toast.makeText(getContext(), "Hole does not exists", Toast.LENGTH_SHORT).show();
            }
        }
        showAdvertisementBanners();
    }

    private void setLastAdvertisementFetch() {
        PreferenceManager.getInstance().setAdvertisementsLastFetch(System.currentTimeMillis());
    }

    private List<AdvertisementModel> getLocalAdvertisements() {
        return PreferenceManager.getInstance().getAdvertisements();
    }

    public void showAdvertisementBanners() {
        Log.d("FragmentMap", "🎯 showAdvertisementBanners() called");
        List<AdvertisementModel> localAdvertisements = getLocalAdvertisements();
        Log.d("FragmentMap", "📊 Local advertisements count: " + (localAdvertisements != null ? localAdvertisements.size() : 0));
        List<AdvertisementModel> bannersAdverts = new ArrayList<>();
        if (localAdvertisements != null) {
            for (AdvertisementModel advert : localAdvertisements) {
                Log.d("FragmentMap", "🔍 Checking ad: ID=" + advert.getId() + 
                      ", Type=" + advert.getType() + 
                      ", Enabled=" + advert.isEnabled() + 
                      ", Downloaded=" + advert.isDownloaded());
                if (advert.isEnabled() && advert.getType().equalsIgnoreCase("banner") && advert.isDownloaded()) {
                    Log.d("FragmentMap", "✅ Ad qualifies for banner display: " + advert.getId());
                    bannersAdverts.add(advert);
                } else {
                    Log.d("FragmentMap", "❌ Ad does not qualify for banner display: " + advert.getId());
                }
            }
            Log.d("FragmentMap", "📊 Qualifying banners count: " + bannersAdverts.size());
            if (bannersAdverts.isEmpty()) {
                Log.d("FragmentMap", "❌ No qualifying banners, hiding ViewPager");
                bannersViewPager.setVisibility(View.GONE);
            } else {
                Log.d("FragmentMap", "✅ Showing banners, count: " + bannersAdverts.size());
                // Check if the banner isEnabled
                if (bannersAdverts.get(0).isEnabled()) {
                    if (!new HashSet<>(bannersAdverts).equals(new HashSet<>(advertisementBannerAdapter.getAdvertisementList()))) {
                        advertisementBannerAdapter.setAdvertisementList(bannersAdverts);
                        advertisementBannerAdapter.notifyDataSetChanged();
                    }
                    if (bannersViewPager.getVisibility() != View.VISIBLE) {
                        startAutoSlide(bannersAdverts);
                        bannersViewPager.setVisibility(View.VISIBLE);
                    }
                } else {
                    bannersViewPager.setVisibility(View.GONE);
                }
            }
        } else {
            bannersViewPager.setVisibility(View.GONE);
        }
    }

    // Timer for auto switching banners
    private void startAutoSlide(List<AdvertisementModel> bannersAdverts) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentBannerIndex < bannersAdverts.get(0).getDownloadsUrls().size() - 1) {
                    currentBannerIndex++;
                } else {
                    currentBannerIndex = 0;
                }
                bannersViewPager.setCurrentItem(currentBannerIndex, true);

                // Schedule the next slide after the current display time
                handler.postDelayed(this, bannersAdverts.get(0).getDisplayTime() * 1000L);
            }
        }, bannersAdverts.get(0).getDisplayTime() * 1000L);
    }

    @Override
    public void onCameraMoveCanceled() {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        onMapClick(marker.getPosition());
        return true;
    }

    @Override
    public void onCameraIdle() {
        if (focusFromMe && startMoving && mMap != null) {
            startMoving = false;
            if (startMovingZoom == mMap.getCameraPosition().zoom) {
                focusFromMe = false;
            }
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE && mMap != null) {
            startMoving = true;
            startMovingZoom = mMap.getCameraPosition().zoom;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (addingPolyline || mMap == null || latLng == null)
            return;

        addingPolyline = true;

        clearMap();

        tappedPoint = new LatLng(latLng.latitude, latLng.longitude);

        if (currentHoleModel == null) {
            return;
        }

        if (GameManager.getCurrentPlayHole() == GameManager.currentHole && checkIsInSection() && myLocationPoint != null) {
            myLocationTappedPoint = new LatLng(myLocationPoint.latitude, myLocationPoint.longitude);

            drawRangeDistance(tappedPoint, myLocationTappedPoint);
            presenter.getHoleDistanceFromPointAndMyLocation(myLocationPoint, tappedPoint, currentHoleModel);
        } else {
            drawRangeDistance(tappedPoint, new LatLng(currentHoleModel.getStartPoint().get(0), currentHoleModel.getStartPoint().get(1)));
            presenter.getHoleDistanceFromPoint(tappedPoint, currentHoleModel);
        }
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (transactionDialog != null && transactionDialog.isShowing()) {
            transactionDialog.dissmiss();
        }
        if (getGolfService() != null) {
            getGolfService().unbindCallback(this);
        }

        if (timer != null) {
            timer.removeCallbacks(timerRunnable);
            timer = null;
        }

        if (mMap != null) {
            clearMap();
            clearMapZones();
            mMap.clear();
            teesMarkers.clear();
            holesMarkers.clear();
            mMap = null;
        }
        getBaseActivity().removeRebindCallback(this);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMeasureUnitChange(EventMeasureUnit eventMeasureUnit) {
        mMeasureUnit.setText(PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER) ?
                getString(R.string.meters) : getString(R.string.yards));

        double multiplier = 1.0936133;

        if (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER)) {
            mFrontDistance.setText(String.valueOf(Math.round(Integer.parseInt(mFrontDistance.getText().toString()) / multiplier)));
            mCenterDistance.setText(String.valueOf(Math.round(Integer.parseInt(mCenterDistance.getText().toString()) / multiplier)));
            mBackDistance.setText(String.valueOf(Math.round(Integer.parseInt(mBackDistance.getText().toString()) / multiplier)));
        } else {
            mFrontDistance.setText(String.valueOf(Math.round(Integer.parseInt(mFrontDistance.getText().toString()) * multiplier)));
            mCenterDistance.setText(String.valueOf(Math.round(Integer.parseInt(mCenterDistance.getText().toString()) * multiplier)));
            mBackDistance.setText(String.valueOf(Math.round(Integer.parseInt(mBackDistance.getText().toString()) * multiplier)));
        }

        if (tappedPoint != null) {
            onMapClick(tappedPoint);
        }
    }

    private void setupBottomBar() {
        mLeftTimeView.setVisibility(View.GONE);
        mRightTimer.setVisibility(View.GONE);

        if (TMUtil.getLeftMinutes(GameManager.getStartGameTime()) > 0) {
            mLeftTimeView.setVisibility(View.VISIBLE);
            presenter.startCountDownTimerToStartGame();
        } else {
            mRightTimer.setIcon(R.drawable.ic_delay);
            mRightTimer.setTime("00:00");
            mRightTimer.setTitle(getString(R.string.played));
        }

        mLeftTimer.setTime(GameManager.getStartGameTime());

        mTag.setText(PreferenceManager.getInstance().getDeviceTag());
    }

    @Override
    public void makeNotification() {
        TMUtil.vibrateAndMakeNoiseDevice(requireContext());
    }

    @Override
    public void onGetCourseBackground(Bitmap image) {
        if (checkFragmentAdded()) return;
//        getBaseActivity().getAllNearestCourses();
        if (image == null || mMap == null) {
            Toast.makeText(getContext(), getString(R.string.looks_like_there_are_some_issues_pls_restart_app), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(image))
                    .positionFromBounds(boundOfArea);

            mMap.addGroundOverlay(newarkMap);

            presenter.getAllHoles();

            presenter.getPointsOfInterest();

            presenter.startCheckGeoFences();

            drawGeoZones();
        } catch (Exception e) {
            Toast.makeText(getContext(), getString(R.string.looks_like_there_are_some_issues_pls_restart_app), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSlideShowDismiss() {
        if (checkFragmentAdded()) return;
        if (getChildFragmentManager().findFragmentByTag("advertisement_dialog") != null) {
            ((AdvertisementDialog) getChildFragmentManager().findFragmentByTag("advertisement_dialog")).dismissAllowingStateLoss();
        }
    }

    private void drawGeoZones() {
        for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
            geoZonesPolygonsList.put(model.getProperties().getNumber(),
                    mMap.addPolygon(model.getGeometry().getPolygon()));
            //draw geozones
//            geoZonesPolygonsList.get(model.get_id())
//                    .setStrokeColor(Color.parseColor(model.getProperties().getColor()));
//            geoZonesPolygonsList.get(model.get_id()).setVisible(true);
        }
    }

    @Override
    public void onGetGeoZones() {
        drawGeoZones();
    }

    private void getHole(int hole, boolean fromCurrentLocation) {
        this.focusOnTheHole = true;

        presenter.getHoleInfo(hole);
        if (hole == GameManager.getCurrentPlayHole() && fromCurrentLocation && checkIsInSection()) {
            focusFromMe = true;
            getGolfService().getForceDistanceFromMeToHole();
        } else {
            presenter.getHoleDistanceFromTee(hole);
        }
    }

    @Override
    public void onFail(String message) {
        if (checkFragmentAdded()) return;
    }

    private void clearMap() {
        tappedPoint = null;

        if (polyline != null)
            polyline.remove();

        if (tappedPointImage != null)
            tappedPointImage.remove();

        if (distanceToBackMarker != null)
            distanceToBackMarker.remove();

        if (distanceToFrontMarker != null)
            distanceToFrontMarker.remove();
    }

    @Override
    public void onGetFlags(Bitmap image) {
        if (checkFragmentAdded()) return;
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(image))
                .positionFromBounds(boundOfArea);

        mMap.addGroundOverlay(newarkMap);
    }

    private void showHoleInfo(final RestHoleModel holeModel) {
        if (checkFragmentAdded()) return;

        if (holeModel == null || mMap == null || !isAdded()) {
            Toast.makeText(getContext(), getString(R.string.looks_like_there_are_some_issues_pls_restart_app), Toast.LENGTH_LONG).show();
            return;
        }

        clearMap();

        if (holeModel.getPar() != null) {
            mHolePar.setText(getString(R.string.par_number, holeModel.getPar()));
        } else {
            mHolePar.setText(getString(R.string.par_number, "0"));
        }

        if (holeModel.checkIsNullRequiredFields()) {
            Toast.makeText(getContext(), getString(R.string.looks_like_there_are_problems_with_retreiving_of_hole_data), Toast.LENGTH_LONG).show();
            return;
        }

        focusOnTheHole(holeModel);

        mHoleNumber.setText(String.valueOf(holeModel.getHoleDisplay()));

        if (holeModel.getPin() != null) {
            mCenter.setText(getString(R.string.pin));
        } else {
            mCenter.setText(getString(R.string.center));
        }

    }

    private void focusOnTheHole(final RestHoleModel holeModel) {
        try {
            if (focusOnTheHole) {
                if (holeModel.getCenterPointLatLng() == null || holeModel.getBackPointLatLng() == null ||
                        holeModel.getFrontPointLatLng() == null || holeModel.getStartPointLatLng() == null) {
                    Toast.makeText(getContext(), getString(R.string.some_point_of_hole_is_null), Toast.LENGTH_LONG).show();
                    return;
                }

                LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();

                latlngBuilder.include(holeModel.getCenterPointLatLng());

                final float bearing;
                if (focusFromMe && checkIsInSection()) {
                    if (getGolfService().getLastKnownMyLocationLatLng() == null) {
                        Toast.makeText(getContext(), getString(R.string.cannot_get_device_location), Toast.LENGTH_SHORT).show();

                        bearing = TMUtil.getBearing(holeModel.getStartPointLatLng(), holeModel.getCenterPointLatLng());
                        latlngBuilder.include(holeModel.getStartPointLatLng());
                    } else {
                        bearing = TMUtil.getBearing(getGolfService().getLastKnownMyLocationLatLng(), holeModel.getCenterPointLatLng());
                        latlngBuilder.include(getGolfService().getLastKnownMyLocationLatLng());
                    }
                } else {
                    bearing = TMUtil.getBearing(holeModel.getStartPointLatLng(), holeModel.getCenterPointLatLng());
                    latlngBuilder.include(holeModel.getStartPointLatLng());
                }

                final LatLngBounds latLngBounds = latlngBuilder.build();

                CameraPosition pos = CameraPosition.builder()
                        .bearing(bearing)
                        .target(latLngBounds.getCenter())
                        .zoom(TMUtil.getBoundsZoomLevel(latLngBounds, getResources().getInteger(R.integer.map_zoom_value) - TMUtil.getNavigationHeight(getBaseActivity().getWindowManager()) / 3, 1f))
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 500, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (mMap == null) return;

                        Projection projection = mMap.getProjection();

                        Point point = projection.toScreenLocation(latLngBounds.getCenter());
                        Point pointCenter = projection.toScreenLocation(holeModel.getCenterPointLatLng());
                        projection.toScreenLocation(mMap.getProjection().getVisibleRegion().farLeft);
                        point.x *= 0.66f;
                        point.y += pointCenter.y - TMUtil.getTopMargin() / 2 - TMUtil.getHoleMarkerSize(getContext());
                        LatLng center = projection.fromScreenLocation(point);

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(center), 500, null);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        } catch (Throwable e) {
            Timber.e(e);
        }

    }

    @Override
    public void onGetHoleInfo(RestHoleModel holeModel, int position) {
        if (checkFragmentAdded()) return;
        currentHoleModel = holeModel;
        GameManager.currentHole = position;
        showHoleInfo(holeModel);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onGetMapBounds(RestBoundsModel bounds) {
        if (checkFragmentAdded()) return;
        if (bounds == null || mMap == null) {
            return;
        }

        if (bounds.getSWLAT() != null
                && bounds.getNELAT() != null
                && bounds.getSWLON() != null
                && bounds.getNELON() != null) {
            boundOfArea = new LatLngBounds(
                    new LatLng(Double.parseDouble(bounds.getSWLAT()), Double.parseDouble(bounds.getSWLON())),       // South west corner |_
                    new LatLng(Double.parseDouble(bounds.getNELAT()), Double.parseDouble(bounds.getNELON())));
        }

        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setMinZoomPreference(15f);
        if (checkIsTablet()) {
            mMap.setMaxZoomPreference(20f);
        } else {
            mMap.setMaxZoomPreference(19.5f);
        }

        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setLatLngBoundsForCameraTarget(boundOfArea);

        getBaseActivity().showCallDrinksCart(bounds.isDrinksActive());
        getBaseActivity().showFoodAndBevs(bounds.isFoodAndBeveragesActive());
        getBaseActivity().setGroupSafety(bounds.getGroupSafety());

        isRateRoundActive = bounds.isRateRoundActive();
        isOutOfSequenceActive = bounds.isOutOfSequenceActive() == null ? true : bounds.isOutOfSequenceActive();
        PreferenceManager.getInstance().setIsScoreActive(bounds.isScoreActive());
        PreferenceManager.getInstance().setIsLeaderBoardActive(bounds.isLeaderActive());
        PreferenceManager.getInstance().setCurrency(bounds.getCurrency());
        PreferenceManager.getInstance().setReturnDeviceStatus(bounds.getReturnDeviceActiveStatus());
        PreferenceManager.getInstance().saveKitchenTime(bounds.getKitchen());

        if (bounds.getStartHoles() != null && !bounds.getStartHoles().isEmpty()) {
            try {
                startHoles = bounds.getStartHoles();
            } catch (Throwable e) {
                startHoles = Arrays.asList(1, 10);
            }
        }

        presenter.getCourseBackground(bounds.getMap(), requireContext());
    }

    @Override
    public void onGetGeoFences(List<RestGeoFenceModel> geoFenceModels) {
        drawGeoFences();
    }

    private boolean checkIsTablet() {
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        return diagonalInches >= 7.0;
    }

    private void drawAdvertisement() {
        List<AdvertisementModel> adverts = getLocalAdvertisements();
        if (adverts != null) {
            for (AdvertisementModel advert : adverts) {
                if (advert.getSections() != null && !advert.getSections().isEmpty()) {
                    for (AdvertisementModel.Geometry advertSection : advert.getSections()) {
                        mMap.addPolygon(advertSection.getPolygons(Color.BLACK, Color.BLACK));
                    }
                }
            }
        }
    }

    private void drawGeoFences() {
        if (GeofenceHelper.ignoreAllGeofences) {

            //Ignore Geofences
            for (Polygon polygon : geofencePolygonsList.values()) {
                polygon.remove();
            }
            geofencePolygonsList.clear();
            geoFencesMap.clear();

        } else {

            //Allow Geofences
            for (Polygon polygon : geofencePolygonsList.values()) {
                polygon.remove();
            }

            geofencePolygonsList.clear();
            geoFencesMap.clear();

            List<RestGeoFenceModel> geofences = PreferenceManager.getInstance().getGeoFences();
            if (geofences != null && !geofences.isEmpty()
                    && (currentRoundModel == null || !currentRoundModel.isIgnoreGeofence())) {
                for (RestGeoFenceModel model : geofences) {
                    if (!model.isActive()) continue;

                    //Default colours
                    String strokeColor = "#FFFFFF";
                    String fillColor = "#00FFFFFF";
                    try {
                        strokeColor = model.getProperties().getColor();
                    } catch (IllegalArgumentException e) {
                        strokeColor = "#FFFFFF";
                    }

                    if (geoFencesMap.get(model.getId()) != null) {
                        Log.d("GEOGT", "GeoFence with ID " + model.getId() + " already exists.");
                        geoFencesMap.put(model.getId(), model);
                        try {
                            geofencePolygonsList.get(model.getId())
                                    .setFillColor(Color.parseColor(model.getProperties().getColor()));
                            geofencePolygonsList.get(model.getId())
                                    .setStrokeColor(Color.parseColor(strokeColor));

                        } catch (IllegalArgumentException e) {
                        }
                    } else {
                        try {
                            geoFencesMap.put(model.getId(), model);
                            //                      geofencePolygonsList.put(model.getId(), mMap.addPolygon(model.getGeometry()
                            //                                .getPolygons(Color.parseColor(model.getProperties().getColor()),
                            //                                        Color.parseColor(model.getProperties().getColor()))));
                            geofencePolygonsList.put(model.getId(), mMap.addPolygon(model.getGeometry()
                                    .getPolygons(Color.parseColor(strokeColor),
                                            Color.parseColor(fillColor))));
                        } catch (IllegalArgumentException e) {
                        }
                    }

                    Polygon polygon = geofencePolygonsList.get(model.getId());
                    if (polygon != null) {
                        polygon.setVisible(model.isVisible());
                    } else {
                        Log.e("GEOGT", "Polygon for geofence ID " + model.getId() + " is null.");
                    }
                }
            }
        }
    }

    @Override
    public void onGetTeeTimes(List<RestTeeTimesModel> teeTimesList) {

    }

    private void fillPaceInfoView(RestInRoundModel roundModel, boolean isFromOnline) {
        if (roundModel == null || !isAdded())
            return;
        if (roundModel.isInRound() && !inRound) {
            inRound = roundModel.isInRound();
            onCurrentLocationClick();
        }

        inRound = roundModel.isInRound();

        int color;
//        setBottomMenuHeight(getResources().getDimension(R.dimen.bottom_bar_height));
        if (roundModel.isNullModel() && GameManager.getStartGameTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            long paceTime = (long) TMUtil.timeToMilli(GameManager.getStartGameTime() + ":00") - TMUtil.timeToMilli(sdf.format(new Date()));
            if (paceTime < 0) {
                mRightTimer.setVisibility(View.INVISIBLE);
                mLeftTimeView.setVisibility(View.VISIBLE);
                mLeftTimeView.setTimeOnPace(TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(String.valueOf(paceTime).replace("-", ""))));
                color = ContextCompat.getColor(getContext(), R.color.red);
                mLeftTimeView.setRightText(getString(R.string.late));

                try {
                    GradientDrawable bgShape = (GradientDrawable) mTag.getBackground().getConstantState().newDrawable().mutate();
                    bgShape.setColor(color);
                    mTag.setBackground(bgShape);
                } catch (Throwable e) {
                    Timber.d(e);
                }
                mBottomMenu.setBackgroundColor(color);
            }
        } else if (roundModel.isNullModel() && roundModel.getPlayTime() == 0) {
            color = ContextCompat.getColor(getContext(), R.color.green);
            mLeftTimeView.setVisibility(View.GONE);
            mRightTimer.setVisibility(View.GONE);

            try {
                GradientDrawable bgShape = (GradientDrawable) mTag.getBackground().getConstantState().newDrawable().mutate();
                bgShape.setColor(color);
                mTag.setBackground(bgShape);
            } catch (Throwable e) {
                Timber.d(e);
            }
            mBottomMenu.setBackgroundColor(color);
        } else {
            if (!isFromOnline) {
                if (oldPlayTime != roundModel.getPlayTime()) {
                    oldPlayTime = roundModel.getPlayTime();
                    playedSeconds = roundModel.getPlayTime();
                }
            }

            mLeftTimeView.setVisibility(View.VISIBLE);
            mRightTimer.setVisibility(View.VISIBLE);

            mLeftTimeView.setTimeOnPace(Math.abs(roundModel.getPace()));

            mLeftTimer.setTime(roundModel.getStartTime());

            mRightTimer.setIcon(R.drawable.ic_delay);
            if (!isFromOnline) {
                mRightTimer.setTime(TMUtil.getHourAndSecondsTimeFromSecs(playedSeconds));
            }
            mRightTimer.setTitle(getString(R.string.played));

            if (roundModel.getPace() >= 0) {
                color = ContextCompat.getColor(getContext(), R.color.green);
                mLeftTimeView.setRightText(getString(R.string.on_pace));
                changeMyLocationMarkerColor(false);
            } else if (roundModel.isHeldUp()) {
                color = ContextCompat.getColor(getContext(), R.color.orange);
                mLeftTimeView.setRightText(getString(R.string.delayed));
            } else if (roundModel.isHoldingUp()) {
                color = ContextCompat.getColor(getContext(), R.color.magenta);
                mLeftTimeView.setRightText(getString(R.string.delayer));
            } else {
                color = ContextCompat.getColor(getContext(), R.color.red);
                mLeftTimeView.setRightText(getString(R.string.slow));
                changeMyLocationMarkerColor(true);
            }
            try {
                GradientDrawable bgShape = (GradientDrawable) mTag.getBackground().getConstantState().newDrawable().mutate();
                bgShape.setColor(color);
                mTag.setBackground(bgShape);
            } catch (Throwable e) {
                Timber.d(e);
            }

            mBottomMenu.setBackgroundColor(color);
        }

        if (!roundModel.isInRound() && !roundModel.isComplete() && !roundModel.isNineHole()) {
            if (firstRequest) {
                getHole(GameManager.getStartHole(), false);
                firstRequest = false;
            }
            return;
        }

        if (!roundModel.isInRound() && roundModel.isComplete() && isRateRoundActive) {
            if (roundModel.isNineHole() && !halfWay) {
                halfWay = roundModel.isNineHole();
                RoundRateFragment fragment = new RoundRateFragment();
                fragment.bindRoundModel(roundModel);
                PreferenceManager.getInstance().setRatedRound(true);
                addFragment(this, fragment, true, RoundRateFragment.class.getName());
            } else if (!roundModel.isNineHole() && !PreferenceManager.getInstance().isRoundRated()) {
                stopTimer();
                PreferenceManager.getInstance().setRatedRound(true);
                RoundRateFragment fragment = new RoundRateFragment();
                fragment.bindRoundModel(roundModel);

                getBaseActivity().setMapEnabled(false);
                clearAndAddRootFragment(fragment);
            }
        } else if (!roundModel.isInRound() && roundModel.isComplete() && !isRateRoundActive && !PreferenceManager.getInstance().isRoundRated()) {
            FragmentRoundEnd fragment = new FragmentRoundEnd();
            fragment.bindRoundModel(roundModel);

            if (!roundModel.isNineHole()) {
                PreferenceManager.getInstance().setRatedRound(true);
                clearAndAddRootFragment(fragment);
            } else {
                PreferenceManager.getInstance().setRatedRound(true);
                addFragment(this, fragment, true, fragment.getTag());
            }
        }

        presenter.stopCountDownTimerToStartGame();

        //THERE IS CONDITION FOR AUTO-TRANSITION BETWEEN HOLES ACCORDING TO GEO-ZONES
        if (firstRequest && roundModel.getCurrentHole() != null && GameManager.getCurrentPlayHole() != Integer.parseInt(roundModel.getCurrentHole())) {
            GameManager.setCurrentPlayHole(Integer.parseInt(roundModel.getCurrentHole()));
            getHole(GameManager.getCurrentPlayHole(), inRound);
        }
        firstRequest = false;
    }

    private void changeMyLocationMarkerColor(boolean isLate) {
        if (getGolfService() != null && getGolfService().getLastKnownMyLocationLatLng() != null && isLate != globalIsLate && myLocationMarker != null) {
            myLocationMarker.remove();
            myLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(getGolfService().getLastKnownMyLocationLatLng())
                    .anchor(0.5f, 0.5f)
                    .zIndex(2f)
                    .icon(TMUtil.getMyLocationMarker(getContext(),
                            PreferenceManager.getInstance().getDeviceTag(), isLate)));
            globalIsLate = isLate;
        }
    }

    @Override
    public void drawPointsOfInterest(List<PointOfInterest> pointOfInterests) {
        if (checkFragmentAdded()) return;
        for (PointOfInterest item : pointOfInterests) {
            if (item.getImage() != null && !item.getImage().equals("")) {
                presenter.getBitmapDescriptionFromUrl(item, getContext());
            } else {
                onBitmapFromUrl(item, null);
            }
        }
    }

    @Override
    public void onBitmapFromUrl(PointOfInterest point, BitmapDescriptor icon) {
        if (checkFragmentAdded()) return;
        if (icon == null) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getCoordinates().get(0), point.getCoordinates().get(1)))
                    .icon(TMUtil.getHoleMarker(getContext(), point.getName())));
        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getCoordinates().get(0), point.getCoordinates().get(1)))
                    .anchor(0.5f, 0.1f)
                    .icon(icon));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getCoordinates().get(0), point.getCoordinates().get(1)))
                    .icon(TMUtil.getHoleMarker(getContext(), point.getName())));
        }
    }

    @Override
    public void onGetRoundInfo(RestInRoundModel roundModel) {
        if (checkFragmentAdded()) return;
        if (PreferenceManager.getInstance().getLastRoundID() != null && PreferenceManager.getInstance().getLastRoundID().equals(roundModel.getId())) {
            return;
        } else {
            PreferenceManager.getInstance().saveLastRoundID(null);
        }

        // has anything changed?
        if (currentRoundModel == null || currentRoundModel.isIgnoreGeofence() != roundModel.isIgnoreGeofence()) {
            drawGeoFences();
        }

        this.currentRoundModel = roundModel;
        EventBus.getDefault().postSticky(new RoundInfoEvent(roundModel));

        fillPaceInfoView(roundModel, false);
    }

    @Override
    public void onGetHoleDistanceFromTee(RestHoleDistanceModel distanceModel) {
        if (checkFragmentAdded()) return;
        mFrontDistance.setText(distanceModel.getFront());
        mCenterDistance.setText(distanceModel.getCenter());
        mBackDistance.setText(distanceModel.getBack());
    }

    @Override
    public void onGetAllHoles(List<RestHoleModel> holes) {
        if (checkFragmentAdded()) return;
        if (holes == null || mMap == null) {
            Toast.makeText(getContext(), getString(R.string.looks_like_there_are_some_issues_pls_restart_app), Toast.LENGTH_LONG).show();
            return;
        }

        for (RestHoleModel restHoleModel : holes) {
            switch (restHoleModel.getColor()) {
                case GolfConstants.COLOR_BLUE: {
                    int flag = GameManager.getHolesBlueIcons().get((int) (restHoleModel.getHole() - 1));
                    holesMarkers.add(
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(restHoleModel.getCenterPoint().get(0), restHoleModel.getCenterPoint().get(1)))
                                    .icon(TMUtil.bitmapDescriptorFromVector(getActivity(), flag, 0.25f)))
                    );
                    break;

                }
                case GolfConstants.COLOR_GREEN: {
                    int flag = GameManager.getHolesGreenIcons().get((int) (restHoleModel.getHole() - 1));
                    holesMarkers.add(
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(restHoleModel.getCenterPoint().get(0), restHoleModel.getCenterPoint().get(1)))
                                    .icon(TMUtil.bitmapDescriptorFromVector(getActivity(), flag, 0.25f)))
                    );
                    break;

                }
                case GolfConstants.COLOR_RED: {
                    int flag = GameManager.getHolesRedIcons().get((int) (restHoleModel.getHole() - 1));
                    holesMarkers.add(
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(restHoleModel.getCenterPoint().get(0), restHoleModel.getCenterPoint().get(1)))
                                    .icon(TMUtil.bitmapDescriptorFromVector(getActivity(), flag, 0.25f)))
                    );
                    break;

                }
                case GolfConstants.TRANSPARENT:
                    holesDots.add(
                            mMap.addGroundOverlay(
                                    new GroundOverlayOptions()
                                            .image(TMUtil.bitmapDescriptorFromVector(getActivity(), R.drawable.front_back_icon))
                                            .position(restHoleModel.getCenterPointLatLng(), 2f)
                            )
                    );
                    break;
                default: {
                    int flag = GameManager.getHolesYellowIcons().get((int) (restHoleModel.getHole() - 1));
                    holesMarkers.add(
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(restHoleModel.getCenterPoint().get(0), restHoleModel.getCenterPoint().get(1)))
                                    .icon(TMUtil.bitmapDescriptorFromVector(getActivity(), flag, 0.25f)))
                    );
                    break;
                }
            }
        }

        for (RestHoleModel restHoleModel : holes) {
            GroundOverlayOptions frontImage = new GroundOverlayOptions()
                    .image(TMUtil.bitmapDescriptorFromVector(getActivity(), R.drawable.front_back_icon))
                    .position(restHoleModel.getFrontPointLatLng(), 2f);
            mMap.addGroundOverlay(frontImage);

            GroundOverlayOptions backImage = new GroundOverlayOptions()
                    .image(TMUtil.bitmapDescriptorFromVector(getActivity(), R.drawable.front_back_icon))
                    .position(restHoleModel.getBackPointLatLng(), 2f);
            mMap.addGroundOverlay(backImage);

            teesMarkers.add(
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(restHoleModel.getStartPoint().get(0), restHoleModel.getStartPoint().get(1)))
                            .icon(TMUtil.getHoleMarker(getActivity(), String.valueOf(restHoleModel.getHole()))))
            );
        }

        if (!TMUtil.isOnline()) {
            getHole(1, false);
        }
    }

    @Override
    public void onCheckGeoFences() {
        if (checkFragmentAdded()) return;
        drawGeoFences();
    }

    private void showFiveMinutesToStartDialog(int leftMinutes) {
        fiveMinutesLeftToStartGame = true;
        GolfAlertDialog dialog = new GolfAlertDialog(getActivity())
                .setBottomBtnText(getString(R.string.pls_assist_us))
                .setCancelable(false)
                .setOkText(getString(R.string.ok_we_will_be_there))
                .setIcon(R.drawable.ic_info)
                .setMessage(getString(R.string.your_round_starts_in, leftMinutes, GameManager.getStartHole()))
                .setTitle(getString(R.string.reminder));

        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {

            }

            @Override
            public void onBottomBtnClick() {
                showSendMessageDialog();
            }
        });

        dialog.show();
    }

    @Override
    public void onCountDownTimerToStartGameTick() {
        if (checkFragmentAdded()) return;
        int leftMinutes = TMUtil.getLeftMinutes(GameManager.getStartGameTime());

        if (leftMinutes <= 5 && !fiveMinutesLeftToStartGame) {
            showFiveMinutesToStartDialog(leftMinutes);
        }

        if (leftMinutes <= 0) {
            presenter.stopCountDownTimerToStartGame();
            mLeftTimeView.setVisibility(View.GONE);
            return;
        }

        int color;

        if (leftMinutes > 10) {
            color = ContextCompat.getColor(getContext(), R.color.green);
            mLeftTimeView.setRightText("On Pace");
        } else if (leftMinutes <= 10 && leftMinutes > 5) {
            color = ContextCompat.getColor(getContext(), R.color.orange);
            mLeftTimeView.setRightText("Delayed");
        } else {
            color = ContextCompat.getColor(getContext(), R.color.red);
            mLeftTimeView.setRightText("Delayer");
        }

        GradientDrawable bgShape = (GradientDrawable) mTag.getBackground();
        bgShape.setColor(color);

        mBottomMenu.setBackgroundColor(color);

        mLeftTimeView.setStartingIn(TMUtil.getLeftMinutes(GameManager.getStartGameTime()));
        mRightTimer.setVisibility(View.GONE);
    }

    private void showHoles() {
        mHolesContainer.setVisibility(View.VISIBLE);
        mWarningContainer.setVisibility(View.GONE);
    }

    private void showWarningContainer() {
        mHolesContainer.setVisibility(View.GONE);
        mWarningContainer.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_current_location)
    void onCurrentLocationClick() {
        clearMap();

        manualChangingHole = false;
        LatLng myLocation;
        try {
            myLocation = getGolfService().getLastKnownLocationLatLngWithRules();
        } catch (Throwable e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            myLocation = null;
        }
        focusFromMe = true;


        if (myLocation != null) {
            getHole(GameManager.getCurrentPlayHole() == 0 ? 1 : GameManager.getCurrentPlayHole(), true);
        } else {
            if (inRound) {
                getHole(GameManager.getCurrentPlayHole() == 0 ? 1 : GameManager.getCurrentPlayHole(), false);
            } else {
                getHole(GameManager.currentHole, false);
            }
        }

        onFocusBetweenMeAndHole();
    }

    @OnClick(R.id.btn_prev_hole)
    void onPrevHole() {
        if (GameManager.getHoles() == null || GameManager.getHoles().isEmpty()) {
            return;
        }
        if (GameManager.currentHole == 1) {
            GameManager.currentHole = GameManager.getHoles().size();
        } else {
            GameManager.currentHole--;
        }
        switchHole(GameManager.currentHole);

        RestGeoZoneModel section = getCurrentSection();
        if (section != null) {
            String id = section.getProperties().getColor() + section.getProperties().getHole();
            int index = GameManager.getHoleIndexByColor(id) + 1;
            GameManager.setCurrentPlayHole(index);
        }
        manualChangingHole = true;
    }

    @OnClick(R.id.btn_score)
    void onScoreClick() {
        Intent intent = new Intent(getContext(), ScoringActivity.class);
        getActivity().startActivityForResult(intent, 1);
    }

    @OnClick(R.id.btn_next_hole)
    void onNextHole() {
        if (GameManager.getHoles() == null || GameManager.getHoles().isEmpty()) {
            return;
        }
        if (GameManager.currentHole == GameManager.getHoles().size()) {
            GameManager.currentHole = 1;
        } else {
            GameManager.currentHole++;
        }
        switchHole(GameManager.currentHole);

        RestGeoZoneModel section = getCurrentSection();
        if (section != null) {
            String id = section.getProperties().getColor() + section.getProperties().getHole();
            int index = GameManager.getHoleIndexByColor(id) + 1;
            GameManager.setCurrentPlayHole(index);
        }
        manualChangingHole = true;
    }

    private void switchHole(int hole) {
        clearMap();
        focusFromMe = false;
        getHole(hole, true);
        showHoleInfo(GameManager.getHole(GameManager.currentHole));
    }

    private void startTimer() {
        timer = new Handler();
        timer.postDelayed(timerRunnable, 0); // 1 second delay (takes millis)
    }

    private void stopTimer() {
        if (timer != null) {
            timer.removeCallbacks(timerRunnable);
            timer = null;
        }
    }

    //From service
    @Override
    public void onGetPaceInfo(RestInRoundModel inRoundModel) {
        if (checkFragmentAdded()) return;
        if (PreferenceManager.getInstance().getLastRoundID() != null && PreferenceManager.getInstance().getLastRoundID().equals(inRoundModel.getId())) {
            return;
        } else {
            PreferenceManager.getInstance().saveLastRoundID(null);
        }
        if (inRoundModel.isRefresh()) {
            try {
                getGolfService().refreshPaceInfo();
            } catch (Throwable e) {
                Timber.d(e.getMessage());
            }
        }

        // has anything changed?
        if (currentRoundModel == null || currentRoundModel.isIgnoreGeofence() != inRoundModel.isIgnoreGeofence()) {
            drawGeoFences();
        }
        currentRoundModel = inRoundModel;
        if (currentRoundModel.isInRound()) {
            if (mDisclaimer.getDataEntryStatus() == DataEntryStatus.SUBMITTED) {
                rangeFindingContent.setVisibility(View.GONE);
                topContent.setVisibility(View.VISIBLE);
                bottomContent.setVisibility(View.VISIBLE);
            } else {
                rangeFindingContent.setVisibility(View.VISIBLE);
                topContent.setVisibility(View.GONE);
                bottomContent.setVisibility(View.GONE);
            }
        }
        Timber.d("set timer get pace info");
        fillPaceInfoView(inRoundModel, false);

        if (inRoundModel.getDevices() != null && !inRoundModel.getDevices().isEmpty())
            drawDevices(inRoundModel.getDevices());

        mDisclaimer = PreferenceManager.getInstance().getDisclaimer();

        if (inRoundModel.isWaiverEnabled()) {
            if (mDisclaimer.getDataEntryStatus() != DataEntryStatus.SUBMITTED) {
                showDataEntryFragment();
            }
        }
    }

    private void drawDevices(Set<TMDevice> devices) {
        if (mMap != null) {
            if (devicesMap.isEmpty()) {
                createDeviceMarkers(devices);
            } else {
                Set<TMDevice> devicesToRemove = new HashSet<>(CollectionUtils.removeAll(devicesMap.keySet(), devices));
                Set<TMDevice> newDevices = new HashSet<>(CollectionUtils.removeAll(devices, devicesMap.keySet()));
                removeDeviceMarkers(devicesToRemove);
                updateMarkerPositions();

                createDeviceMarkers(newDevices);
            }
        }
    }

    private void updateMarkerPositions() {
        if (devicesMap.isEmpty())
            for (TMDevice device : devicesMap.keySet()) {
                TMUtil.smoothlyChangeMarkerPosition(devicesMap.get(device), new LatLng(device.getCoos().get(0), device.getCoos().get(1)));
            }
    }

    private void removeDeviceMarkers(Set<TMDevice> devices) {
        if (!devices.isEmpty()) {
            for (TMDevice device : devices) {
                try {
                    devicesMap.get(device).remove();
                    devicesMap.remove(device);
                } catch (Throwable exc) {

                }
            }
        }
    }

    private void createDeviceMarkers(Set<TMDevice> devices) {
        if (!devices.isEmpty()) {
            for (TMDevice device :
                    devices) {
                devicesMap.put(device, mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(device.getCoos().get(0), device.getCoos().get(1)))
                        .anchor(0.5f, 0.5f)
                        .zIndex(2f)
                        .icon(TMUtil.createMarkerBitmapByDeviceType(device, requireContext()))));
            }
        }
    }

    private Marker addDistanceText(LatLng startLocation, LatLng endLocation, String distance) {
        if (endLocation == null || startLocation == null || mMap == null) return null;

        return mMap.addMarker(new MarkerOptions()
                .position(TMUtil.getDistanceMarkerPosition(startLocation, endLocation))
                .draggable(false)
                .anchor(0.5f, 0.5f)
                .icon(TMUtil.getDistanceMarker(getContext(), distance)));
    }


    private void drawRangeDistance(LatLng tappedPoint, LatLng startPoint) {
        if (mMap == null) {
            Toast.makeText(getContext(), getString(R.string.looks_like_there_are_some_issues_pls_restart_app), Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<LatLng> points = new ArrayList<>();
        points.add(startPoint);
        points.add(tappedPoint);
        points.add(new LatLng(currentHoleModel.getCenterPoint().get(0), currentHoleModel.getCenterPoint().get(1)));

        GroundOverlayOptions pointImage = new GroundOverlayOptions()
                .image(TMUtil.bitmapDescriptorFromVector(getActivity(), R.drawable.range_finder_icon))
                .position(points.get(1), 28f);

        tappedPointImage = mMap.addGroundOverlay(pointImage);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(3);
        polylineOptions.color(Color.WHITE);
        polylineOptions.addAll(points);

        polyline = mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onGetHoleDistanceFromPoint(RestHoleDistanceModel distanceModel) {
        if (checkFragmentAdded()) return;
        distanceToBackMarker = addDistanceText(tappedPoint, new LatLng(currentHoleModel.getCenterPoint().get(0),
                currentHoleModel.getCenterPoint().get(1)), distanceModel.getCenter());

        distanceToFrontMarker = addDistanceText(tappedPoint, new LatLng(currentHoleModel.getStartPoint().get(0),
                currentHoleModel.getStartPoint().get(1)), distanceModel.getTee());

        addingPolyline = false;
    }

    @Override
    public void onGetHoleDistanceFromPointAndMyLocation(RestHoleDistanceModel distanceModel) {
        if (checkFragmentAdded()) return;
        distanceToBackMarker = addDistanceText(tappedPoint, new LatLng(currentHoleModel.getCenterPoint().get(0),
                currentHoleModel.getCenterPoint().get(1)), distanceModel.getCenter());

        distanceToFrontMarker = addDistanceText(tappedPoint, myLocationTappedPoint, distanceModel.getMyLocation());

        currentDistanceToHole = distanceModel.getCenter();
        Timber.d(distanceModel.getBack() +
                "= back" +
                distanceModel.getCenter() +
                "= center" + distanceModel.getFront() +
                "=front" + distanceModel.getMyLocation() +
                "myLocation" + distanceModel.getTee() +
                " = tee");

        addingPolyline = false;
    }

    @Override
    public void clearHoleDistances() {
        if (checkFragmentAdded()) return;
        currentDistanceToHole = null;
        clearMap();

    }

    @Override
    public void onGetAlerts(final List<RestAlertModel> alertsList) {

    }

    private void focusBetweenMeAndHole() {
        if (mMap == null) {
            try {
                Toast.makeText(requireContext(), getString(R.string.looks_like_there_are_some_issues_pls_restart_app), Toast.LENGTH_LONG).show();
            } catch (Throwable e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return;
        }


        try {

            LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();

            latlngBuilder.include(currentHoleModel.getCenterPointLatLng());

            final float bearing;
            if (focusFromMe) {
                if (getGolfService().getLastKnownMyLocationLatLng() == null) {
                    Toast.makeText(getContext(), getString(R.string.cannot_get_device_location), Toast.LENGTH_SHORT).show();

                    bearing = TMUtil.getBearing(currentHoleModel.getStartPointLatLng(), currentHoleModel.getCenterPointLatLng());
                    latlngBuilder.include(currentHoleModel.getStartPointLatLng());
                } else {
                    bearing = TMUtil.getBearing(getGolfService().getLastKnownMyLocationLatLng(), currentHoleModel.getCenterPointLatLng());
                    latlngBuilder.include(getGolfService().getLastKnownMyLocationLatLng());
                }
            } else {
                bearing = TMUtil.getBearing(currentHoleModel.getStartPointLatLng(), currentHoleModel.getCenterPointLatLng());
                latlngBuilder.include(currentHoleModel.getStartPointLatLng());
            }

            final LatLngBounds latLngBounds = latlngBuilder.build();

            CameraPosition pos = CameraPosition.builder()
                    .bearing(bearing)
                    .target(latLngBounds.getCenter())
                    .zoom(TMUtil.getBoundsZoomLevel(latLngBounds, getResources().getInteger(R.integer.map_zoom_value) - TMUtil.getNavigationHeight(getBaseActivity().getWindowManager()) / 3, 1f))
                    .build();

            GoogleMap.CancelableCallback callBack = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                callBack = new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (mMap == null || getContext() == null) return;

                        Projection projection = mMap.getProjection();

                        Point point = projection.toScreenLocation(latLngBounds.getCenter());
                        Point pointCenter = projection.toScreenLocation(currentHoleModel.getCenterPointLatLng());
                        projection.toScreenLocation(mMap.getProjection().getVisibleRegion().farLeft);
                        point.x *= 0.66f;
                        point.y += pointCenter.y - TMUtil.getTopMargin() / 2 - TMUtil.getHoleMarkerSize(getContext());
                        LatLng center = projection.fromScreenLocation(point);

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(center), 500, null);
                    }

                    @Override
                    public void onCancel() {

                    }
                };
            }

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 500, callBack);
        } catch (Throwable exception) {
            FirebaseCrashlytics.getInstance().recordException(exception);
        }
    }

    private void drawMyLocation(LatLng myLocation) {
        myLocationPoint = myLocation;

        if (myLocationMarker == null) {
            myLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(myLocation)
                    .anchor(0.5f, 0.5f)
                    .zIndex(2f)
                    .icon(TMUtil.getMyLocationMarker(getContext(),
                            PreferenceManager.getInstance().getDeviceTag(), false)));
        } else {
            TMUtil.smoothlyChangeMarkerPosition(myLocationMarker, myLocation);
        }
    }

    private boolean checkIsInSection() {
        try {
            if (getGolfService() != null) {
                for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
                    if (model != null
                            && PolyUtil.containsLocation(getGolfService().getLastKnownMyLocationLatLng().latitude,
                            getGolfService().getLastKnownMyLocationLatLng().longitude,
                            model.getGeometry().getPolygon().getPoints(), false)
                            && model.getProperties().getHole() > 0) {
                        return true;
                    }

                }
            }
            return false;
        } catch (Throwable throwable) {
            return false;
        }
    }

    private RestGeoZoneModel getCurrentSection() {
        try {
            if (getGolfService() != null) {
                for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
                    if (model != null
                            && PolyUtil.containsLocation(getGolfService().getLastKnownMyLocationLatLng().latitude,
                            getGolfService().getLastKnownMyLocationLatLng().longitude,
                            model.getGeometry().getPolygon().getPoints(), false)
                            && model.getProperties().getHole() > 0) {
                        return model;
                    }
                }
            }
            return null;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private RestGeoZoneModel getSectionByNumber(int num) {
        try {
            if (getGolfService() != null) {
                for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
                    if (model != null && model.getProperties().getNumber() == num) {
                        return model;
                    }
                }
            }
            return null;
        } catch (Throwable throwable) {
            return null;
        }
    }


    private AdvertisementModel checkIsInAdvertisementSection
            (List<AdvertisementModel> advertisementList, LatLng location) {
        for (AdvertisementModel advert : advertisementList) {
            for (AdvertisementModel.Geometry advertSection : advert.getSections()) {
                if (PolyUtil.containsLocation(location.latitude, location.longitude, advertSection.getPolygons().getPoints(), false)) {
                    if (!advertiseId.equals(advert.getId())) {
                        advertiseId = advert.getId();
                        return advert;
                    } else if (advertiseId.equals(advert.getId())) {
                        return null;
                    }
                }
            }
        }
        advertiseId = "";
        return null;
    }

    @Override
    public void onLocationChanged(LatLng location, float accuracy, boolean inGeofence) {

        // Check for advertisement zone enter
        List<AdvertisementModel> allAdverts = getLocalAdvertisements();

        // Remove banners and disabled adverts
        List<AdvertisementModel> adverts = new ArrayList<>();
        for (AdvertisementModel advert : allAdverts) {
            if (advert.isEnabled() && !advert.getType().equalsIgnoreCase("banner")) {
                adverts.add(advert);
            }
        }

        if (!adverts.isEmpty()) {
            AdvertisementModel advertisement = checkIsInAdvertisementSection(adverts, location);
            if (advertisement != null) {

                boolean isEnabled = advertisement.isEnabled();
                boolean isActive = advertisement.isActive();
                boolean isValid = advertisement.isValid();
                boolean isExpired = advertisement.isExpired();

                if (isEnabled && isActive && isValid && !isExpired) {
                    showAdvertisementDialog(advertisement);
                }
            }
        }

        // Use the location polling to constantly check when last the waiver was shown
        // If more than an hour not in round then show the waiver again
        if (mDisclaimer != null && mDisclaimer.getLastShownTime() != null && currentRoundModel == null) {
            if (isMoreThanOneHourOld(mDisclaimer.getLastShownTime())) {
                mDisclaimer.setDataEntryStatus(DataEntryStatus.NONE);
                mDisclaimer.setStatus(DisclaimerStatus.NONE);
                PreferenceManager.getInstance().setDisclaimer(mDisclaimer);
                showWaiverFragment();
            }
        }

        if (location == null || mMap == null || checkFragmentAdded()) return;

        if (!getBaseActivity().moved()) {
            if (!GeofenceHelper.mockingLocation)
                return; //If Geofence is not mocking location, then return if there has been no movement
        } else {
            getBaseActivity().setMoved(false);
        }

        LatLng myLast = myLocationPoint;
        if (accuracy <= 5) drawMyLocation(location);

        if (currentDistanceToHole != null && currentHoleModel != null) {
            presenter.checkDistancesFromTappedToCurrent(currentDistanceToHole, location, currentHoleModel);
        } else if (currentHoleModel != null) {
            Timber.d("onLocationChanged: " + location + " " + currentHoleModel.toString());
        }

        RestGeoZoneModel currentSection = getCurrentSection();
        if (currentRoundModel != null
                && currentRoundModel.isGolfGenius()
                && currentSection != null
                && currentSection.getProperties().getNumber() != this.currentSection
                && PreferenceManager.getInstance().getGolfGenius() != null) {
            RestGeoZoneModel nextSection = getSectionByNumber(currentSection.getProperties().getNumber() + 1);

            if ((nextSection == null || nextSection.getProperties().getHole() != currentSection.getProperties().getHole())) {
                this.currentSection = currentSection.getProperties().getNumber();
                Intent intent = new Intent(getContext(), ScoringActivity.class);
                intent.putExtra("hole", currentSection.getProperties().getHole());
                getActivity().startActivityForResult(intent, 1);
            }
        }

        if (!checkIsInSection() && lastCheckInCourse) {
            lastCheckInCourse = false;
            switchHole(GameManager.currentHole);
            manualChangingHole = true;
        } else if (checkIsInSection() && !lastCheckInCourse) {
            lastCheckInCourse = true;
            manualChangingHole = false;
            onCurrentLocationClick();
        }

        if (PreferenceManager.getInstance().getGeoZones() != null) {
            //AUTO-TRANSITION BETWEEN HOLES ACCORDING TO GEO-ZONES
            if (PreferenceManager.getInstance().isDeviceMobile() && !checkIsInSection() && PreferenceManager.getInstance().isRoundRated() &&
                    currentRoundModel != null && !currentRoundModel.isNineHole() &&
                    !currentRoundModel.isNullModel()) {
                FragmentRoundEnd fragment = new FragmentRoundEnd();
                fragment.bindRoundModel(currentRoundModel);
                clearAndAddRootFragment(fragment);
            }
            if (!manualChangingHole) {
                for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
                    if (geoZonesPolygonsList.get(model.getProperties().getNumber()) != null &&
                            PolyUtil.containsLocation(location.latitude, location.longitude,
                                    geoZonesPolygonsList.get(model.getProperties().getNumber()).getPoints(), false)) {
                        if (currentHoleModel == null) break;

                        if (!model.getHoleInternalId().equals(currentHoleModel.getHoleInternalId())) {
                            String id = model.getProperties().getColor() + model.getProperties().getHole();
                            int index = GameManager.getHoleIndexByColor(id) + 1;
                            if (index == GameManager.getCurrentPlayHole() + 1) {
                                transactionDialog.dissmiss();
                                GameManager.setCurrentPlayHole(index);
                                onCurrentLocationClick();
                            }
                        }
                        break;
                    }
                }
            }

            for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
                if (geoZonesPolygonsList.get(model.getProperties().getNumber()) != null &&
                        currentGeoZoneId != model.getProperties().getNumber() &&
                        PolyUtil.containsLocation(location.latitude, location.longitude,
                                geoZonesPolygonsList.get(model.getProperties().getNumber()).getPoints(), false)) {
                    {
                        currentGeoZoneId = model.getProperties().getNumber();
                        if (getGolfService() != null) {
                            getGolfService().restartSendLocationTimer();
                        }
                        String id = model.getProperties().getColor() + model.getProperties().getHole();
                        int index = GameManager.getHoleIndexByColor(id) + 1;
                        currentStayHole = index;

                        if (startHoles.contains(model.getProperties().getHole())) {
                            if (isCalibrationMode && calibrationPrevIndex != index) {
                                calibrationPrevIndex = index;
                                FragmentCalibrationMode calibrationFragment = new FragmentCalibrationMode();
                                addFragmentWithoutResume(this, calibrationFragment, true, FragmentCalibrationMode.class.getName());
                            }
                        } else {
                            calibrationPrevIndex = index;
                        }
                        if (startHoles.contains(model.getProperties().getHole()) || index == GameManager.getCurrentPlayHole() + 1) {
                            transactionDialog.dissmiss();
                            pressedNoHole = 0;
                            GameManager.setCurrentPlayHole(index);
                            onCurrentLocationClick();
                        } else if (index == GameManager.getCurrentPlayHole() && isVisible() && transactionDialog.isShowing()) {
                            transactionDialog.dissmiss();
                        } else if (pressedNoHole != index && isOutOfSequenceActive) {
                            if (transactionDialog.isShowing() && prevIndex != index && isVisible()) {
                                prevIndex = index;
                                transactionDialog.setMessage("Would you like to move to Hole " + model.getProperties().getHole());
                                transactionDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                                    @Override
                                    public void onOkClick() {
                                        GameManager.setCurrentPlayHole(index);
                                        onCurrentLocationClick();
                                        pressedNoHole = 0;
                                        transactionDialog.dissmiss();
                                    }

                                    @Override
                                    public void onBottomBtnClick() {
                                        transactionDialog.dissmiss();
                                        pressedNoHole = index;
                                    }
                                });
                            } else if (!transactionDialog.isShowing() && index != GameManager.getCurrentPlayHole() && isVisible() && isOutOfSequenceActive) {
                                prevIndex = index;
                                transactionDialog.show();
                                transactionDialog.setMessage("Would you like to move to Hole " + model.getProperties().getHole());
                                transactionDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                                    @Override
                                    public void onOkClick() {
                                        GameManager.setCurrentPlayHole(index);
                                        onCurrentLocationClick();
                                        pressedNoHole = 0;
                                        transactionDialog.dissmiss();
                                    }

                                    @Override
                                    public void onBottomBtnClick() {
                                        transactionDialog.dissmiss();
                                        pressedNoHole = index;
                                    }
                                });
                            }
                        } else if (isOutOfSequenceActive) {
                            pressedNoHole = 0;
                        } else if (index != GameManager.getCurrentPlayHole()) {
                            transactionDialog.dissmiss();
                            pressedNoHole = 0;
                        }
                    }
                    break;
                }
            }

            try {
                showAdvertisementBanners();
                // Fetch advertisements if 30 min has passed
                long advertisementsLastFetch = PreferenceManager.getInstance().getAdvertisementsLastFetch();
                if (isMoreThanThirtyMinutesOld(advertisementsLastFetch)) {
                    setLastAdvertisementFetch();
                    presenter.getAdvertisements(getContext());
                }
            } catch (Throwable e) {
                Log.d("GEOGT", "Exception during show Advertisements: " + e.getMessage());
            }
        }

        Location currentLocation = null;
        if (getGolfService() != null) {
            currentLocation = getGolfService().getLastKnownMyLocation();
        }
        //boolean isInGeofence = insideGeofence(location);
        boolean isInGeofence = inGeofence;
        double dis = myLast != null ? TMUtil.getDistanceByBounds(myLast, location) : -1.0;
        double speed = currentLocation != null ? currentLocation.getSpeed() : -1.0;

//        if(currentRoundModel == null || !currentRoundModel.isIgnoreGeofence()) {
//            if (firstRestrictedZoneEntries > 2 && accuracy <= 5 && dis > 0 && dis <= 4 && speed > 0 && speed <= 4 && enteredInRestrictedZone(location)) {
//                RestGeoFenceModel model = geoFencesMap.get(restrictedZoneId);
//                if(Objects.requireNonNull(model).isActive()) {
//                    RestBoundsModel mapBounds = PreferenceManager.getInstance().getMapBounds();
//                    int delay = mapBounds != null && mapBounds.getHighPriorityFenceTimer() > 0
//                            ? mapBounds.getHighPriorityFenceTimer()
//                            : 10; // default 10 seconds
//
//                    Location finalCurrentLocation = currentLocation;
//
//                    geoCountDownChecker.removeCallbacksAndMessages(null);
//                    geoCountDownChecker.postDelayed(() -> {
//                        try {
//                            if(model.isNotify()) {
//                                FixModel fix = new FixModel(
//                                        finalCurrentLocation.getLatitude(),
//                                        finalCurrentLocation.getLongitude(),
//                                        finalCurrentLocation.getAccuracy(),
//                                        finalCurrentLocation.getSpeed(),
//                                        TMUtil.getBatteryLevel(),
//                                        TMUtil.getTimeUTC(System.currentTimeMillis()),
//                                        true,
//                                        true
//                                );
//                                presenter.sendLocationFix(fix);
//                            }
//
//                            if(!model.isVisible()) return;
//
//                            boolean shown = showRestrictedZoneScreen();
//                            if(!shown) return;
//
////                            if(model.playSound())
////                                TMUtil.vibrateAndMakeNoiseDevice(getContext());
//                        } catch (Throwable e) {
//                            FirebaseCrashlytics.getInstance().recordException(e);
//                        }
//                    }, 1000L * delay);
//
//                    if(!model.isVisible()) return;
//
//                    if (getBaseActivity() != null) {
//                        getBaseActivity().deviceInteraction();
//                    }
//
//                    showSoftRestrictedZoneScreen();
//                }
//            } else if(!isInGeofence) {
//                geoCountDownChecker.removeCallbacksAndMessages(null);
//                firstRestrictedZoneEntries = 0;
//                hideRestrictedZoneScreen();
//            } else if(accuracy <= 5 && dis > 0 && dis <= 4 && speed > 0 && speed <= 4) {
//                firstRestrictedZoneEntries++;
//            }
//        }

        if (!CartControlHelper.inCartControlGeofence) {
            if (isInGeofence && !GeofenceHelper.ignoreAllGeofences) {
                boolean enteredRestrictedZone = enteredInRestrictedZone(location);
                if (!hasGeoAlertTriggered) {
                    showGeofenceAlertDialog(); //Commented out for Cart Control GeoTesting
                    hasGeoAlertTriggered = true;
                }
                if (enteredRestrictedZone) {
                    //Record the time when the user entered the restricted zone
                    if (currentLocation != null && accuracy <= 5 && dis > 0) {
                        FixModel fix = new FixModel(
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude(),
                                accuracy,
                                speed,
                                accuracy,
                                GeofenceHelper.averageTop3SNR,
                                TMUtil.getBatteryLevel(),
                                TMUtil.getTimeUTC(System.currentTimeMillis()),
                                true,
                                true
                        );
                        presenter.sendLocationFix(fix);
                    }
                }
            } else {
                hasGeoAlertTriggered = false;
                restrictedZoneId = ""; //Clear the restricted zone id so that alert can be triggered again
                geofenceAlert.setVisibility(View.INVISIBLE);
            }

            if (!isInGeofence) {
                //presenter.stopMakingNotification();
                geofenceAlert.setVisibility(View.INVISIBLE);
            } else if (currentLocation != null && accuracy <= 5 && dis > 0) {
                FixModel fix = new FixModel(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        accuracy,
                        speed,
                        accuracy,
                        GeofenceHelper.averageTop3SNR,
                        TMUtil.getBatteryLevel(),
                        TMUtil.getTimeUTC(System.currentTimeMillis()),
                        false,
                        true
                );
                presenter.sendLocationFix(fix);
            }

            if (GameManager.getCurrentPlayHole() == GameManager.currentHole && tappedPoint != null) {
                onMapClick(tappedPoint);
            }
        }

        if (currentRoundModel != null && currentRoundModel.isCaptureLogs()) {
            if ((currentRoundModel.isComplete() && !currentRoundModel.isNineHole()) || getLogsSizeFromFile() > 5000) {
                presenter.sendSupportLogs(currentRoundModel.getId());
            } else if (currentRoundModel.isInRound() || currentRoundModel.isNineHole()) {
                writeToFile(TMUtil.getTimeUTC(System.currentTimeMillis()), location.latitude, location.longitude, accuracy, speed);
            }
        }
    }

    private int getLogsSizeFromFile() {
        Scanner scanner = null;
        int count = 0;
        try {
            scanner = new Scanner(new File(LogFileConstants.raw_fixes_filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (Objects.requireNonNull(scanner).hasNextLine()) {
            scanner.nextLine();
            count++;
        }
        return count;
    }

    private void showAdvertisementDialog(AdvertisementModel advertisement) {
        if (this.isAdded()) {
            if (getChildFragmentManager().findFragmentByTag("advertisement_dialog") == null) {
                AdvertisementDialog dialog = AdvertisementDialog.newInstance(advertisement);
                dialog.show(getChildFragmentManager(), "advertisement_dialog");
                return;
            }

            try {
                getChildFragmentManager().beginTransaction().remove(Objects.requireNonNull(getChildFragmentManager().findFragmentByTag("advertisement_dialog"))).commitAllowingStateLoss();
                getChildFragmentManager().popBackStackImmediate("advertisement_dialog", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } catch (IllegalStateException ignored) {
                Log.d("GEOGT", "FragmentMap - showAdvertisementDialog() - Exception: " + ignored.getMessage());
            }
            AdvertisementDialog dialog = AdvertisementDialog.newInstance(advertisement);
            dialog.show(getChildFragmentManager(), "advertisement_dialog");
            EventBus.getDefault().postSticky(new CartIsMovedAd(advertisement.getDisplayTime()));
        }
    }

    private void showGeofenceAlertDialog() {
        RestGeoFenceModel geoZoneModel = geoFencesMap.get(restrictedZoneId);
        if (geoZoneModel == null)
            return;
        if (geoZoneModel.getProperties().getAlertType().toLowerCase().contains("audible")) {
            geofenceAlertTopSection.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
            geofenceAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            geofenceAlertTopSection.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
            geofenceAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }

        if (geoZoneModel.isNotify() && !GeofenceHelper.ignoreAllGeofences) {
            geofenceAlertMessage.setText(geoZoneModel.getProperties().getMessage());
            geofenceAlert.setVisibility(View.VISIBLE);
        }
    }

    public void showCartControlGeofenceAlertDialog(double latitude, double longitude, double hdop, double snr) {
        //public void showCartControlGeofenceAlertDialog() {
        if (true) {
            geofenceAlertTopSection.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
            geofenceAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            geofenceAlertTopSection.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
            geofenceAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }

        //geofenceAlertMessage.setText(geoZoneModel.getProperties().getMessage());
        if (!GeofenceHelper.ignoreAllGeofences) {
            geofenceAlertMessage.setText("You have entered a restricted zone. Please put your cart in reverse and back out of the zone.");
            geofenceAlert.setVisibility(View.VISIBLE);

            boolean newBreach = false;
            if (hasCartBreached == false) {
                hasCartBreached = true;
                newBreach = true;
            }

            FixModel fix = new FixModel(
                    latitude,
                    longitude,
                    hdop,
                    0,
                    hdop,
                    snr,
                    TMUtil.getBatteryLevel(),
                    TMUtil.getTimeUTC(System.currentTimeMillis()),
                    newBreach,
                    true
            );

            Gson gson = new Gson();
            String json;
            json = gson.toJson(fix);

            Log.d("GEOGT", "Sending breach:" + json);

            presenter.sendLocationFix(fix);
        }
    }

    public void hideCartControlGeofenceAlertDialog() {
        hasCartBreached = false;
        geofenceAlert.setVisibility(View.INVISIBLE);
    }

    private boolean showRestrictedZoneScreen() {
//        hideRestrictedZoneScreen();
//        RestGeoFenceModel geoZoneModel = geoFencesMap.get(restrictedZoneId);
//        if(geoZoneModel == null)
//            return false;
//
//        String priority = geoZoneModel.getPriority();
//        restrictedZoneFragmentAlert = new FragmentAlert();
//        restrictedZoneFragmentAlert.setOkText(getString(R.string.ok_no_problem));
//        restrictedZoneFragmentAlert.setWhatText(getString(R.string.pls_assist_me));
//
//        if(priority.equals("high")) {
//            presenter.startMakingNotification();
//        }
//
//        RestAlertModel alertModel = new RestAlertModel();
//        String message;
//        try {
//            if (geoZoneModel.getProperties().getMessage() != null) {
//                message = geoZoneModel.getProperties().getMessage();
//            } else {
//                message = getString(R.string.you_have_entered_restricted_zone);
//            }
//        } catch (Throwable e) {
//            message = getString(R.string.you_have_entered_restricted_zone);
//        }
//
//        alertModel.setMessage(message);
//        alertModel.setImage(getString(R.string.geo_fence_alert));
//        alertModel.setSound(geoZoneModel.playSound());
//        alertModel.setVisible(false);
//
//        restrictedZoneFragmentAlert.bindAlert(alertModel);
//
//        restrictedZoneFragmentAlert.setOnDialogListener(new GolfCallback.GolfDialogListener() {
//            @Override
//            public void onOkClick() {
//                restrictedZoneFragmentAlert = null;
//            }
//
//            @Override
//            public void onBottomBtnClick() {
//            }
//        });
//        getBaseActivity().addFragment(restrictedZoneFragmentAlert);
        return true;
    }

    private void showSoftRestrictedZoneScreen() {
//        hideRestrictedZoneScreen();
//        restrictedZoneFragmentAlert = new FragmentAlert();
//        restrictedZoneFragmentAlert.setOkText(getString(R.string.ok_no_problem));
//        restrictedZoneFragmentAlert.setWhatText(getString(R.string.pls_assist_me));
//
//        RestAlertModel alertModel = new RestAlertModel();
//        String message = getString(R.string.you_are_entering_a_restricted_area);
//
//        alertModel.setMessage(message);
//        alertModel.setImage(getString(R.string.geo_fence_alert));
//        alertModel.setVisible(false);
//
//        restrictedZoneFragmentAlert.bindAlert(alertModel);
//
//        restrictedZoneFragmentAlert.setOnDialogListener(new GolfCallback.GolfDialogListener() {
//            @Override
//            public void onOkClick() {
//                restrictedZoneFragmentAlert = null;
//            }
//
//            @Override
//            public void onBottomBtnClick() {
//            }
//        });
//        getBaseActivity().addFragment(restrictedZoneFragmentAlert);
    }

    private void hideRestrictedZoneScreen() {
        if (restrictedZoneFragmentAlert != null) {
            restrictedZoneFragmentAlert.goBack();
        }

        if (geoFenceAlert != null) {
            geoFenceAlert.goBack();
        }
        restrictedZoneFragmentAlert = null;
        geoFenceAlert = null;
        firstRestrictedZoneEntries = 0;
    }

    private boolean insideGeofence(LatLng location) {
        for (int i = 0; i < geofencePolygonsList.size(); i++) {
            if (PolyUtil.containsLocation(location.latitude,
                    location.longitude,
                    geofencePolygonsList.valueAt(i).getPoints(),
                    false)) {
                return true;
            }
        }
        return false;
    }

    private boolean enteredInRestrictedZone(LatLng location) {
        for (int i = 0; i < geofencePolygonsList.size(); i++) {
            if (PolyUtil.containsLocation(location.latitude, location.longitude,
                    geofencePolygonsList.valueAt(i).getPoints(), false)) {
                if (!Objects.equals(restrictedZoneId, geofencePolygonsList.keyAt(i))) {
                    enteredRestrictedZone = false;
                }
                if (enteredRestrictedZone) {
                    return false;
                }
                restrictedZoneId = geofencePolygonsList.keyAt(i);
                return enteredRestrictedZone = true;
            }
        }

        return enteredRestrictedZone = false;
    }

    @Override
    public void onCanUpdateDistanceFromMeToCurrentHole(RestHoleDistanceModel distanceModel) {
        if (checkFragmentAdded()) return;
        if (checkIsInSection() && GameManager.getCurrentPlayHole() == GameManager.currentHole) {
            mBackDistance.setText(distanceModel.getBack());
            mCenterDistance.setText(distanceModel.getCenter());
            mFrontDistance.setText(distanceModel.getFront());
        }
    }

    @Override
    public void onFocusBetweenMeAndHole() {
        if (checkFragmentAdded()) return;
        if (checkIsInSection()) {
            if (focusFromMe) {
                focusBetweenMeAndHole();
            } else if (currentStayHole == GameManager.currentHole) {
                onCurrentLocationClick();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void showSendMessageDialog() {
        GolfSendMessageDialog dialog = new GolfSendMessageDialog(getContext());
        dialog.show();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOnlineEvent(OnlineEvent event) {
        if (checkFragmentAdded()) return;
        isOnline = true;

        /*scoreBtn.setEnabled(true);*/
        offlineTv.setVisibility(View.GONE);

        fillPaceInfoView(currentRoundModel, true);

        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineEvent(OfflineEvent event) {
        if (checkFragmentAdded()) return;
        enableOfflineMode(event);
        EventBus.getDefault().removeStickyEvent(event);
    }

    private void enableOfflineMode(OfflineEvent event) {
        if (isOnline)
            offlineTv.setText("Device synchronizing. Status as at " + TMUtil.getHourTimeFromSecs(event.getSetDate()));

        offlineTv.setVisibility(View.VISIBLE);

        setBottomMenuHeight(getResources().getDimension(R.dimen.bottom_bar_height_offline));
        isOnline = false;

    }


    public void setBottomMenuHeight(float height) {
        CoordinatorLayout.LayoutParams leftSide = (CoordinatorLayout.LayoutParams) mHolesContainer.getLayoutParams();
        leftSide.bottomMargin = (int) height;
        mHolesContainer.setLayoutParams(leftSide);

        CoordinatorLayout.LayoutParams currentLocation = (CoordinatorLayout.LayoutParams) mCurrentLocation.getLayoutParams();
        currentLocation.bottomMargin = (int) height;
        mCurrentLocation.setLayoutParams(currentLocation);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mBottomMenu.getLayoutParams();
        lp.height = (int) height;
        mBottomMenu.setLayoutParams(lp);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMapBoundsEvent(MapBoundsEvent event) {
        if (mMap != null) {
            clearMap();
            clearMapZones();
        }
        getBaseActivity().clearLogo();
        getBaseActivity().loadClubLogo();
        onGetMapBounds(PreferenceManager.getInstance().getMapBounds());
        EventBus.getDefault().removeStickyEvent(event);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCourseHolesEvent(CourseHolesEvent event) {
        for (Marker marker : holesMarkers) {
            marker.remove();
        }

        for (Marker marker : teesMarkers) {
            marker.remove();
        }

        for (GroundOverlay marker : holesDots) {
            marker.remove();
        }

        holesMarkers.clear();
        holesDots.clear();
        clearMap();

        List<RestHoleModel> holes = PreferenceManager.getInstance().getHoles();

        if (currentHoleModel != null) {
            for (RestHoleModel model : holes) {
                if (currentHoleModel.getHole() == model.getHole() && currentHoleModel.getColor().equals(model.getColor())) {
                    currentHoleModel = model;
                    break;
                }
            }
        }

        onGetAllHoles(holes);
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGeoFencesUpdateEvent(GeoFencesUpdateEvent event) {
        drawGeoFences();
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeactivateGeoFenceByIdEvent(DeactivateGeoFenceByIdEvent event) {
        ArrayList<String> ids = event.getIds();
        for (String id : ids) {
            if (geoFencesMap.get(id) != null) {
                geofencePolygonsList.get(id).setVisible(false);
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActivateGeoFenceByIdEvent(ActivateGeoFenceByIdEvent event) {
        ArrayList<String> ids = event.getIds();
        for (String id : ids) {
            if (geofencePolygonsList.get(id) != null) {
                geofencePolygonsList.get(id).setVisible(true); // validate
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceUp(ServiceOnline event) {
        getGolfService().bindCallback(this);
        EventBus.getDefault().removeStickyEvent(event);

    }


    private void clearMapZones() {
        if (mMap != null) {
            mMap.clear();
            teesMarkers.clear();
            holesMarkers.clear();
            myLocationMarker = null;
            geoZonesPolygonsList.clear();
            geoFencesMap.clear();
            geofencePolygonsList.clear();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClearTeeEvent(ClearTeeEvent event) {
        currentRoundModel = null;
        clearMap();
        mLeftTimer.setTime("");
        EventBus.getDefault().removeStickyEvent(event);

    }

    @Override
    public void rebind() {
        if (checkFragmentAdded()) return;
        if (getGolfService() != null) {
            getGolfService().bindCallback(this);
        }
    }

    public void clearHighPriorAlert() {
        geoFenceAlert = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowScoreEvent(ShowScoreEvent event) {
        if (event.isActive()) {
            scoreBtn.setVisibility(View.VISIBLE);
        } else {
            scoreBtn.setVisibility(View.GONE);
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    private void writeToFile(String time, Double lat, Double lon, Float accuracy, Double speed) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/raw_fixes.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            String log = time + " , " + lat + " , " + lon + " , " + accuracy + " , " + speed;
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(log);
            writer.append("\n");

            writer.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_close_snaptoplay_bluetooth)
    void onSnapToPlayBluetoothClose() {
        bottomSnapToPlayDialogs.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_snaptoplpay_bluetooth_continue)
    void onSnapToPlayBluetoothContinue() {
        bottomSnapToPlayDialogs.setVisibility(View.GONE);
    }

}
