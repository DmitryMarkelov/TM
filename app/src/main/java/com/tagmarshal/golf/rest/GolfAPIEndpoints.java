package com.tagmarshal.golf.rest;
import com.tagmarshal.golf.rest.model.LogInModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GolfAPIEndpoints {

    @GET("courses/imei/{imei}")
    Single<Response<ResponseBody>> getCourses(@Path("imei") String imei);

    @POST("uploadlogs")
    Single<ResponseBody> sendLogsToSupport(@Body SupportLogModel logModel);

    @POST("raw-fixes")
    Single<ResponseBody> sendRawLogsToSupport(@Body SupportLogModel logModel);

    @GET("devices/firmware-status/{imei}/{progress}/{status}")
    Single<ResponseBody> updateDownloadStatus(@Path("imei") String imei,@Path("progress") int progress, @Path("status") String status);

    @POST("login")
    Single<Response<ResponseBody>> adminLogin(@Body LogInModel logInModel);
}
