package com.tagmarshal.golf.fragment.waiver;

import com.tagmarshal.golf.rest.GolfAPI;

import io.reactivex.Single;
import okhttp3.ResponseBody;

public class FragmentWaiverModel implements FragmentWaiverContract.Model {
    @Override
    public Single<ResponseBody> onAgree() {
        return GolfAPI.getGolfCourseApi().acceptDisclaimer();
    }
}
