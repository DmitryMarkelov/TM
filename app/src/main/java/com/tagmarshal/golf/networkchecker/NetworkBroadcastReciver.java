package com.tagmarshal.golf.networkchecker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.refresher.GeoRefresherService;

public class NetworkBroadcastReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if (isOnline(context) && PreferenceManager.getInstance().isGeoZonesFailed()) {
                Log.d(getClass().getName(), "Service started from network checker");
                   ComponentName comp = new ComponentName(context.getPackageName(),
                      GeoRefresherService.class.getName());

             GeoRefresherService.enqueueWork(context,comp, 50 , intent);;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
