package com.tagmarshal.golf.fragment.roundend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentRoundEndPresenter implements FragmentRoundEndContract.Presenter {

    private final FragmentRoundEndContract.Model model = new FragmentRoundEndModel();
    private final FragmentRoundEndContract.View view;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable disposableReturnDevicePopup;
    private Disposable disposableReturnDeviceScreen;
    private Disposable disposablePlugInInterval;
    private Disposable checkBatteryTimer;
    private Disposable dismissRoundInfoTimer;



    public FragmentRoundEndPresenter(FragmentRoundEndContract.View view) {
        this.view = view;
    }

    @SuppressLint("CheckResult")
    @Override
    public void startGettingRoundInfo() {
        disposables.add(model.getRoundInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RestInRoundModel>() {
                    @Override
                    public void onNext(RestInRoundModel restInRoundModel) {
                        view.onGetRoundInfo(restInRoundModel);
                        EventBus.getDefault().postSticky(new RoundInfoEvent(restInRoundModel));
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
    public void startShowingReturnDevicePopup() {
        disposableReturnDevicePopup = model.startShowingReturnDevicePopup()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.onShowReturnDeviceScreen();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposables.add(disposableReturnDevicePopup);
    }

    @Override
    public void startShowingReturnDeviceScreen() {
        disposableReturnDeviceScreen = model.startShowingReturnDeviceScreen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.onShowReturnDeviceScreen();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposables.add(disposableReturnDeviceScreen);
    }

    @Override
    public void startPlugInInterval() {
        disposablePlugInInterval = model.startPlugInInterval()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (TMUtil.isCharging(GolfApplication.context)) {
                            view.disableIntervals();

                            if (disposablePlugInInterval != null && !disposablePlugInInterval.isDisposed()) {
                                disposablePlugInInterval.dispose();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposables.add(disposablePlugInInterval);
    }


    @Override
    public void startChargeCheckTimer(Context context){
        if(checkBatteryTimer!=null){
            Log.d(getClass().getName(), "Checkbattery timer already started ");
            if(!checkBatteryTimer.isDisposed()) {
                checkBatteryTimer.dispose();
            }
            checkBatteryTimer = null;
        }

        Log.d(getClass().getName(), "Checkbattery timer has been started");

        checkBatteryTimer = Observable.interval(0, 350, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (TMUtil.isCharging(context)) {
                            view.goToGroupPageOrBackToRoot();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        disposables.add(checkBatteryTimer);
    }


    @Override
    public void stopShowingReturnDeviceNotifications() {
        if (disposableReturnDevicePopup != null && !disposableReturnDevicePopup.isDisposed()) {
            disposables.remove(disposableReturnDevicePopup);
            disposableReturnDevicePopup.dispose();
        }

        if (disposableReturnDeviceScreen != null && !disposableReturnDeviceScreen.isDisposed()) {
            disposables.remove(disposableReturnDeviceScreen);
            disposableReturnDeviceScreen.dispose();
        }
    }


    @Override
    public void stopCheckingCharge() {
        if (checkBatteryTimer != null && !checkBatteryTimer.isDisposed()) {
            checkBatteryTimer.dispose();
            disposables.remove(checkBatteryTimer);
            checkBatteryTimer = null;
        }
    }


    @Override
    public void onDestroy() {
        stopCheckingCharge();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    @Override
    public void startRoundInfoDismiss(int seconds) {
        if(dismissRoundInfoTimer != null) {
            if(!dismissRoundInfoTimer.isDisposed()) {
                dismissRoundInfoTimer.dispose();
            }
            dismissRoundInfoTimer = null;
        }

        dismissRoundInfoTimer = model.startRoundInfoDismissTimer(seconds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.dismissRoundInfo();
                    }

                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onComplete() { }
                });
        disposables.add(dismissRoundInfoTimer);
    }
}
