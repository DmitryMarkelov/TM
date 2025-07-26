package com.tagmarshal.golf.fragment.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.glide.GlideApp;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.CoordinateModel;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.FixModel;
import com.tagmarshal.golf.rest.model.LatLngModel;
import com.tagmarshal.golf.rest.model.PointOfInterest;
import com.tagmarshal.golf.rest.model.PolygonModel;
import com.tagmarshal.golf.rest.model.RestBoundsModel;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestHoleModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.rest.model.SupportLogModel;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FragmentMapModel implements FragmentMapContract.Model {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public Observable<Long> startNotificationTimer() {
        return Observable.interval(0,2,TimeUnit.SECONDS);
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
    public Single<List<RestHoleModel>> getHoleInfo(int hole) {
        return GolfAPI.getGolfCourseApi().getHoleInfo(hole);
    }

    @Override
    public Single<List<RestBoundsModel>> getMapBounds(String imei) {
        return GolfAPI.getGolfCourseApi().getMapBounds(imei);
    }

    @Override
    public Single<List<RestTeeTimesModel>> getTeeTimes() {
        return GolfAPI.getGolfCourseApi().getTeeTimes();
    }

    @Override
    public Single<RestInRoundModel> getRoundInfo() {
        return GolfAPI.getGolfCourseApi().getDeviceInfo();
    }

    @Override
    public Single<List<RestHoleDistanceModel>> getHoleDistanceInfo(int hole) {
        return GolfAPI.getGolfCourseApi().getHoleDistanceFromTee(hole);
    }

    @Override
    public Single<RestHoleDistanceModel> getHoleDistanceFromPoint(final LatLng tapLocation,
                                                                  final RestHoleModel holeModel) {
        return Single.just(TMUtil.getRangeFinderFromTeeLocation(tapLocation, holeModel));
            }

    @Override
    public Single<RestHoleDistanceModel> getHoleDistanceFromPointAndMyLocation(final LatLng myLocation,
                                                                               final LatLng tapLocation,
                                                                               final RestHoleModel holeModel) {
        return Single.just(TMUtil.getRangeFinderFromMyLocation(myLocation, tapLocation, holeModel));
    }

    @Override
    public Single<List<RestGeoFenceModel>> getGeoFences(String imei) {
        return GolfAPI.getGolfCourseApi().getGeoFences(imei);
    }

    @Override
    public Single<ResponseBody> gotAlert(String alertId) {
        return GolfAPI.getGolfCourseApi().sendAcknowledgeAlerts(alertId);
    }

    @Override
    public Observable<PictureDrawable> getPictureDrawable(String url, Context context){
        return Observable.create(emitter -> emitter.onNext(GlideApp.with(context)
                .as(PictureDrawable.class)
                .error(R.drawable.ic_arrow_left)
                .transition(withCrossFade())
                .load(url)
                .into(0,0)
                .get()));
    }

    @Override
    public Observable<Drawable> getImage(String image, Context context) {
        return Observable.create(emitter -> emitter.onNext(GlideApp.with(context)
                       .load(image)
                       .into(2000,2000)
                       .get()
        ));
    }

    @Override
    public Observable<List<AdvertisementModel>> getAdvertisements() {
        return GolfAPI.getGolfCourseApi().getAdvertisements();

    }

    @Override
    public Single<List<RestGeoZoneModel>> getGeoZones() {
        return GolfAPI.getGolfCourseApi().getGeoZones();
    }

    @Override
    public Observable<List<RestHoleModel>> getAllHoles() {
        return GolfAPI.getGolfCourseApi().getAllHolesObservable();
    }

    @Override
    public Observable<Long> startCheckGeoFences() {
        return Observable.interval(0, 1, TimeUnit.MINUTES);
    }

    @Override
    public Observable<Long> startCountDownTimerToStartGame() {
        return Observable.interval(0, 15, TimeUnit.SECONDS);
    }

    @Override
    public Single<ResponseBody> sendLocationFix(FixModel fix) {
        return GolfAPI.getGolfCourseApi().sendLocationFix(fix);
    }

    @Override
    public Observable<List<PointOfInterest>> getPointsOfInterest() {
        return GolfAPI.getGolfCourseApi().getPointsOfInterest();
    }

    @Override
    public Single<ResponseBody> sendLogsToSupport(SupportLogModel supportLogs) {
        return GolfAPI.getGolfApi().sendRawLogsToSupport(supportLogs);
    }
}
