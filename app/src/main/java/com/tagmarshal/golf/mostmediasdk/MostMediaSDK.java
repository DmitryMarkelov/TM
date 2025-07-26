package com.tagmarshal.golf.mostmediasdk;

import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.tagmarshal.golf.rest.model.AdvertisementModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Main MostMedia SDK class for handling advertisement management
 * Handles proper banner rotation by organizing advertisement data
 */
public class MostMediaSDK {
    private static final String TAG = "MostMediaSDK";
    private static boolean isConfigured = false;
    private static Retrofit retrofit;
    private static MostMediaService service;
    
    /**
     * Initialize the SDK with the new server URL
     * @param serverUrl Base URL of the new ad server
     */
    public static void initialize(String serverUrl) {
        Log.d(TAG, "🚀 Initializing MostMediaSDK with server: " + serverUrl);
        MostMediaConfig.configure(serverUrl);
        initializeRetrofit(serverUrl);
        isConfigured = true;
        Log.d(TAG, "✅ MostMediaSDK initialized successfully");
    }
    
    /**
     * Initialize Retrofit instance for the new ad server
     * @param serverUrl Base URL of the new ad server
     */
    private static void initializeRetrofit(String serverUrl) {
        Log.d(TAG, "🔧 Initializing Retrofit for URL: " + serverUrl);
        
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();

        service = retrofit.create(MostMediaService.class);
        Log.d(TAG, "✅ Retrofit initialized successfully");
    }
    
    /**
     * Check if SDK is configured
     * @return true if SDK is configured
     */
    public static boolean isConfigured() {
        boolean configured = isConfigured && MostMediaConfig.isEnabled();
        Log.d(TAG, "🔍 SDK Configuration Check - isConfigured: " + isConfigured + 
              ", MostMediaConfig.isEnabled(): " + MostMediaConfig.isEnabled() + 
              ", Result: " + configured);
        return configured;
    }
    
    /**
     * Get the total number of banners for rotation
     * This includes all individual banner images from all advertisements
     * 
     * @param advertisements List of advertisements
     * @return Total number of individual banners available for rotation
     */
    public static int getTotalBannerCount(List<AdvertisementModel> advertisements) {
        if (advertisements == null || advertisements.isEmpty()) {
            Log.d(TAG, "⚠️ No advertisements to count");
            return 0;
        }
        
        int totalBanners = 0;
        for (AdvertisementModel ad : advertisements) {
            if (ad.getType().equalsIgnoreCase("banner") && ad.isEnabled() && ad.isDownloaded()) {
                List<String> downloadUrls = ad.getDownloadsUrls();
                if (downloadUrls != null) {
                    totalBanners += downloadUrls.size();
                }
            }
        }
        
        Log.d(TAG, "🎯 Total banner count: " + totalBanners);
        return totalBanners;
    }
    
    /**
     * Get specific banner information by global index
     * This maps a global banner index to the specific advertisement and image URL
     * 
     * @param advertisements List of advertisements
     * @param globalIndex Global banner index (0 to totalBannerCount-1)
     * @return BannerInfo containing the advertisement and image URL, or null if invalid index
     */
    public static BannerInfo getBannerByGlobalIndex(List<AdvertisementModel> advertisements, int globalIndex) {
        if (advertisements == null || advertisements.isEmpty()) {
            Log.d(TAG, "⚠️ No advertisements available");
            return null;
        }
        
        int currentIndex = 0;
        for (AdvertisementModel ad : advertisements) {
            if (ad.getType().equalsIgnoreCase("banner") && ad.isEnabled() && ad.isDownloaded()) {
                List<String> downloadUrls = ad.getDownloadsUrls();
                if (downloadUrls != null) {
                    for (String imageUrl : downloadUrls) {
                        if (currentIndex == globalIndex) {
                            Log.d(TAG, "🎯 Found banner at global index " + globalIndex + ": " + imageUrl);
                            return new BannerInfo(ad, imageUrl, currentIndex);
                        }
                        currentIndex++;
                    }
                }
            }
        }
        
        Log.w(TAG, "⚠️ Global index " + globalIndex + " not found, total banners: " + currentIndex);
        return null;
    }
    
