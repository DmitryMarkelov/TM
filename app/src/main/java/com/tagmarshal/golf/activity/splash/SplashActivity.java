package com.tagmarshal.golf.activity.splash;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.manager.IntentManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.TMUtil;

public class SplashActivity extends AppCompatActivity implements SplashActivityContract.View {

    private boolean inRound;
    private boolean halfway;
    private boolean closed;
    private boolean roundResponded;
    private boolean configResponded;
    private boolean permissionsGranted;
    private boolean tickTimer;
    private Handler permissionChecker = new Handler();
    private RestInRoundModel roundModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PreferenceManager preferenceManager = PreferenceManager.getInstance();
        if (preferenceManager.getDeviceTag() != null && !preferenceManager.isTag40Setup()) {
            String deviceType = preferenceManager.getDeviceType();
            String units = preferenceManager.getDeviceUnitMetric();
            preferenceManager.clearData(getApplicationContext());
            preferenceManager.setDeviceType(deviceType);
            preferenceManager.setDeviceUnitMetric(units);
            preferenceManager.setTag40Setup(true);
            preferenceManager.setFirstLaunch(false);
        }

        SplashActivityPresenter presenter = new SplashActivityPresenter(this);

        GolfAPI.bindBaseCourseUrl(preferenceManager.getCourse());

        if (PreferenceManager.getInstance().getCourse() != null) {
            presenter.checkInRound();
            CourseModel courseModel = PreferenceManager.getInstance().getCurrentCourseModel();
            presenter.getCourseConfig(courseModel);
//            if (PreferenceManager.getInstance().getDeviceTag() == null ||
//                    PreferenceManager.getInstance().getDeviceType() == null ||
//                    PreferenceManager.getInstance().getCourseName() == null) {
//
//                CourseModel courseModel = PreferenceManager.getInstance().getCurrentCourseModel();
//                presenter.getCourseConfig(courseModel);
//            } else {
//                configResponded = true;
//            }
        } else {
            configResponded = true;
            roundResponded = true;
            inRound = false;
        }
        calculateInches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsAndOpenMain();
    }

    private void checkPermissionsAndOpenMain() {
        if (permissionChecker != null) {
            permissionChecker.removeCallbacks(null);
        } else {
            permissionChecker = new Handler();
        }
        permissionChecker.postDelayed(() -> {
            tickTimer = true;
            if (checkAllPermissions()) {
                startMainActivity();
            }
        }, 1500);
    }

    private void calculateInches() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float scaleFactor = metrics.density;
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        PreferenceManager.getInstance().setDeviceInches(Math.min(widthDp, heightDp));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.app_requires_permissions), Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            permissionsGranted = true;
            checkPermissionsAndOpenMain();
        }
    }

    private void startMainActivity() {
        if (tickTimer
                && roundResponded
                && configResponded
                && permissionsGranted) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra(IntentManager.ACTIVITY_INTENT_IN_ROUND, inRound);
            intent.putExtra(IntentManager.ACTIVITY_INTENT_HALFWAY, halfway);
            intent.putExtra(IntentManager.ACTIVITY_INTENT_CLOSED, closed);
            intent.putExtra(IntentManager.ACTIVITY_INTENT_ROUND_MODEL, roundModel);
            startActivity(intent);
            finish();
        }
    }

    private boolean checkAllPermissions() {
        boolean settingsCanWrite = hasWriteSettingsPermission(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE},
                    11);

            return false;
        } else if (!settingsCanWrite && TMUtil.isQuestDevice()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        } else {
            permissionsGranted = true;
            return true;
        }
    }

    private boolean hasWriteSettingsPermission(Context context) {
        return Settings.System.canWrite(context);
    }

    @Override
    public void onGetInRoundInfo(RestInRoundModel inRoundModel) {
        roundModel = inRoundModel;
        inRound = inRoundModel.isInRound();
        halfway = inRoundModel.isNineHole();
        closed = inRoundModel.isComplete();
        roundResponded = true;
        startMainActivity();
    }

    @Override
    protected void onPause() {
        permissionChecker.removeCallbacks(null);
        permissionChecker = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (permissionChecker != null) {
            permissionChecker.removeCallbacks(null);
            permissionChecker = null;
        }
        super.onDestroy();
    }

    @Override
    public void onGetInRoundInfoFailure(String message) {
        Log.d("GEOGT", "onGetRoundInfoFailure");
        roundResponded = true;
        startMainActivity();
    }

    @Override
    public void onSuccessSaveConfig() {
        configResponded = true;
        startMainActivity();
    }

    @Override
    public void onSaveConfigFailure(String message) {
        configResponded = true;
        Toast.makeText(getApplicationContext(), "Network error: Cannot get config", Toast.LENGTH_LONG).show();
        startMainActivity();
    }
}
