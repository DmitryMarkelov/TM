package com.tagmarshal.golf.fragment.groups.pager;

import com.tagmarshal.golf.rest.model.RestTeeTimesModel;

import java.util.List;

import io.reactivex.Single;

public interface FragmentGroupsPagerContract {

    interface View {
        void showGroups(List<RestTeeTimesModel> groups);

        void showWaitDialog(boolean show);

        void onInactiveState();

        void showFailureMessage(String message);
    }

    interface Model {
        Single<List<RestTeeTimesModel>> getGroups();

        Single<Long> startInactiveStateTimer();
    }

    interface Presenter {
        void onCreate();

        void onDestroy();

        void startInactiveStateTimer();

        void getGroups();
    }
}
