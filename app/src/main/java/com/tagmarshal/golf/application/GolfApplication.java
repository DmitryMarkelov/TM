package com.tagmarshal.golf.application;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagmarshal.golf.BuildConfig;
import com.tagmarshal.golf.constants.FirebaseConstants;
import com.tagmarshal.golf.manager.PreferenceManager;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.test.TestCourseHoles;

import timber.log.Timber;

public class GolfApplication extends MultiDexApplication {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
//        TMUtil.initLogs();
//        TMUtil.deleteLogsFile();
        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        GolfAPI.init();
        PreferenceManager.init(this);
        if (PreferenceManager.getInstance().getCourseName() != null && !PreferenceManager.getInstance().getCourseName().isEmpty()) {
            FirebaseCrashlytics.getInstance().setCustomKey(FirebaseConstants.user_name, PreferenceManager.getInstance().getCourseName());
        }
        TestCourseHoles.init();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
    }
}
