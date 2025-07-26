package com.tagmarshal.golf.data;

import io.reactivex.Completable;

public interface FileWorkerContract {

    interface Model {
        Completable writeToFile(String tag, String time, String battery, String isOnline, String inActive);

        Completable writeToFile(String tag, String time, String battery);

        Completable writeToFile(String tag, String time, String battery, String isOnline, String inActive, String lat, String lon, String accuracy);

        Completable writeInAccuracyToFile(String tag, String time, String battery, String isOnline, String inActive, String lat, String lon, String accuracy);
    }

    interface Presenter {


        default void writeToFile(String tag, String time, String battery, String isOnline, String inActive) {
        }

        default void writeToFile(String tag, String time, String battery) {
        }

        default void writeToFile(String tag, String time, String battery, String isOnline, String inActive, String lat, String lon, String accuracy) {
        }



    }


}
