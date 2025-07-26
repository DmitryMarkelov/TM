package com.tagmarshal.golf.rest;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GolfAPI {

    private static final String BASE_URL = "https://rest.tagmarshal.golf/api/";
    private static final String testiurl = "https://fastlane.tagmarshal.com/";
    private static String BASE_COURSE_URL;

    private static Retrofit golfRetrofit;
    private static Retrofit golfCourseRetrofit;
    private static GolfAPIEndpoints golfApi;
    private static GolfAPICourseEndpoint golfCourseApi;
    private static GolfAPIMapConfig golfMapConfig;
    private static Retrofit golfMapConfigRetrifot;

    public static String getUsedMemorySize() {

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) GolfApplication.context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;

        return String.valueOf(availableMegs) + "MB";
    }

    public static void init() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
        httpClient.addInterceptor(logging);

        golfMapConfigRetrifot = new Retrofit.Builder()
                .baseUrl(testiurl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(
                        android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.N
                                ? httpClient.build()
                                : getUnsafeOkHttpClient()
                )
                .build();

        golfRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(
                        android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.N
                                ? httpClient.build()
                                : getUnsafeOkHttpClient()
                )
                .build();

        golfApi = golfRetrofit.create(GolfAPIEndpoints.class);
        golfMapConfig = golfMapConfigRetrifot.create(GolfAPIMapConfig.class);
    }

    public static GolfAPIMapConfig getGolfMapConfig() {
        return golfMapConfig;
    }

    public static GolfAPIEndpoints getGolfApi() {
        return golfApi;
    }

    public static void bindBaseCourseUrl(String baseCourseUrl) {
        BASE_COURSE_URL = baseCourseUrl;
        initBaseCourseClient(BASE_COURSE_URL);
    }

    public static String getBaseUrl() {
        return BASE_COURSE_URL;
    }

    private static void initBaseCourseClient(String baseCourseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        HeaderInterceptor imeiHeader = new HeaderInterceptor();

// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
        httpClient.addInterceptor(logging);

        httpClient.addInterceptor(imeiHeader);


        golfCourseRetrofit = new Retrofit.Builder()
                .baseUrl("https://" + baseCourseUrl + "/two-ways/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(
                        android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.N
                                ? httpClient.build()
                                : getUnsafeOkHttpClient()
                )
                .build();

        golfCourseApi = golfCourseRetrofit.create(GolfAPICourseEndpoint.class);
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier((hostname, session) -> true);

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static GolfAPICourseEndpoint getGolfCourseApi() {
        if (golfCourseApi != null) {
            return golfCourseApi;
        } else {
            initBaseCourseClient(PreferenceManager.getInstance().getCourse());
            return golfCourseApi;
        }
    }

    private static class HeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("DeviceIMEI", TMUtil.getDeviceIMEI())
                    .build();
            return chain.proceed(request);
        }
    }
}
