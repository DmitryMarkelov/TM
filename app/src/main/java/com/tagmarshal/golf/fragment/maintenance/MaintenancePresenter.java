package com.tagmarshal.golf.fragment.maintenance;

import com.tagmarshal.golf.data.Maintenance;
import com.tagmarshal.golf.manager.PreferenceManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

class MaintenancePresenter {
    private final MaintenanceView view;

    private final CompositeDisposable disposable = new CompositeDisposable();
    private Disposable maintenceDisposable;


    public MaintenancePresenter(MaintenanceView view) {
        this.view = view;
    }


    public void startTimer() {
        Maintenance maintenance = PreferenceManager.getInstance().getMaintenance();

        if (maintenance != null && maintenance.getFromTime() < System.currentTimeMillis()) {
            if(maintenceDisposable !=null && !maintenceDisposable.isDisposed()){
                maintenceDisposable.dispose();
            }

            if(maintenance.getToTime() == 0){
                return;
            }
            long diff = maintenance.getToTime() - System.currentTimeMillis();
            if (diff > 0) {
                maintenceDisposable = Observable.timer(maintenance.getToTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(success -> view.onTimerFinish(),
                                        error -> view.onTimerError(error));
                disposable.add(maintenceDisposable);
            }
        } else {
            view.onTimerFinish();
        }
    }

    public void onDestroy() {
        disposable.dispose();
    }
}
