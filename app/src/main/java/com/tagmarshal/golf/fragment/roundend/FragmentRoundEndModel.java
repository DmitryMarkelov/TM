package com.tagmarshal.golf.fragment.roundend;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class FragmentRoundEndModel implements FragmentRoundEndContract.Model {
    @Override
    public Observable<Long> startRoundInfoDismissTimer(int seconds) {
        return Observable.timer(seconds, TimeUnit.SECONDS);
    }

    @Override
    public Observable<RestInRoundModel> getRoundInfo() {
        return Observable.interval(0, 1, TimeUnit.MINUTES).flatMap((Function<Long, ObservableSource<RestInRoundModel>>) aLong -> GolfAPI.getGolfCourseApi().getRepeatDeviceInfo());
    }

    @Override
    public Observable<Long> startShowingReturnDevicePopup() {
        return Observable.interval(0, 5, TimeUnit.MINUTES);
    }

    @Override
    public Observable<Long> startShowingReturnDeviceScreen() {
        return Observable.interval(1, 1, TimeUnit.MINUTES);
    }

    @Override
    public Observable<Long> startPlugInInterval() {
        return Observable.interval(0, 150, TimeUnit.MILLISECONDS);
    }
}
