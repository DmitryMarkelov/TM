package com.tagmarshal.golf.fragment.roundinfo;

import com.tagmarshal.golf.rest.model.RestInRoundModel;
import io.reactivex.Single;

public interface FragmentRoundInfoContract {

    interface View {
        void onGetRoundInfo(RestInRoundModel roundModel);

        void showLoadingDialog(boolean show);

        void onGetRoundInfoFailure(String message);
    }

    interface Model {
        Single<RestInRoundModel> getRoundInfo();
    }

    interface Presenter {
        void getRoundInfo();

        void onDestroy();
    }
}
