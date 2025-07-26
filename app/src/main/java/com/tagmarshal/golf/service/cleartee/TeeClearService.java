package com.tagmarshal.golf.service.cleartee;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.CustomJobIntentService;

import com.tagmarshal.golf.eventbus.ClearTeeEvent;
import com.tagmarshal.golf.manager.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

public class TeeClearService extends CustomJobIntentService {
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            PreferenceManager.getInstance().setCourseTeeStartTime(null);
            Log.d(getClass().getName(), "Tee cleared => success");

            EventBus.getDefault().postSticky(new ClearTeeEvent());
        } catch (Throwable throwable) {
            Timber.d(throwable);
        }
    }
}
