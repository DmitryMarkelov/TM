package com.tagmarshal.golf.fragment.roundinfo;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Single;

public class FragmentRoundInfoModel implements FragmentRoundInfoContract.Model {
    @Override
    public Single<RestInRoundModel> getRoundInfo() {
        return GolfAPI.getGolfCourseApi().getDeviceInfo();
    }
}
