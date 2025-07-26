package com.tagmarshal.golf.service.firebase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.CoordinateModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.IdsModel;
import com.tagmarshal.golf.rest.model.LatLngModel;
import com.tagmarshal.golf.rest.model.PolygonModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.mostmediasdk.MostMediaSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FirebaseModel implements FirebaseContract.Model {
    private final Gson gson = new GsonBuilder().create();

    @Override
    public Single<Response<ResponseBody>> sendRefreshToken(Map<String, String> token) {
        return GolfAPI.getGolfCourseApi().sendFirebaseRefreshToken(token);
    }

    @Override
    public Single<Response<ResponseBody>> reregister(String imei, String type, String build) {
        return GolfAPI.getGolfCourseApi().registerDeviceUrl("https://" + PreferenceManager.getInstance().getCurrentCourseModel().getCourseUrl() + "/app/registerdevice/" + imei + "/type/" + type + "/" + build + "/");
    }

    @Override
    public Single<List<CourseModel>> getCourses() {
        return GolfAPI.getGolfApi().getCourses(TMUtil.getDeviceIMEI())
                .map(responseBodyResponse -> {
                    List<CourseModel> courseList = new ArrayList<>();
                    JsonReader reader = gson.newJsonReader(responseBodyResponse.body().charStream());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginArray();
                        CourseModel course = new CourseModel();
                        course.setCourseName(reader.nextString());
                        course.setCourseUrl(reader.nextString());
                        if (reader.peek() != JsonToken.NULL && reader.peek() != JsonToken.END_ARRAY) {
                            reader.beginObject();
                            PolygonModel polygon = new PolygonModel();
                            while (reader.hasNext()) {
                                String name = reader.nextName();
                                if (name.equals("type")) {
                                    polygon.setType(reader.nextString());
                                } else if (name.equals("coordinates")) {
                                    CoordinateModel coordinateModel = new CoordinateModel();
                                    List<LatLngModel> latLngList = new ArrayList<>();
                                    reader.beginArray();
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        reader.beginArray();
                                        LatLngModel latLng = new LatLngModel();
                                        latLng.setLon(reader.nextDouble());
                                        latLng.setLat(reader.nextDouble());
                                        reader.endArray();
                                        latLngList.add(latLng);
                                    }
                                    reader.endArray();
                                    reader.endArray();
                                    coordinateModel.setLatLng(latLngList);
                                    polygon.setCoordinate(coordinateModel);
                                }
                            }

                            reader.endObject();
                            course.setPolygon(polygon);

                        } else if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                        }
                        courseList.add(course);
                        reader.endArray();

                    }
                    reader.endArray();
                    return courseList;
                });
    }

    @Override
    public Single<ResponseBody> sendLogsToSupport(SupportLogModel supportLogs) {
        return GolfAPI.getGolfApi().sendLogsToSupport(supportLogs);
    }

    @Override
    public Observable<List<AdvertisementModel>> getAdvertisements() {
        // Use MostMedia SDK if configured, otherwise use original API
        if (MostMediaSDK.isConfigured()) {
            return MostMediaSDK.getAdvertisements();
        } else {
            return GolfAPI.getGolfCourseApi().getAdvertisements();
        }
    }

    @Override
    public Single<List<RestGeoFenceModel>> getGeoFences(String deviceIMEI) {
        return GolfAPI.getGolfCourseApi().getGeoFences(deviceIMEI);
    }

    @Override
    public Single<List<RestHoleModel>> getHoles() {
        return GolfAPI.getGolfCourseApi().getAllHoles();
    }

    @Override
    public Single<List<RestGeoFenceModel>> getGeofenceById(ArrayList<String> listItems) {
        IdsModel idsModel = new IdsModel(listItems);
        return GolfAPI.getGolfCourseApi().getGeoFenceById(idsModel);
    }

    private Observable<retrofit2.Response<ResponseBody>> getImageResponse(String url) {
        return GolfAPI.getGolfCourseApi().getAdvertisementImages(url);
    }
}
