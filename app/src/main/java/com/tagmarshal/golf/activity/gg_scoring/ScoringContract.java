package com.tagmarshal.golf.activity.gg_scoring;

import com.tagmarshal.golf.rest.model.SaveScoreModel;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

public interface ScoringContract {

    interface View {
        void onInsertScore(SaveScoreModel saveScoreModel);

        void showWaitDialog(boolean show);

        void showSignalRestored();

        void backToMap();

        void showFailureMessage(SaveScoreModel saveScoreModel);
    }

    interface Model {
        Single<ResponseBody> insertScore(SaveScoreModel saveScoreModel);

        Observable<Long> startInactiveStateTimer();

        Observable<Long> startBackToMapTimer();
    }

    interface Presenter {
        void startInactiveStateTimer();

        void startBackToMapTimer();

        void stopBackToMapTimer();

        void onCreate();

        void onDestroy();

        void insertScore(SaveScoreModel saveScoreModel);
    }
}
