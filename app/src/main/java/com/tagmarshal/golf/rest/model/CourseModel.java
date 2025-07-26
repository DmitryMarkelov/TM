package com.tagmarshal.golf.rest.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Objects;

public class CourseModel {

    private String courseName;

    private String courseUrl;

    private PolygonModel polygon;


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseUrl() {
        return courseUrl;
    }

    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }

    public PolygonModel getPolygon() {
        return polygon;
    }

    public PolygonOptions getPolygonOptions() {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        for (int i = 0; i < getPolygon().getCoordinate().getLatLng().size(); i++) {
            coordinates.add(new LatLng(getPolygon().getCoordinate().getLatLng().get(i).getLat(), getPolygon().getCoordinate().getLatLng().get(i).getLon()));
        }

        return new PolygonOptions()
                .addAll(coordinates)
                .clickable(false)
                .visible(false);
    }

    public void setPolygon(PolygonModel polygon) {
        this.polygon = polygon;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof CourseModel)) {
            return false;
        }
        CourseModel course = (CourseModel) o;
        return courseName.equals(course.courseName) &&
                Objects.equals(courseName, course.courseName) &&
                Objects.equals(courseUrl, course.courseUrl) &&
                Objects.equals(polygon, course.polygon)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseName, courseUrl, polygon);
    }

    public String getCourseName(CourseConfigModel courseConfigModel) {
        return courseName;
    }
}
