package com.tagmarshal.golf.service.firebase;

import static com.tagmarshal.golf.application.GolfApplication.context;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.FileWorkerContract;
import com.tagmarshal.golf.data.FileWorkerModel;
import com.tagmarshal.golf.eventbus.CourseHolesEvent;
import com.tagmarshal.golf.eventbus.FirmwareUpdateEvent;
import com.tagmarshal.golf.eventbus.GeoFencesUpdateEvent;
import com.tagmarshal.golf.fragment.map.AdvertisementDownloadWorker;
import com.tagmarshal.golf.fragment.map.AdvertisementSyncWorker;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.FirmwareUpdateModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class FirebasePresenter implements FirebaseContract.Presenter, FileWorkerContract.Presenter {

    private FileWorkerModel fileModel;
    private CompositeDisposable disposables;
    private FirebaseContract.View view;
    private FirebaseContract.Model model;

    public FirebasePresenter(FirebaseContract.View view) {
        this.view = view;
        this.model = new FirebaseModel();
        this.fileModel = new FileWorkerModel();
        disposables = new CompositeDisposable();

    }

    @Override
    public void sendRefreshToken(Map<String, String> token) {
        disposables.add(model.sendRefreshToken(token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        __ -> view.onSuccessRefreshToken(),
                        e -> view.onError(e.getMessage())
                )
        );
    }

    @Override
    public void refreshMap() {
        if (GolfAPI.getBaseUrl() != null && !GolfAPI.getBaseUrl().isEmpty()) {
            PreferenceManager.getInstance().clearMapData();
            view.onSuccessMapRefresh();
        }
    }

    @Override
    public void getAdvertisements() {
        disposables.add(model.getAdvertisements()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        advertisements -> {
                            saveAdvertisements(advertisements);
                            startDownloads(context);
                        },
                        e -> {
                            Timber.tag("TAG").d("getAdvertisements: " + e.getMessage());
                        }
                )
        );
    }

    public void saveAdvertisements(List<AdvertisementModel> newAdvertisements) {
        // Enable new Advert
        for (AdvertisementModel newAdvert : newAdvertisements) {
            newAdvert.setEnabled(true);
        }

        List<AdvertisementModel> localAdvertisements = getLocalAdvertisements();
        if (localAdvertisements != null) {

            // Add new Adverts
            Set<AdvertisementModel> setA = new HashSet<>(newAdvertisements);
            Set<AdvertisementModel> setB = new HashSet<>(localAdvertisements);
            setA.removeAll(setB);
            if (!setA.isEmpty()) {
                localAdvertisements.addAll(setA);
                // Removed Expired Adverts
                List<AdvertisementModel> activeAdverts = removeExpiredAdvertisements(localAdvertisements);
                PreferenceManager.getInstance().setAdvertisement(activeAdverts);
            }

            List<AdvertisementModel> savedAdvertisements = getLocalAdvertisements();

            // Reset local adverts
            for (AdvertisementModel oldAdvert : savedAdvertisements) {
                oldAdvert.setEnabled(false);
            }

            // Update local adverts
            for (AdvertisementModel newAdvert : newAdvertisements) {
                for (AdvertisementModel oldAdvert : savedAdvertisements) {
                    if (newAdvert.getId().equalsIgnoreCase(oldAdvert.getId())) {
                        oldAdvert.update(newAdvert);
                        oldAdvert.setEnabled(true);
                    }
                }
            }

            PreferenceManager.getInstance().setAdvertisement(savedAdvertisements);
        } else {
            PreferenceManager.getInstance().setAdvertisement(newAdvertisements);
        }
    }

    public List<AdvertisementModel> getLocalAdvertisements() {
        return PreferenceManager.getInstance().getAdvertisements();
    }

    private List<AdvertisementModel> removeExpiredAdvertisements(List<AdvertisementModel> localAdvertisements) {
        for (AdvertisementModel advertisementModel : localAdvertisements) {
            if (advertisementModel.isExpired()) {
                localAdvertisements.remove(advertisementModel);
            }
        }
        return localAdvertisements;
    }

    public void startDownloads(Context context) {
        List<AdvertisementModel> advertisements = getLocalAdvertisements();

        if (advertisements != null) {

            List<OneTimeWorkRequest> downloadsWorkRequests = new ArrayList<>();

            for (AdvertisementModel advert : advertisements) {
                try {
                    if (!advert.isDownloaded() && advert.getMediaNames() != null && !advert.getMediaNames().isEmpty()) {
                        for (AdvertisementModel.MediaName mediaName : advert.getMediaNames()) {

                            Timber.d("Starting download for advertisement: %s", advert.getId());
                            boolean wifiOnly = advert.isVideo();
                            Constraints constraints = new Constraints.Builder()
                                    .setRequiredNetworkType(wifiOnly ? NetworkType.UNMETERED : NetworkType.CONNECTED)
                                    .build();

                            Data inputData = new Data.Builder()
                                    .putString("advertisementID", advert.getId())
                                    .putString("type", advert.getType())
                                    .putString("mediaUrl", mediaName.getUrl())
                                    .putString("mediaName", mediaName.getName())
                                    .putBoolean("wifiOnly", wifiOnly)
                                    .build();

                            OneTimeWorkRequest downloadRequest = new OneTimeWorkRequest.Builder(AdvertisementDownloadWorker.class)
                                    .setConstraints(constraints)
                                    .setInputData(inputData)
                                    .build();

                            downloadsWorkRequests.add(downloadRequest);
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e, "Failed to process advertisement: %s", advert.getId());
                }
            }

            if (!downloadsWorkRequests.isEmpty()) {
                OneTimeWorkRequest advertisementSyncRequest = new OneTimeWorkRequest.Builder(AdvertisementSyncWorker.class)
                        .build();

                WorkManager.getInstance(context)
                        .beginWith(downloadsWorkRequests)
                        .then(advertisementSyncRequest)
                        .enqueue();
            }
        }
    }

    @Override
    public void getGeofenceById(ArrayList<String> listItems) {
        disposables.add(model.getGeofenceById(listItems)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestGeoFenceModel>>() {
                    @Override
                    public void onSuccess(@NonNull List<RestGeoFenceModel> models) {
                        List<RestGeoFenceModel> geoFenceModels = PreferenceManager.getInstance().getGeoFences();
                        if (geoFenceModels == null) {
                            geoFenceModels = new ArrayList<>();
                        }

                        for (RestGeoFenceModel model : models) {
                            for (RestGeoFenceModel m : geoFenceModels) {
                                if (m.getId().equals(model.getId())) {
                                    geoFenceModels.remove(m);
                                    break;
                                }
                            }
                            geoFenceModels.add(model);
                        }

                        PreferenceManager.getInstance().setCourseGeoFences(geoFenceModels);
                        EventBus.getDefault().postSticky(new GeoFencesUpdateEvent());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        PreferenceManager.getInstance().setGeoFailed(true);
                    }
                }));
    }

    @Override
    public void getHoles() {
        disposables.add(model.getHoles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestHoleModel>>() {
                    @Override
                    public void onSuccess(@NonNull List<RestHoleModel> models) {
                        PreferenceManager.getInstance().setCourseHoles(models);
                        EventBus.getDefault().postSticky(new CourseHolesEvent());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "Get holes: " + e.getMessage());
                    }
                }));
    }

    @Override
    public void sendSupportLogs(String limit) {
        disposables.add(model.sendLogsToSupport(new SupportLogModel(
                        TMUtil.getTimeUTC(System.currentTimeMillis()),
                        PreferenceManager.getInstance().getCourseName(),
                        PreferenceManager.getInstance().getCourse(),
                        TMUtil.getDeviceIMEI(),
                        getLogsFromFile(limit)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> clearFixesFile(),
                        e -> {
                            Log.d(getClass().getName(), "sendSupportLogs: " + e.getMessage());
                            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "sendSupportLogs: " + e.getMessage());
                        }
                )
        );
    }

    @Override
    public void reregister(String units, String type, String build) {
        disposables.add(model.reregister(TMUtil.getDeviceIMEI(), type, build)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                            if (!PreferenceManager.getInstance().getDeviceType().equals(type)) {
                                getGeoFences();
                            }
                            PreferenceManager.getInstance().setDeviceUnitMetric(units.equals("meters") || units.equals("meter") ||
                                    units.equals("metres") || units.equals("metre")
                                    ? PreferenceManager.UNIT_METRIC_METER : PreferenceManager.UNIT_METRIC_YARD);
                            try {
                                JSONArray jsonArray = new JSONArray(s.body().string());
                                if (jsonArray.getJSONObject(0).has("tag")) {
                                    PreferenceManager.getInstance().setDeviceType(type.equals("mobile") ? PreferenceManager.DEVICE_TYPE_MOBILE : PreferenceManager.DEVICE_TYPE_CART);
                                    PreferenceManager.getInstance().setDeviceTag(jsonArray.getJSONObject(0).getString("tag"), PreferenceManager.getInstance().getCourse());

                                    view.onSuccessReregister();
                                }
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        },
                        FirebaseCrashlytics.getInstance()::recordException)

        );
    }

    @Override
    public void getCourses() {
        disposables.add(model.getCourses()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(coosList -> PreferenceManager.getInstance().saveNearestCourses(coosList))
        );
    }

    @Override
    public void downloadFirmware(String fileName) {
        String make = Build.MANUFACTURER; // SOTEN
        String model = Build.MODEL; // TM-TW80

        // only update if its a Soten 8 inch
        if (make.equals("SOTEN") && model.contains("TM-TW80")) {
            FirmwareUpdateModel firmwareUpdateModel = new FirmwareUpdateModel(fileName);
            PreferenceManager.getInstance().saveFirmwareUpdate(firmwareUpdateModel);
            EventBus.getDefault().postSticky(new FirmwareUpdateEvent());
        }
    }

    private void getGeoFences() {
        disposables.add(model.getGeoFences(TMUtil.getDeviceIMEI())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    PreferenceManager.getInstance().setCourseGeoFences(s);
                    view.onGeoFenceGet();
                })
        );
    }

    private List<String> getLogsFromFile(String limit) {
        File file = new File(LogFileConstants.fixes_filePath);
        List<String> logList = new ArrayList<>();
        int lastLine = Integer.parseInt(limit);
        try {
            if (file.exists()) {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                int lines = 0;
                StringBuilder builder = new StringBuilder();
                long length = file.length();
                length--;
                randomAccessFile.seek(length);
                for (long seek = length; seek >= 0; --seek) {
                    randomAccessFile.seek(seek);
                    char c = (char) randomAccessFile.read();
                    if (c != '\n') {
                        builder.append(c);
                    }
                    if (c == '\n' || seek == 0) {
                        builder = builder.reverse();
                        System.out.println(builder.toString());
                        if (builder.toString().equals("")) {
                            continue;
                        }
                        logList.add(builder.toString());
                        lines++;
                        builder = new StringBuilder();
                        if (lines == lastLine) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(logList);
        return logList;
    }

    @Override
    public void sendSupportLogs() {
        disposables.add(model.sendLogsToSupport(new SupportLogModel(
                        TMUtil.getTimeUTC(System.currentTimeMillis()),
                        PreferenceManager.getInstance().getCourseName(),
                        PreferenceManager.getInstance().getCourse(),
                        TMUtil.getDeviceIMEI(),
                        getLogsFromFile()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> clearFixesFile(),
                        e -> {
                            Log.d(getClass().getName(), "sendSupportLogs: " + e.getMessage());
                            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "sendSupportLogs: " + e.getMessage());
                        })
        );
    }

    private List<String> getLogsFromFile() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(LogFileConstants.fixes_filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> logList = new ArrayList<>();
        while (Objects.requireNonNull(scanner).hasNextLine()) {
            logList.add(scanner.nextLine());
        }
        return logList;
    }

    private void clearFixesFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
        PrintWriter writer = null;
        if (file.exists()) {
            try {
                writer = new PrintWriter(file);
                writer.print("");
                writer.close();
            } catch (FileNotFoundException e) {
            }
        }
    }

    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive) {
        disposables.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("LOG COMPLETED");
                }, Timber::d)
        );
    }

    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive, String lat, String lon, String accuracy) {
        disposables.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive, lat, lon, accuracy)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("LOG COMPLETED");
                }, Timber::d)
        );
    }
}
