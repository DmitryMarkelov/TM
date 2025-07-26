package com.tagmarshal.golf.fragment.roundend;

import android.content.Context;

import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Observable;

public interface FragmentRoundEndContract {

    interface View {
        void dismissRoundInfo();

        void onGetRoundInfo(RestInRoundModel model);

        void onShowReturnDevicePopup(boolean canMakeNoise);

        void onShowReturnDeviceScreen();

        void disableIntervals();

        void goToGroupPageOrBackToRoot();
    }

    interface Model {
        Observable<Long> startRoundInfoDismissTimer(int seconds);

        Observable<RestInRoundModel> getRoundInfo();

        Observable<Long> startShowingReturnDevicePopup();

        Observable<Long> startShowingReturnDeviceScreen();

        Observable<Long> startPlugInInterval();

    }

    interface Presenter {
        void startGettingRoundInfo();

        void startShowingReturnDevicePopup();

        void startShowingReturnDeviceScreen();

        void startPlugInInterval();

        void startChargeCheckTimer(Context context);

        void stopShowingReturnDeviceNotifications();


        void stopCheckingCharge();

        void onDestroy();

        void startRoundInfoDismiss(int seconds);

    }
}
