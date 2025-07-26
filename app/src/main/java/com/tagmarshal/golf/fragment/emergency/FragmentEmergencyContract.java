package com.tagmarshal.golf.fragment.emergency;

import java.util.Map;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FragmentEmergencyContract {

    interface View {
        void showWaitDialog(boolean show);

        void onMessageSent();

        void onRequestFailure(String message);
    }

    interface Model {
        Single<Response<ResponseBody>> sendMessage(Map<String, String> body);
    }

    interface Presenter {
        void sendMessage(Map<String, String> body);

        void onDestroy();
    }
}
