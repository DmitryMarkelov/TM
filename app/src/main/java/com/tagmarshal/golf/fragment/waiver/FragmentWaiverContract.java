package com.tagmarshal.golf.fragment.waiver;

import com.tagmarshal.golf.rest.model.Disclaimer;

import io.reactivex.Single;
import okhttp3.ResponseBody;

public class FragmentWaiverContract {
    public interface View {
        void showWaiver(Disclaimer disclaimer);
    }

    public interface Presenter {
        void getWaiver();

        void onAgree();
    }

    public interface Model {
        Single<ResponseBody> onAgree();
    }
}
