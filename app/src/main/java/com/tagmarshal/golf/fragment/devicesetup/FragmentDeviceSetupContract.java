package com.tagmarshal.golf.fragment.devicesetup;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FragmentDeviceSetupContract {

    interface View {
        void onVariableRegistrationSuccess(String tag);

        void onVariableRegistrationFailure(String error);

        void showWaitDialog(boolean show);
    }

    interface Model {
        Single<Response<ResponseBody>> registerVariableDevice(String imei, String type, String build);
    }

    interface Presenter {
        void registerVariableDevice(String imei, String type, String build);

        void onDestroy();
    }
}
