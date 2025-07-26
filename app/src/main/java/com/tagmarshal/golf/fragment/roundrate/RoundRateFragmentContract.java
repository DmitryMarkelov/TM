package com.tagmarshal.golf.fragment.roundrate;

import com.tagmarshal.golf.rest.model.RestRatingModel;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface RoundRateFragmentContract {

    interface View {
        void openEndOfRoundScreen();
        void onRateSuccess();
        void onRateFailure(String message);
        void onSuccessBadRating();
    }

    interface Model {
        Single<Response<ResponseBody>> rateRound(String roundID, RestRatingModel ratingModel);

        Single<Response<ResponseBody>> sendBadRound(String imei);
    }

    interface Presenter {
        void rateRound(String imei,
                       String position,
                       int rating,
                       String comment);

        void sendBadRound(String imei);
        void onDestroy();
        void closeTimer();
    }
}
