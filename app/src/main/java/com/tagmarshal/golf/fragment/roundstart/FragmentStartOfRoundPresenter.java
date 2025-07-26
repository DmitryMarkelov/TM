package com.tagmarshal.golf.fragment.roundstart;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentStartOfRoundPresenter implements FragmentStartOfRoundContract.Presenter {

    private final FragmentStartOfRoundContract.View view;
    private final FragmentStartOfRoundContract.Model model;

    private final CompositeDisposable compositeDisposable;

    public FragmentStartOfRoundPresenter(FragmentStartOfRoundContract.View view) {
        this.view = view;
        model = new FragmentStartOfRoundModel();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void startCountDownTimer() {
        launchCountDownTimer();
    }

    @Override
    public void stopCountDownTimer() {

    }

    @Override
    public void onDestroy() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public void onCreate() {
        launchCountDownTimer();
    }

    private void launchCountDownTimer() {
        compositeDisposable.add(model.startTimer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.countDownTimerTick();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}
