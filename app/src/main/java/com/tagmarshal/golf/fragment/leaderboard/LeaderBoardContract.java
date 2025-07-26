package com.tagmarshal.golf.fragment.leaderboard;

import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Single;

public interface LeaderBoardContract {

    interface View{
        void onGetRoundInfo(RestInRoundModel restInRoundModel);
        void onFailure(String message);
        void showLoading(boolean show);
    }


    interface Model {
        Single<RestInRoundModel> getRoundInfo();
    }

    interface Presenter {
        void getRoundInfo();
        void onDestroy();
    }
}
