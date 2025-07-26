package com.tagmarshal.golf.mostmediasdk;

import android.util.Log;

import com.tagmarshal.golf.rest.model.AdvertisementModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MostMedia SDK - Main interface for ad requests
 * Redirects advertisement requests to a new server while maintaining compatibility
 */
public class MostMediaSDK {
    private static final String TAG = "MostMediaSDK";
    private static MostMediaService service;
    private static Retrofit retrofit;
    
    // Initialize SDK automatically when class is loaded
    static {
        Log.d(TAG, "🔄 MostMedia SDK static initializer called");
        initialize();
    }
    
    /**
     * Initialize the SDK with the configured ad server URL
     */
    public static void initialize() {
        Log.d(TAG, "🚀 MostMedia SDK initialize() called");
        Log.d(TAG, "📊 SDK Enabled: " + MostMediaConfig.isEnabled());
        Log.d(TAG, "🌐 Server URL: " + MostMediaConfig.getAdServerUrl());
        
        if (MostMediaConfig.isEnabled()) {
            Log.d(TAG, "✅ SDK is enabled, setting up Retrofit");
            setupRetrofit();
        } else {
            Log.w(TAG, "❌ SDK is disabled, skipping initialization");
        }
    }
    
    /**
     * Initialize the SDK with a custom ad server URL (for runtime configuration)
     * @param serverUrl Base URL of the new ad server (e.g., "https://your-server.com/")
     */
    public static void initialize(String serverUrl) {
        Log.d(TAG, "🚀 MostMedia SDK initialize(String) called with URL: " + serverUrl);
        MostMediaConfig.configure(serverUrl);
        setupRetrofit();
    }
    
    /**
     * Get advertisements from the configured server
     * @return Observable list of advertisements in the same format as original API
     */
    public static Observable<List<AdvertisementModel>> getAdvertisements() {
        Log.d(TAG, "📡 MostMedia SDK getAdvertisements() called");
        Log.d(TAG, "📊 SDK Enabled: " + MostMediaConfig.isEnabled());
        Log.d(TAG, "🌐 Server URL: " + MostMediaConfig.getAdServerUrl());
        Log.d(TAG, "🔧 Service null: " + (service == null));
        
        if (!MostMediaConfig.isEnabled()) {
            Log.e(TAG, "❌ SDK not enabled, returning error");
            return Observable.error(new IllegalStateException("MostMedia SDK not initialized. Call initialize() first."));
        }
        
        if (service == null) {
            Log.w(TAG, "⚠️ Service is null, setting up Retrofit");
            setupRetrofit();
        }
        
        Log.d(TAG, "✅ Making request to server: " + MostMediaConfig.getAdServerUrl() + "ads");
        return service.getAdvertisements()
                .doOnNext(ads -> {
                    Log.d(TAG, "✅ Received " + ads.size() + " advertisements from server");
                    for (int i = 0; i < ads.size(); i++) {
                        AdvertisementModel ad = ads.get(i);
                        Log.d(TAG, "📋 Ad " + (i+1) + ": ID=" + ad.getId() + ", Type=" + ad.getType() + 
                              ", DisplayTime=" + ad.getDisplayTime() + ", BannerInterval=" + ad.getBannerInterval());
                        if (ad.getMediaNames() != null && !ad.getMediaNames().isEmpty()) {
                            Log.d(TAG, "🖼️ Ad " + (i+1) + " Media URL: " + ad.getMediaNames().get(0).getUrl());
                        }
                    }
                })
                .doOnError(error -> {
                    Log.e(TAG, "❌ Error getting advertisements: " + error.getMessage(), error);
                });
    }
    
    /**
     * Check if SDK is properly configured
     * @return true if SDK is ready to use
     */
    public static boolean isConfigured() {
        boolean configured = MostMediaConfig.isEnabled() && service != null;
        Log.d(TAG, "🔍 SDK Configuration Check - Enabled: " + MostMediaConfig.isEnabled() + 
              ", Service: " + (service != null) + ", Configured: " + configured);
        return configured;
    }
    
    /**
     * Setup Retrofit client for the new ad server
     */
    private static void setupRetrofit() {
        Log.d(TAG, "🔧 Setting up Retrofit client");
        
        if (!MostMediaConfig.isEnabled()) {
            Log.w(TAG, "❌ SDK not enabled, skipping Retrofit setup");
            return;
        }
        
        String baseUrl = MostMediaConfig.getAdServerUrl();
        Log.d(TAG, "🌐 Base URL: " + baseUrl);
        
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        Log.d(TAG, "🔧 Creating Retrofit instance");
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        
        Log.d(TAG, "🔧 Creating service interface");
        service = retrofit.create(MostMediaService.class);
        Log.d(TAG, "✅ Retrofit setup complete");
    }
} 