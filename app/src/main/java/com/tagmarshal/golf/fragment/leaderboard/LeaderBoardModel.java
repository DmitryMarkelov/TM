package com.tagmarshal.golf.fragment.leaderboard;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Single;

public class LeaderBoardModel implements LeaderBoardContract.Model {

    @Override
    public Single<RestInRoundModel> getRoundInfo() {
        return GolfAPI.getGolfCourseApi().getDeviceInfo();
    }
}
