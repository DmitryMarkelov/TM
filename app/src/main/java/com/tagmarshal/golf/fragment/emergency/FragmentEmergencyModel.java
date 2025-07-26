package com.tagmarshal.golf.fragment.emergency;

import com.tagmarshal.golf.rest.GolfAPI;

import java.util.Map;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentEmergencyModel implements FragmentEmergencyContract.Model {

    @Override
    public Single<Response<ResponseBody>> sendMessage(Map<String, String> body) {
        return GolfAPI.getGolfCourseApi().sendMessage(body);
    }
}
