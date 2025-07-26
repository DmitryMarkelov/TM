package com.tagmarshal.golf.fragment.alert;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class FragmentAlertModel implements FragmentAlertContract.Model {

    @Override
    public Observable<Long> startNotificationTimer() {
        return Observable.interval(0,5,TimeUnit.SECONDS);
    }
}
