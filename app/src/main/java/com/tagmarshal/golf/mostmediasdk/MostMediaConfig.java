package com.tagmarshal.golf.mostmediasdk;

import android.util.Log;

/**
 * Configuration class for MostMedia SDK
 * Handles the endpoint configuration for redirecting ad requests
 */
public class MostMediaConfig {
    private static final String TAG = "MostMediaConfig";
    // NEW AD SERVER URL - CONFIGURE HERE
    private static final String NEW_AD_SERVER_URL = "http://10.0.2.2:8080/";
    
    // Enable/disable the SDK - set to true to use new server
    private static final boolean ENABLE_SDK = true;
    
    private static String adServerUrl = NEW_AD_SERVER_URL;
    private static boolean isEnabled = ENABLE_SDK;
    
    /**
     * Configure the SDK with new ad server URL (for runtime configuration)
     * @param serverUrl The base URL of the new ad server
     */
    public static void configure(String serverUrl) {
        Log.d(TAG, "🔧 Configuring SDK with server URL: " + serverUrl);
        adServerUrl = serverUrl;
        isEnabled = true;
        Log.d(TAG, "✅ SDK configured - URL: " + adServerUrl + ", Enabled: " + isEnabled);
    }
    
    /**
     * Get the configured ad server URL
     * @return The ad server URL
     */
    public static String getAdServerUrl() {
        Log.d(TAG, "🌐 Getting ad server URL: " + adServerUrl);
        return adServerUrl;
    }
    
    /**
     * Check if SDK is enabled and configured
     * @return true if SDK is configured
     */
    public static boolean isEnabled() {
        boolean enabled = isEnabled && adServerUrl != null && !adServerUrl.isEmpty();
        Log.d(TAG, "🔍 SDK Enabled Check - isEnabled: " + isEnabled + 
              ", adServerUrl: " + adServerUrl + ", Result: " + enabled);
        return enabled;
    }
    
    /**
     * Disable the SDK (fallback to original endpoint)
     */
    public static void disable() {
        Log.d(TAG, "❌ Disabling SDK");
        isEnabled = false;
    }
} 