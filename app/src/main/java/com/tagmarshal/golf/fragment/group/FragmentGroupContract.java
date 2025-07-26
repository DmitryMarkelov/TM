package com.tagmarshal.golf.fragment.group;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FragmentGroupContract {

    interface Presenter {
        void onCreate();

        void onDestroy();

        void confirmOrRemoveGroup(String id, String action);

        void sendSkipMessage();


    }

    interface Model {
        Single<Response<ResponseBody>> confirmOrRemoveGroup(String tag, String groupId);

        Single<Response<ResponseBody>> sendSkipMessage();

    }

    interface View {
        void onChangeGroupSuccess();

        void onConfirmationSuccess();

        void onConfirmationFailure(String message);

        void showWaitDialog(boolean show);

        void onSkipMessageSuccess();
    }

}
