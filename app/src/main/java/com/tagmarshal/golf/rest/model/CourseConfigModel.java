package com.tagmarshal.golf.rest.model;

import java.util.List;

public class CourseConfigModel {


    private final List<RestHoleModel> holes;
    private final List<RestGeoZoneModel> geoZones;
    private final List<RestGeoFenceModel> geoFence;
    private final List<PointOfInterest> pointsOfInterests;
    private final RestBoundsModel mapConfig;
    private CourseModel courseModel = null;

    public CourseConfigModel() {
        holes = null;
        geoZones = null;
        geoFence = null;
        pointsOfInterests = null;
        mapConfig = null;
    }


    public CourseConfigModel(List<RestGeoZoneModel> restGeoZoneModels,
                             List<RestGeoFenceModel> restGeoFenceModels,
                             List<RestHoleModel> restHoleModels,
                             RestBoundsModel mapConfig,
                             List<PointOfInterest> pointOfInterests) {
        this.geoZones = restGeoZoneModels;
        this.geoFence = restGeoFenceModels;
        this.holes = restHoleModels;
        this.mapConfig = mapConfig;
        this.pointsOfInterests = pointOfInterests.isEmpty() ? null : pointOfInterests;
    }

    public CourseConfigModel(List<RestGeoZoneModel> restGeoZoneModels,
                             List<RestGeoFenceModel> restGeoFenceModels,
                             List<RestHoleModel> restHoleModels,
                             RestBoundsModel mapConfig,
                             List<PointOfInterest> pointOfInterests,
                             CourseModel courseModel
    ) {
        this.geoZones = restGeoZoneModels;
        this.geoFence = restGeoFenceModels;
        this.holes = restHoleModels;
        this.mapConfig = mapConfig;
        this.pointsOfInterests = pointOfInterests.isEmpty() ? null : pointOfInterests;
        this.courseModel = courseModel;
    }

    public CourseConfigModel(List<RestGeoZoneModel> restGeoZoneModels,
                             List<RestGeoFenceModel> restGeoFenceModels,
                             List<RestHoleModel> restHoleModels,
                             RestBoundsModel mapConfig,
                             CourseModel courseModel
    ) {
        this.geoZones = restGeoZoneModels;
        this.geoFence = restGeoFenceModels;
        this.holes = restHoleModels;
        this.mapConfig = mapConfig;
        this.courseModel = courseModel;
        this.pointsOfInterests = null;
    }

    public CourseModel getCourseModel() {
        return courseModel;
    }

    public List<RestHoleModel> getHoles() {
        return holes;
    }

    public List<RestGeoZoneModel> getGeoZones() {
        return geoZones;
    }

    public List<RestGeoFenceModel> getGeoFence() {
        return geoFence;
    }

    public List<PointOfInterest> getPointsOfInterests() {
        return pointsOfInterests;
    }

    public RestBoundsModel getMapConfig() {
        return mapConfig;
    }


}
