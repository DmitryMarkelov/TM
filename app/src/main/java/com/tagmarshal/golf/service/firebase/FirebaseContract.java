package com.tagmarshal.golf.service.firebase;

import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface FirebaseContract {
    interface View{
        void onSuccessRefreshToken();
        void onError(String message);
        void onSuccessMapRefresh();

        void onSuccessReregister();

        void onGeoFenceGet();
    }

    interface Presenter{
        void sendRefreshToken(Map<String, String> token);
        void refreshMap();


        void getCourses();

        void sendSupportLogs();

        void getAdvertisements();

        void getGeofenceById(ArrayList<String> listItems);

        void getHoles();

        void sendSupportLogs(String limit);

        void reregister(String units, String type, String build);

        void downloadFirmware(String fileName);
    }

    interface Model{
        Single<Response<ResponseBody>> reregister(String units, String type, String build);
        Single<ResponseBody> sendLogsToSupport(SupportLogModel supportLogs);
        Single<List<CourseModel>> getCourses();


        Single<Response<ResponseBody>> sendRefreshToken(Map<String, String> token);

        Observable<List<AdvertisementModel>> getAdvertisements();

        Single<List<RestGeoFenceModel>> getGeoFences(String deviceIMEI);

        Single<List<RestHoleModel>> getHoles();

        Single<List<RestGeoFenceModel>> getGeofenceById(ArrayList<String> listItems);
    }
}
