package com.tagmarshal.golf.fragment.alert;


import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentAlertPresenter implements FragmentAlertContract.Presenter {

    private final String tag = this.getClass().getName();

    private final FragmentAlertContract.View view;
    private final FragmentAlertContract.Model model = new FragmentAlertModel();

    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable makeNotificationDisposable;

    public FragmentAlertPresenter(FragmentAlertContract.View view) {
        this.view = view;
    }



    @Override
    public void startMakingNotification() {
        if(makeNotificationDisposable!=null){
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

        disposables.add(makeNotificationDisposable);
    }

    @Override
    public void stopMakingNotification() {
        if (makeNotificationDisposable != null && !makeNotificationDisposable.isDisposed()) {
            makeNotificationDisposable.dispose();
            disposables.remove(makeNotificationDisposable);
            makeNotificationDisposable = null;
        }
    }

    @Override
    public void onDestroy() {
        if(!disposables.isDisposed()){
            disposables.dispose();
        }
    }
}
