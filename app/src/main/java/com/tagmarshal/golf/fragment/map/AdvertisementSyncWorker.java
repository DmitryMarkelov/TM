package com.tagmarshal.golf.fragment.map;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.AdvertisementModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdvertisementSyncWorker extends Worker {
    public AdvertisementSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        updateSavedAdverts();
        return Result.success();
    }

    private void updateSavedAdverts() {
        List<AdvertisementModel> advertisementList = PreferenceManager.getInstance().getAdvertisements();

        if (advertisementList != null) {

            for (AdvertisementModel advert : advertisementList) {

                for (AdvertisementModel.MediaName mediaName : advert.getMediaNames()) {

                    File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), mediaName.getName());

                    if (file.exists()) {
                        List<String> downloads = advert.getDownloadsUrls();
                        if (downloads == null) {
                            downloads = new ArrayList<>();
                        }
                        downloads.add(mediaName.getName());
                        advert.setDownloadsUrls(downloads);
                        mediaName.setDownloaded(true);
                    } else {
                        mediaName.setDownloaded(false);
                    }
                }
                advert.setDownloaded(true);

                for (AdvertisementModel.MediaName mediaName : advert.getMediaNames()) {
                    if (!mediaName.isDownloaded()) {
                        advert.setDownloaded(false);
                    }
                }
            }
            PreferenceManager.getInstance().setAdvertisement(advertisementList);
        }
    }
}
