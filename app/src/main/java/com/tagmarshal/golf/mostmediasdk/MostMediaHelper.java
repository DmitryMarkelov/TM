package com.tagmarshal.golf.mostmediasdk;

/**
 * Helper class for easy MostMedia SDK configuration
 * Provides simple methods to enable/disable the ad redirection
 */
public class MostMediaHelper {
    
    /**
     * Enable ad redirection to new server
     * @param serverUrl Base URL of the new ad server (e.g., "https://your-server.com/")
     */
    public static void enableAdRedirection(String serverUrl) {
        MostMediaSDK.initialize(serverUrl);
    }
    
    /**
     * Disable ad redirection (use original ad server)
     */
    public static void disableAdRedirection() {
        MostMediaConfig.disable();
    }
    
    /**
     * Check if ad redirection is currently active
     * @return true if ads are being redirected to new server
     */
    public static boolean isAdRedirectionActive() {
        return MostMediaSDK.isConfigured();
    }
    
    /**
     * Get current ad server URL (null if using original)
     * @return The configured ad server URL or null
     */
    public static String getCurrentAdServer() {
        return MostMediaConfig.isEnabled() ? MostMediaConfig.getAdServerUrl() : null;
    }
} 