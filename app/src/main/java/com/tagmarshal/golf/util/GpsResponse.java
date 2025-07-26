package com.tagmarshal.golf.util;
import android.location.Location;

import java.time.LocalDateTime;

public class GpsResponse {

    public boolean ignore = true; //default

    public LocalDateTime timestamp;
    public Location location; //Location including Lat, Lon, Bearing, Speed, Altitude
    public String msg; //Information
    public double hdop; //HDOP (used for validation)`
    public double vdop; //VDOP (used for validation)
    public boolean inGeofence = false; //True whenever inside a Geofence
    public boolean enteredGeofence = false; //Will trigger once at the moment of entering into a Geofence
    public boolean leftGeofence = false; //Being worked on - will follow
    public boolean audibleAlert = false; 
    public boolean notifyOperator = true; //To show dialog or not
    public String geofenceId = ""; //RestrictedZoneID
    public int satcount; //Satellite count (used for validation)
    public int fixType; //Fix Typed (used for validation)
}