    /**
     * Get display time for a specific banner by global index
     * @param advertisements List of advertisements
     * @param globalIndex Global banner index
     * @return Display time in seconds, or default 5 seconds if not found
     */
    public static int getDisplayTimeForBanner(List<AdvertisementModel> advertisements, int globalIndex) {
        BannerInfo bannerInfo = getBannerByGlobalIndex(advertisements, globalIndex);
        if (bannerInfo != null && bannerInfo.advertisement != null) {
            int displayTime = bannerInfo.advertisement.getDisplayTime();
            Log.d(TAG, "⏰ Display time for banner " + globalIndex + ": " + displayTime + " seconds");
            return displayTime > 0 ? displayTime : 5; // Default to 5 seconds if 0 or negative
        }
                 Log.w(TAG, "⚠️ Could not get display time for banner " + globalIndex + ", using default 5 seconds");
         return 5; // Default fallback
     }
     
     /**
      * Get advertisements from the new server
      * This is the main method called by FragmentMapModel and FirebaseModel
      * @return Observable list of advertisements from the new server
      */
     public static Observable<List<AdvertisementModel>> getAdvertisements() {
         Log.d(TAG, "🌐 Getting advertisements from new server");
         
         if (service == null) {
             Log.e(TAG, "❌ Service not initialized, reinitializing...");
             initializeRetrofit(MostMediaConfig.getAdServerUrl());
         }
         
         return service.getAdvertisements()
                 .doOnNext(ads -> Log.d(TAG, "✅ Received " + ads.size() + " advertisements from new server"))
                 .doOnError(error -> Log.e(TAG, "❌ Error getting advertisements: " + error.getMessage()));
     }
    
    /**
     * Enhanced rotation helper for ViewPager
     * This provides the logic for proper banner rotation with individual display times
     */
    public static class EnhancedRotationHelper {
        private static final String TAG = "EnhancedRotationHelper";
        
        /**
         * Calculate the next banner index in rotation
         * @param currentIndex Current banner index
         * @param totalBanners Total number of banners
         * @return Next banner index (wraps around to 0 after the last banner)
         */
        public static int getNextBannerIndex(int currentIndex, int totalBanners) {
            if (totalBanners <= 0) {
                Log.w(TAG, "⚠️ Invalid total banners: " + totalBanners);
                return 0;
            }
            
            int nextIndex = (currentIndex + 1) % totalBanners;
            Log.d(TAG, "🔄 Next banner index: " + currentIndex + " -> " + nextIndex + " (total: " + totalBanners + ")");
            return nextIndex;
        }
        
        /**
         * Get display time for current banner index
         * @param advertisements List of advertisements
         * @param currentIndex Current banner index
         * @return Display time in milliseconds
         */
        public static long getDisplayTimeMs(List<AdvertisementModel> advertisements, int currentIndex) {
            int displayTimeSeconds = getDisplayTimeForBanner(advertisements, currentIndex);
            long displayTimeMs = displayTimeSeconds * 1000L;
            Log.d(TAG, "⏰ Display time for banner " + currentIndex + ": " + displayTimeMs + "ms");
            return displayTimeMs;
        }
        
        /**
         * Check if there are multiple banners to rotate
         * @param advertisements List of advertisements
         * @return true if there are multiple banners
         */
        public static boolean hasMultipleBanners(List<AdvertisementModel> advertisements) {
            int totalBanners = getTotalBannerCount(advertisements);
            boolean hasMultiple = totalBanners > 1;
            Log.d(TAG, "🔍 Has multiple banners: " + hasMultiple + " (total: " + totalBanners + ")");
            return hasMultiple;
        }
    }
    
    /**
     * Banner information container
     */
    public static class BannerInfo {
        public final AdvertisementModel advertisement;
        public final String imageUrl;
        public final int globalIndex;
        
        public BannerInfo(AdvertisementModel advertisement, String imageUrl, int globalIndex) {
            this.advertisement = advertisement;
            this.imageUrl = imageUrl;
            this.globalIndex = globalIndex;
        }
    }
} 