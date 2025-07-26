package com.tagmarshal.golf.fragment.calibration;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CalibrationPresenter {
    private final CalibrationView view;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public CalibrationPresenter(CalibrationView view) {
        this.view = view;
    }

    public void startDismissingTimer() {
        disposables.add(
                Observable.timer(5, TimeUnit.MINUTES)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> view.onCalibrationFinish()
                                , error -> view.onCalibrationError(error))
        );
    }

    public void onDestroy() {
        disposables.dispose();
    }
}
