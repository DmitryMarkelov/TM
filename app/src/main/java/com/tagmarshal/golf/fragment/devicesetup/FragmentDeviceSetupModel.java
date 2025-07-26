package com.tagmarshal.golf.fragment.devicesetup;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentDeviceSetupModel implements FragmentDeviceSetupContract.Model {

    @Override
    public Single<Response<ResponseBody>> registerVariableDevice(String imei, String type, String build) {
        return GolfAPI.getGolfCourseApi().registerDeviceUrl("https://" + PreferenceManager.getInstance().getCurrentCourseModel().getCourseUrl() + "/app/registerdevice/" + imei + "/type/" + type + "/" + build + "/");
    }
}
