package com.tagmarshal.golf.fragment.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.FileWorkerContract;
import com.tagmarshal.golf.data.FileWorkerModel;
import com.tagmarshal.golf.eventbus.FirebaseGetAlerts;
import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.manager.GameManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class FragmentMapPresenter implements FragmentMapContract.Presenter, FileWorkerContract.Presenter {

    private final FragmentMapContract.View view;
    private final FragmentMapContract.Model model;
    private final CompositeDisposable disposable;
    private final FileWorkerModel fileModel;
    private final String tag = this.getClass().getName();
    boolean sendingLogs = false;
    private Disposable rxCountDownTimerToStartGame;
    private Disposable mapBoundDisposable;
    private Disposable slideShowDisposable;
    private Disposable makeNotificationDisposable;

    public FragmentMapPresenter(FragmentMapContract.View view) {
        this.view = view;
        this.model = new FragmentMapModel();
        this.disposable = new CompositeDisposable();
        this.fileModel = new FileWorkerModel();
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void getBoundsForMap() {
        getGeoFences(TMUtil.getDeviceIMEI());
        getGeoZones();
        if (PreferenceManager.getInstance().getMapBounds() != null
                && PreferenceManager.getInstance().getMapBounds().getNELAT() != null
                && !PreferenceManager.getInstance().getMapBounds().getNELAT().isEmpty()) {
            view.onGetMapBounds(PreferenceManager.getInstance().getMapBounds());
            Log.d(getClass().getName(), "GET MAP BOUNDS => local ");
            return;
        }
        if (mapBoundDisposable != null && !mapBoundDisposable.isDisposed()) {
            disposable.remove(mapBoundDisposable);
            mapBoundDisposable.dispose();
        }
        mapBoundDisposable = model.getMapBounds(TMUtil.getDeviceIMEI())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestBoundsModel>>() {
                    @Override
                    public void onSuccess(List<RestBoundsModel> restBoundsModels) {
                        PreferenceManager.getInstance().setMapBounds(restBoundsModels.get(0));
                        Log.d(getClass().getName(), "GET MAP BOUNDS => remote ");
                        view.onGetMapBounds(restBoundsModels.get(0));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
        disposable.add(mapBoundDisposable);
    }

    @SuppressLint("CheckResult")
    @Override
    public void getCourseBackground(final String courseBackground, Context context) {
        if (courseBackground != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(courseBackground)
                    .signature(new ObjectKey(PreferenceManager.getInstance().getMapVersion()))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(new CustomTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onStop() {

                        }

                        @Override
                        public void onDestroy() {

                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            writeToFile(LogFileConstants.map_failed,
                                    TMUtil.getTimeUTC(System.currentTimeMillis()),
                                    String.valueOf(TMUtil.getBatteryLevel()),
                                    String.valueOf(TMUtil.isOnline()));
                        }

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            view.onGetCourseBackground(resource);
                            Timber.tag(getClass().getName()).d("MAP BACKGROUND => SUCCESS");
                            writeToFile(LogFileConstants.map_downloaded,
                                    TMUtil.getTimeUTC(System.currentTimeMillis()),
                                    String.valueOf(TMUtil.getBatteryLevel()),
                                    String.valueOf(TMUtil.isOnline()));
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }

    @Override
    public void getAdvertisements(Context context) {
        disposable.add(model.getAdvertisements()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        advertisements -> {
                            saveAdvertisements(advertisements);
                            startDownloads(context);
                        },
                        e -> {
                            Timber.d("getAdvertisements: %s", e.getMessage());
                        }
                )
        );
    }

    public List<AdvertisementModel> getLocalAdvertisements() {
        return PreferenceManager.getInstance().getAdvertisements();
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
    public void getCourses() {
        if (!PreferenceManager.getInstance().nearSaved()) {
            disposable.add(model.getCourses()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(coosList -> PreferenceManager.getInstance().saveNearestCourses(coosList))
            );
        }
    }

    @Override
    public void getBitmapDescriptionFromUrl(PointOfInterest item, Context context) {
        String extension = "";
        int i = item.getImage().lastIndexOf('.');
        if (i > 0) {
            extension = item.getImage().substring(i + 1);
        }
        if (extension.equals("svg")) {
            disposable.add(model.getPictureDrawable(item.getImage(), context)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(pictureDrawable -> {
                        final Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        final Canvas canvas = new Canvas(bitmap);
                        canvas.drawPicture(pictureDrawable.getPicture());
                        return bitmap;
                    })
                    .map(bitmap -> resizeBitmap(bitmap, 32, 32))
                    .map(BitmapDescriptorFactory::fromBitmap)
                    .subscribe(
                            s -> view.onBitmapFromUrl(item, s),
                            e -> Log.d(getClass().getName(), e.getMessage())
                    ));
        } else {
            disposable.add(model.getImage(item.getImage(), context)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(drawable -> ((BitmapDrawable) drawable).getBitmap())
                    .map(bitmap -> resizeBitmap(bitmap, 32, 32))
                    .map(BitmapDescriptorFactory::fromBitmap)
                    .subscribe(
                            s -> view.onBitmapFromUrl(item, s),
                            e -> Log.d(getClass().getName(), e.getMessage())
                    )

            );
        }

    }

    private Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) dpToPx(w)) / width;
        float scaleHeight = ((float) dpToPx(h)) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void writeToFile(String event, String time, String battery, String isconnected) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            String log = event + " : " + time + " : " + battery + " : " + isconnected;
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(log);
            writer.append("\n");

            writer.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void getHoleInfo(int hole) {
        if (GameManager.isExistHoles()) {
            view.onGetHoleInfo(GameManager.getHole(hole), hole);
            return;
        }

        disposable.add(model.getHoleInfo(hole)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestHoleModel>>() {
                    @Override
                    public void onSuccess(List<RestHoleModel> restHoleModels) {
                        view.onGetHoleInfo(restHoleModels.get(0), 1);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @Override
    public void getGeoFences(String imei) {
        disposable.add(model.getGeoFences(imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestGeoFenceModel>>() {
                    @Override
                    public void onSuccess(List<RestGeoFenceModel> geoFenceModels) {
                        PreferenceManager.getInstance().setCourseGeoFences(geoFenceModels);
                        view.onGetGeoFences(geoFenceModels);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (PreferenceManager.getInstance().getGeoFences() != null && !PreferenceManager.getInstance().getGeoFences().isEmpty()) {
                            view.onGetGeoFences(PreferenceManager.getInstance().getGeoFences());
                        }
                    }
                }));
    }

    @Override
    public void getGeoZones() {
        disposable.add(model.getGeoZones()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestGeoZoneModel>>() {
                    @Override
                    public void onSuccess(List<RestGeoZoneModel> geoFenceModels) {
                        PreferenceManager.getInstance().setCourseGeoZones(geoFenceModels);
                        view.onGetGeoZones();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (PreferenceManager.getInstance().getGeoZones() != null && !PreferenceManager.getInstance().getGeoZones().isEmpty()) {
                            view.onGetGeoZones();
                        }
                    }
                }));
    }

    @Override
    public void startCheckGeoFences() {
        disposable.add(model.startCheckGeoFences()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.onCheckGeoFences();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void startCountDownTimerToStartGame() {
        rxCountDownTimerToStartGame = model.startCountDownTimerToStartGame()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.onCountDownTimerToStartGameTick();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposable.add(rxCountDownTimerToStartGame);
    }

    @Override
    public void stopCountDownTimerToStartGame() {
        if (rxCountDownTimerToStartGame != null && !rxCountDownTimerToStartGame.isDisposed()) {
            rxCountDownTimerToStartGame.dispose();
            rxCountDownTimerToStartGame = null;
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void getTeeTimes() {
        disposable.add(model.getTeeTimes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestTeeTimesModel>>() {
                    @Override
                    public void onSuccess(List<RestTeeTimesModel> restTeeTimesModels) {
                        view.onGetTeeTimes(restTeeTimesModels);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getRoundInfo() {
        disposable.add(model.getRoundInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RestInRoundModel>() {
                    @Override
                    public void onSuccess(RestInRoundModel roundModel) {
                        EventBus.getDefault().postSticky(new RoundInfoEvent(roundModel));
                        view.onGetRoundInfo(roundModel);
                        if (roundModel.isMessagesPending()) {
                            EventBus.getDefault().postSticky(new FirebaseGetAlerts());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getHoleDistanceFromTee(int hole) {
        RestHoleModel restHoleModel = GameManager.getHole(hole);
        if (GameManager.isExistHoles() && restHoleModel != null) {
            RestHoleDistanceModel distanceModel =
                    TMUtil.getDistanceFromTeeLocation(restHoleModel);
            view.onGetHoleDistanceFromTee(distanceModel);
            return;
        }

        disposable.add(model.getHoleDistanceInfo(hole)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<RestHoleDistanceModel>>() {
                    @Override
                    public void onSuccess(List<RestHoleDistanceModel> restHoleModels) {
                        if (restHoleModels.size() > 0)
                            view.onGetHoleDistanceFromTee(restHoleModels.get(0));
                        else {
                            RestHoleDistanceModel model = new RestHoleDistanceModel();
                            model.setBack("0");
                            model.setCenter("0");
                            model.setFront("0");
                            view.onGetHoleDistanceFromTee(model);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void gotAlert(String alertId) {
        disposable.add(model.gotAlert(alertId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getAllHoles() {
        model.getAllHoles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(unsortedList -> {
                    List<RestHoleModel> sortedList = new ArrayList<RestHoleModel>(unsortedList);
                    Collections.sort(sortedList, (restHoleModel, t1) -> restHoleModel.getOrder() - t1.getOrder());

                    for (int i = 0; i < sortedList.size(); i++) {
                        sortedList.get(i).setOrder(i + 1);
                    }
                    return sortedList;
                })
                .subscribe(new Observer<List<RestHoleModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(List<RestHoleModel> restHoleModels) {
                        GameManager.setHoles(restHoleModels);
                        view.onGetAllHoles(restHoleModels);
                    }

                    @Override
                    public void onError(Throwable e) {
                        List<RestHoleModel> holes = PreferenceManager.getInstance().getHoles();
                        if (holes != null && !holes.isEmpty()) {
                            GameManager.setHoles(holes);
                            view.onGetAllHoles(holes);
                            view.onFail("You're in synchronizing mode");
                        } else {
                            view.onFail(e.getMessage());
                        }

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void getHoleDistanceFromPoint(LatLng tapLocation, RestHoleModel holeModel) {
        disposable.add(model.getHoleDistanceFromPoint(tapLocation, holeModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RestHoleDistanceModel>() {
                    @Override
                    public void onSuccess(RestHoleDistanceModel restHoleDistanceModel) {
                        view.onGetHoleDistanceFromPoint(restHoleDistanceModel);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getHoleDistanceFromPointAndMyLocation(LatLng myLocation, LatLng tapLocation,
                                                      RestHoleModel holeModel) {
        disposable.add(model.getHoleDistanceFromPointAndMyLocation(myLocation, tapLocation, holeModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RestHoleDistanceModel>() {
                    @Override
                    public void onSuccess(RestHoleDistanceModel restHoleDistanceModel) {
                        view.onGetHoleDistanceFromPointAndMyLocation(restHoleDistanceModel);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @Override
    public void sendLocationFix(FixModel fix) {
        disposable.add(model.sendLocationFix(fix)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody s) {
                        try {
                            String responseText = s.string();
                            if (responseText.equals("OK")) {
                                Log.d(getClass().getName(), "restricted zone fix =>  success");
                            }
                        } catch (Exception e) {
                            Log.d(getClass().getName(), "restricted zone fix =>  fail" + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(getClass().getName(), "restricted zone fix =>  fail" + e.getMessage());
                    }
                }));
    }

    @Override
    public void getPointsOfInterest() {
        disposable.add(model.getPointsOfInterest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        view::drawPointsOfInterest,
                        e -> Log.d(getClass().getName(), e.getMessage())
                )
        );
    }

    @Override
    public void stopSlideShowDismissTimer() {
        if (slideShowDisposable != null && !slideShowDisposable.isDisposed()) {
            disposable.remove(slideShowDisposable);
            slideShowDisposable.dispose();
            slideShowDisposable = null;
        }
    }

    @Override
    public void startSlideShowDismissTimer() {
        if (slideShowDisposable != null && !slideShowDisposable.isDisposed()) {
            return;
        }
//        stopSlideShowDismissTimer();
        slideShowDisposable = Completable.timer(7, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onSlideShowDismiss, Timber::d);
        disposable.add(slideShowDisposable);
    }

    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive) {
        disposable.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("LOG COMPLETED");
                }, Timber::d)
        );
    }

    @Override
    public void writeToFile(String tag, String time, String battery, String isOnline, String inActive, String lat, String lon, String accuracy) {
        disposable.add(fileModel.writeToFile(tag, time, battery, isOnline, inActive, lat, lon, accuracy)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("LOG COMPLETED");
                }, Timber::d)
        );
    }

    @Override
    public void checkDistancesFromTappedToCurrent(String currentDistaneToHole, LatLng location, RestHoleModel currentModel) {
        disposable.add(model.getHoleDistanceFromPoint(location, currentModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(RestHoleDistanceModel::getCenter)
                .mergeWith(Single.just(currentDistaneToHole))
                .map(Integer::parseInt)
                .reduce((c, p) -> p - c)
                .subscribe(s -> {
                    if (s < 0) {
                        view.clearHoleDistances();
                    }
                })
        );

    }

    @Override
    public void startMakingNotification() {
        if (makeNotificationDisposable != null) {
            Log.d(tag, "Make notification timer already started");
            return;

        }

        Log.d(tag, "Make notification timer has been started");

        makeNotificationDisposable = model.startNotificationTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.makeNotification();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(tag, "Make notification  timer error = " + e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposable.add(makeNotificationDisposable);
    }

    @Override
    public void stopMakingNotification() {
        if (makeNotificationDisposable != null && !makeNotificationDisposable.isDisposed()) {
            makeNotificationDisposable.dispose();
            disposable.remove(makeNotificationDisposable);
            makeNotificationDisposable = null;
        }
    }

    @Override
    public void sendSupportLogs(String roundId) {
        List<String> logs = getLogsFromFile();
        if (logs.size() > 1 && !sendingLogs) {
            sendingLogs = true;
            SupportLogModel supportLogModel = new SupportLogModel(
                    TMUtil.getDeviceIMEI(),
                    logs,
                    roundId != null ? roundId : "no-round"
            );
            disposable.add(model.sendLogsToSupport(supportLogModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                                clearFixesFile();
                                sendingLogs = false;
                            },
                            e -> {
                                sendingLogs = false;
                                Log.d(getClass().getName(), "sendSupportLogs: " + e.getMessage());
                                FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getClass().getName() + " " + "sendSupportLogs: " + e.getMessage());
                            })
            );
        }
    }

    private List<String> getLogsFromFile() {
        File file = new File(LogFileConstants.raw_fixes_filePath);
        List<String> logList = new ArrayList<>();
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
                        if (lines == 5100) {
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


    private void clearFixesFile() {
        File file = new File(LogFileConstants.raw_fixes_filePath);
        if (file.exists()) {
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.print("");
                writer.close();
            } catch (FileNotFoundException e) {
            }
        }
    }
}
