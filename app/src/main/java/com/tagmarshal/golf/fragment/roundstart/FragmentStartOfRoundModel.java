package com.tagmarshal.golf.fragment.roundstart;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class FragmentStartOfRoundModel implements FragmentStartOfRoundContract.Model {
    @Override
    public Observable<Long> startTimer() {
        return Observable.interval(0, 15, TimeUnit.SECONDS);
    }
}
