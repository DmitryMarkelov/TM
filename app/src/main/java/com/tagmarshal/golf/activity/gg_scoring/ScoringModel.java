package com.tagmarshal.golf.activity.gg_scoring;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.SaveScoreModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

public class ScoringModel implements ScoringContract.Model {


    @Override
    public Observable<Long> startInactiveStateTimer() {
        return Observable.interval(1, TimeUnit.MINUTES);
    }

    @Override
    public Observable<Long> startBackToMapTimer() {
        return Observable.timer(15, TimeUnit.SECONDS);
    }

    @Override
    public Single<ResponseBody> insertScore(SaveScoreModel saveScoreModel) {
        return GolfAPI.getGolfCourseApi().insertScore(saveScoreModel, saveScoreModel.getRoundId());
    }
}
