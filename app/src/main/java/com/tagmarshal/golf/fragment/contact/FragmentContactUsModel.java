package com.tagmarshal.golf.fragment.contact;

import com.tagmarshal.golf.rest.GolfAPI;

import java.util.Map;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentContactUsModel implements FragmentContactUsContract.Model {

    @Override
    public Single<Response<ResponseBody>> getContactNumber() {
        return GolfAPI.getGolfCourseApi().getContactNumber();
    }

    @Override
    public Single<Response<ResponseBody>> sendMessage(Map<String, String> body) {
        return GolfAPI.getGolfCourseApi().sendMessage(body);
    }
}
