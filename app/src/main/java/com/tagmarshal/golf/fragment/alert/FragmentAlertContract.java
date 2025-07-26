package com.tagmarshal.golf.fragment.alert;

import io.reactivex.Observable;

public interface FragmentAlertContract {
    interface View{
        void makeNotification();

        void goBack();
    }

    interface Model{
        Observable<Long> startNotificationTimer();
    }

    interface Presenter{
        void startMakingNotification();
        void stopMakingNotification();
        void onDestroy();

    }

}
