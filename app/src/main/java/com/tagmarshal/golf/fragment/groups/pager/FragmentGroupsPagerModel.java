package com.tagmarshal.golf.fragment.groups.pager;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;

public class FragmentGroupsPagerModel implements FragmentGroupsPagerContract.Model {


    @Override
    public Single<Long> startInactiveStateTimer() {
        return Single.timer(30, TimeUnit.SECONDS);
    }

    @Override
    public Single<List<RestTeeTimesModel>> getGroups() {
        return GolfAPI.getGolfCourseApi().getTeeTimes();
    }
}
