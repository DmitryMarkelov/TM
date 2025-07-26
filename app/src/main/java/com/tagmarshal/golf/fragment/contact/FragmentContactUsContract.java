package com.tagmarshal.golf.fragment.contact;

import java.util.Map;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FragmentContactUsContract {

    interface View {
        void shoWaitDialog(boolean show);

        void showRequestFailure(String message);

        void onGetContactNumber(String number);

        void onMessageSent();
    }

    interface Model {
        Single<Response<ResponseBody>> getContactNumber();

        Single<Response<ResponseBody>> sendMessage(Map<String, String> body);
    }

    interface Presenter {
        void getContactNumber();

        void sendMessage(Map<String, String> body);

        void onDestroy();
    }
}
