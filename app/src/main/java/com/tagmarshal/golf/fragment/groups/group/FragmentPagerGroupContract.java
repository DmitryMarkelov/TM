package com.tagmarshal.golf.fragment.groups.group;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FragmentPagerGroupContract {

    interface Model {
        Single<Response<ResponseBody>> confirmOrRemoveGroup(String id, String action);

        Single<Response<ResponseBody>> sendSkipMessage();

    }

    interface Presenter {
        void confirmOrRemoveGroup(String id, String action);

        void onDestroy();

        void sendSkipMessage();
    }

    interface View {
        void onSkipMessageSuccess();

        void onConfirmationSuccess();

        void onConfirmationFailure(String message);

        void showWaitDialog(boolean show);
    }
}
