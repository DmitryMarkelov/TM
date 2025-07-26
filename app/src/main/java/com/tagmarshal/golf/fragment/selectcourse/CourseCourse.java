package com.tagmarshal.golf.fragment.selectcourse;

import com.tagmarshal.golf.rest.model.FoodAndDrinkModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;

import java.util.List;

class CourseCourse {
    public final FoodAndDrinkModel t2;
    public final List<RestGeoZoneModel> t1;

    public CourseCourse(List<RestGeoZoneModel> t1, FoodAndDrinkModel t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
}
