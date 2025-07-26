package com.tagmarshal.golf.rest;

import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.CompletedFields;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.IdsModel;
import com.tagmarshal.golf.rest.model.MenuGroup;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestGolfGeniusModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.RestRatingModel;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.rest.model.SaveScoreModel;
import com.tagmarshal.golf.rest.model.SendOrderModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface GolfAPICourseEndpoint {

    @GET("device-info/{type}/{build}")
    Single<RestDeviceConfigModel> registerDevice(@Path("type") String path,
                                                 @Path("build") String build);

    @GET("teetimes")
    Single<List<RestTeeTimesModel>> getTeeTimes();

    @GET("messages")
    Single<List<RestAlertModel>> getAlerts();

    @PATCH("messages/{id}/status/acknowledged")
    Single<ResponseBody> sendAcknowledgeAlerts(@Path("id") String alertID);

    @GET("advertisements")
    Observable<List<AdvertisementModel>> getAdvertisements();

    @POST("fixes")
    Single<ResponseBody> sendLocationFix(@Body FixModel fixModel);

    @POST("fixes/cached")
    Single<ResponseBody> sendFailedFixes(@Body ArrayList<FixModel> fixes);

    @GET("menu")
    Observable<List<MenuGroup>> getFood();

    @GET("fnb/kitchen-hours")
    Observable<List<ActiveTimeDaysItemModel>> getKitchenHours();

    @POST("fnb/createOrder")
    Observable<ResponseBody> sendOrder(@Body SendOrderModel sendOrderModel);

    @POST("data-entry")
    Observable<ResponseBody> submitDataEntry(@Body List<CompletedFields> completedFields);

    @GET("rounds/info")
    Single<RestInRoundModel> getDeviceInfo();

    @GET("rounds/info")
    Observable<RestInRoundModel> getRepeatDeviceInfo();

    @GET("rounds/{roundId}/golf-genius")
    Single<RestGolfGeniusModel> getGolfGeniusInfo(@Path("roundId") String roundId);

    @POST("rounds/{roundId}/scores")
    Single<ResponseBody> insertScore(@Body SaveScoreModel saveScoreModel, @Path("roundId") String roundId);

    @POST("rounds/{roundId}/rate")
    Single<retrofit2.Response<ResponseBody>> rateRound(@Body RestRatingModel ratingModel, @Path("roundId") String roundId);

    @PATCH("token")
    Single<Response<ResponseBody>> sendFirebaseRefreshToken(@Body Map<String, String> token);

    @PATCH("accept-disclaimer")
    Single<ResponseBody> acceptDisclaimer();

    @POST("messages")
    Single<Response<ResponseBody>> sendMessage(@Body Map<String, String> body);

    @POST("geofences")
    Single<List<RestGeoFenceModel>> getGeoFenceById(@Body IdsModel idsModel);

    @DELETE("rounds")
    Single<Response<ResponseBody>> closeRound();

    @PATCH("teetimes/{id}/{action}")
    Single<retrofit2.Response<ResponseBody>> confirmOrRemoveGroup(@Path("id") String id,
                                                                  @Path("action") String action);

    /**
     * old endpoints
     **/

    @GET("app/getholeinfo/{hole}/")
    Single<List<RestHoleModel>> getHoleInfo(@Path("hole") int hole);

    @GET("app/getallholeinfo/")
    Single<List<RestHoleModel>> getAllHoles();

    @GET("app/getallholeinfo/")
    Observable<List<RestHoleModel>> getAllHolesObservable();

    @GET("app/courseconfig/{imei}/")
    Single<List<RestBoundsModel>> getMapBounds(@Path("imei") String imei);

    @GET("app/getholedistance/{holenumber}/")
    Single<List<RestHoleDistanceModel>> getHoleDistanceFromTee(@Path("holenumber") int hole);

    @GET
    Single<Response<ResponseBody>> registerDeviceUrl(@Url String url);

    @GET("app/getcontactinfo/")
    Single<retrofit2.Response<ResponseBody>> getContactNumber();

    @GET("app/insertrating/device/{imei}/position/{position}/rating/{rating}/comment/{comment}/")
    Single<retrofit2.Response<ResponseBody>> rateRound(@Path("imei") String imei,
                                                       @Path("position") String position,
                                                       @Path("rating") int rating,
                                                       @Path("comment") String comment);

    @GET("app/getgeofences/imei/{imei}/")
    Single<List<RestGeoFenceModel>> getGeoFences(@Path("imei") String imei);

    @GET("app/sendbadround/{device_id}/")
    Single<retrofit2.Response<ResponseBody>> sendBadRating(@Path("device_id") String imei);

    @GET("/app/calldrinks/{device_id}/")
    Single<retrofit2.Response<ResponseBody>> callDrinksCart(@Path("device_id") String imei);

    @GET("app/getzones/")
    Single<List<RestGeoZoneModel>> getGeoZones();

    @GET("app/pointsofinterest/")
    Observable<List<PointOfInterest>> getPointsOfInterest();

    @GET
    Observable<List<RestGeoZoneModel>> getGeoZonesByUrl(@Url String url);

    @GET
    Observable<List<RestGeoFenceModel>> getGeoFencesByUrl(@Url String courseUrl);

    @GET
    Observable<List<RestHoleModel>> getCourseHolesByUrl(@Url String courseUrl);

    @GET
    Observable<List<RestBoundsModel>> getCourseConfig(@Url String courseUrl);

    @GET
    Observable<List<PointOfInterest>> getPointsOfInterestByUrl(@Url String courseUrl);

    @GET
    Observable<Response<ResponseBody>> getAdvertisementImages(@Url String courseUrl);
}
