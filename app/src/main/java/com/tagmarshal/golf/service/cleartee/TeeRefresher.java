package com.tagmarshal.golf.service.cleartee;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class TeeRefresher extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ComponentName comp = new ComponentName(context.getPackageName(),
                    TeeClearService.class.getName());

            TeeClearService.enqueueWork(context, comp, 100, intent);
        }catch (Throwable e){
            Timber.d(e);
        }
    }
}
