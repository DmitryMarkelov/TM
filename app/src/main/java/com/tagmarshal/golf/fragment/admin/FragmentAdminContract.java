package com.tagmarshal.golf.fragment.admin;

import android.annotation.SuppressLint;

import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FragmentAdminContract {

    interface View {
        void onSentEndOfRound();

        void onRequestFailure(String message);

        void showWaitDialog(boolean show);

        void onGetRoundInfo(RestInRoundModel restInRoundModel);
    }

    interface Model {
        Single<Response<ResponseBody>> sendEndOfRound();

        Single<RestInRoundModel> getRoundInfo();

        Single<ResponseBody> sendLogsToSupport(SupportLogModel supportLogs);
    }

    interface Presenter {
        void sendEndOfRound();

        void sendSupportLogs();

        @SuppressLint("CheckResult")
        void getRoundInfo();

        void onDestroy();
    }
}
