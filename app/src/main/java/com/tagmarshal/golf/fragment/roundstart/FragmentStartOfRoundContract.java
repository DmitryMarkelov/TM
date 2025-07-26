package com.tagmarshal.golf.fragment.roundstart;

import io.reactivex.Observable;

public interface FragmentStartOfRoundContract {

    interface Model {
        Observable<Long> startTimer();
    }

    interface Presenter {
        void startCountDownTimer();

        void stopCountDownTimer();

        void onDestroy();

        void onCreate();
    }

    interface View {
        void countDownTimerTick();
    }
}
