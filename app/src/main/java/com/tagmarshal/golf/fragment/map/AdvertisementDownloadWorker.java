package com.tagmarshal.golf.fragment.map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.AdvertisementModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdvertisementDownloadWorker extends Worker {
    public AdvertisementDownloadWorker(@NonNull @io.reactivex.annotations.NonNull Context context, @NonNull @io.reactivex.annotations.NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @NonNull
    @io.reactivex.annotations.NonNull
    @Override
    public Result doWork() {

//        Data workData = getInputData();
        String urlStr = getInputData().getString("mediaUrl");
        String mediaName = getInputData().getString("mediaName");
//        String advertisementID = getInputData().getString("advertisementID");

        boolean isWifiOnly = getInputData().getBoolean("wifiOnly", false);
        boolean isWifiConnected = isWifiConnected();
        boolean isDownloaded = downloadFile(urlStr, isWifiConnected, isWifiOnly, mediaName);

        return isDownloaded ? Result.success() : Result.failure();
    }

    private boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }

    private boolean downloadFile(String urlStr, boolean isWifiConnected, boolean isWifiOnly, String mediaName) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Check if file type is video or image
            String contentType = connection.getContentType();
            boolean isVideo = contentType != null && contentType.startsWith("video");

            // Skip video if not on WiFi and WiFi constraint is required
            if (isVideo && isWifiOnly && !isWifiConnected) {
                return false;
            }

            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), mediaName);
            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            connection.disconnect();

            Log.d("DownloadWorker", "Downloaded: " + mediaName + " at " + file.getAbsolutePath());
            return true;

        } catch (Exception e) {
            Log.e("DownloadWorker", "Error downloading file: " + e.getMessage());
            return false;
        }
    }
}
