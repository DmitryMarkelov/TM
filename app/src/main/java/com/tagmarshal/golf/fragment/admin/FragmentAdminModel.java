package com.tagmarshal.golf.fragment.admin;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;

import java.util.List;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentAdminModel implements FragmentAdminContract.Model {
    @Override
    public Single<Response<ResponseBody>> sendEndOfRound() {
        return GolfAPI.getGolfCourseApi().closeRound();
    }
    @Override
    public Single<RestInRoundModel> getRoundInfo() {
        return  GolfAPI.getGolfCourseApi().getDeviceInfo();

    }

    @Override
    public Single<ResponseBody> sendLogsToSupport(SupportLogModel supportLogs) {
        return GolfAPI.getGolfApi().sendLogsToSupport(supportLogs);
    }
}
