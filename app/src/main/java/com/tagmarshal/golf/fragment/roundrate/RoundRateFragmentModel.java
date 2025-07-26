package com.tagmarshal.golf.fragment.roundrate;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.RestRatingModel;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class RoundRateFragmentModel implements RoundRateFragmentContract.Model {

    @Override
    public Single<Response<ResponseBody>> rateRound(String roundID, RestRatingModel ratingModel) {
        return GolfAPI.getGolfCourseApi().rateRound(ratingModel, roundID);
    }

    @Override
    public Single<Response<ResponseBody>> sendBadRound(String imei) {
        return GolfAPI.getGolfCourseApi().sendBadRating(imei);
    }
}
