package com.tagmarshal.golf.util;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;;

public class GeofenceHelper {

    //State variables
    static double lastVDOP = 0;
    static double lastHDOP = 0;
    static int lastFixType = 0;
    static int lastSatCount = 0;
    static float lastBearing = 0;
    static float lastSpeed = 0;

    //Geofence public state
    public static boolean currentlyInGeofence = false;
    public static String currentGeofenceId = "";
    static CircularBuffer hdopBuffer = null;
    static CircularBuffer snrBuffer = null;

    static Location previousLocation = null;
    static Location currentLocation = null;

    public static List<GeofenceGeometry> geofenceList = new ArrayList<>();
    public static Boolean allowGeofenceReload = true;
    public static Boolean ignoreAllGeofences = false;

    //For testing
    static public boolean mockingLocation = false;
    static private boolean mockingLocationInReverse = false;
    static private int mockLocationSubIndex = 1;
    static private int mockLoopCount = 0;

    //Noise|multi-path check
    static GPGSVData gpgsvData = new GPGSVData();
    static GPGSAData gpgsaData = new GPGSAData();
    public static double averageTop3SNR = -1;           
    static boolean snrValuesReady = false; //Set to true when an initial average SNR value is available

    //Default thresholds for an allowed GPS fix
    static double fixHdopThreshold = 2.0;  //1.2
    static double fixSnrThreshold = 15; //17.5

    //Default thresholds for an allowed Geofence fix
    static double geofenceHdopThreshold = 1.0; //0.8
    static double geofenceSnrThreshold = 15.0; //21

    public static GpsResponse ProcessGpsData(String nmeaString, long timestamp) {
        if (isNullOrEmpty(nmeaString)) {
            return new GpsResponse();
        }

        if (hdopBuffer == null) {
            //Insert 3xHDOP values slightly below fix threshold to allow faster fix initialization
            hdopBuffer = new CircularBuffer(3);
            for (int i = 0; i < 3; i++) hdopBuffer.insert(fixHdopThreshold-0.01);
        }

        if (snrBuffer == null)
        {
            //Insert 12xSNR values slightly above fix threshold to allow faster snr initialization
            snrBuffer = new CircularBuffer(12);
            for (int i = 0; i < 12; i++) snrBuffer.insert(fixSnrThreshold+0.01);
        }
        
        Boolean isGpxFix = false;

        GpsResponse result = new GpsResponse();

        String[] nmeaParts = nmeaString.split(",");

        if (nmeaParts[0].equalsIgnoreCase("$GPGSV") || nmeaParts[0].equalsIgnoreCase("$GNGSV"))
        {
            parseGPGSV(gpgsvData, nmeaString);
        }

        //Update noise and multi-path check data
        if (nmeaParts[0].equalsIgnoreCase("$GPGSA") || nmeaParts[0].equalsIgnoreCase("$GNGSA"))
        {
            gpgsaData = parseGPGSA(nmeaString);
            if (gpgsvData.currentSentence == gpgsvData.totalSentences) {
                snrBuffer.insert(calculateAverageSNR(gpgsvData, gpgsaData));
                averageTop3SNR = snrBuffer.topThreeAverage();
                snrValuesReady = true;
                //Log.d("GEOGT", "Average SNR: " + averageTop3SNR + " Buffer: " + snrBuffer.getBufferContent());
            } else {
                //GPGSV data required
                //Log.d("GEOGT", "More GPGS required");
            }
        }

        nmeaParts[0] = "$GN" + nmeaParts[0].substring(3); //Standardise NMEA Header for further parsing

        if (nmeaParts[0].equalsIgnoreCase("$GNGSA")) {

            if (nmeaParts.length > 16 && !isNullOrEmpty(nmeaParts[16])) {
                lastHDOP = Double.parseDouble(nmeaParts[16]);
                result.hdop = lastHDOP;
            }

            if (nmeaParts.length > 17 && !isNullOrEmpty(nmeaParts[17]) && !nmeaParts[17].contains("*")) {
                lastVDOP = Double.parseDouble(nmeaParts[17].split("\\*")[0]);
                result.vdop = lastVDOP;
            }

            if (nmeaParts.length > 2 && !isNullOrEmpty(nmeaParts[2])) {  //Overall Position Dilution of Precision
                lastFixType = Integer.valueOf(nmeaParts[2]);
                result.fixType = lastFixType;
            }

            return result; //ignore = true
        }

        if (nmeaParts[0].equalsIgnoreCase("$GNVTG")) {

            if (nmeaParts.length > 8 && !isNullOrEmpty(nmeaParts[1])) {
                lastBearing = Float.parseFloat(nmeaParts[1]);
            }

            if (nmeaParts.length > 7 && !isNullOrEmpty(nmeaParts[7])) {
                lastSpeed = Float.parseFloat(nmeaParts[7]);
            }

            return result; //ignore = true
        }

        if (nmeaParts[0].equalsIgnoreCase("$GNGGA")) {

            if (currentLocation != null) {
                previousLocation = new Location("");
                previousLocation.setLatitude(currentLocation.getLatitude());
                previousLocation.setLongitude(currentLocation.getLongitude());
            }
            
            isGpxFix = true; //Used for logging

            currentLocation = new Location("");
            currentlyInGeofence = false;
            result.inGeofence = false;
            result.enteredGeofence = false;
            result.leftGeofence = false;
            result.msg = "";

            String latitude = nmeaParts[2];
            String latDirection = nmeaParts[3];
            double lat = parseCoordinate(latitude, latDirection);
            currentLocation.setLatitude(lat);

            String longitude = nmeaParts[4];
            String lonDirection = nmeaParts[5];
            double lon = parseCoordinate(longitude, lonDirection);
            currentLocation.setLongitude(lon);

            if (nmeaParts.length > 11 && !isNullOrEmpty(nmeaParts[11])) {
                currentLocation.setAltitude(Double.parseDouble(nmeaParts[11]));
            }

            if (nmeaParts.length > 7 && !isNullOrEmpty(nmeaParts[7])) {
                lastSatCount = Integer.parseInt(nmeaParts[7]);
            }

            if (nmeaParts.length > 8 && !isNullOrEmpty(nmeaParts[8])) {
                lastHDOP = Double.parseDouble(nmeaParts[8]);
            }

            result.location = currentLocation;

            if (lastHDOP == 0 || lastHDOP == 99) {
                lastHDOP = 99;
                hdopBuffer.insert(2.8);
            } else {
                hdopBuffer.insert(lastHDOP);
            }
            //result.hdop = lastHDOP;
            result.hdop = hdopBuffer.average();
            result.vdop = lastVDOP;
            result.satcount = lastSatCount;
            result.fixType = lastFixType;
            currentLocation.setBearing(lastBearing);
            currentLocation.setSpeed(lastSpeed);
            currentLocation.setAccuracy((float) result.hdop);

            result = FixValidation(result); //Validate fix quality
            if (result.ignore == false) {

                if (mockingLocation) result = MockLocation(result);

                //Check for Geofence interaction and validate
                Point p = new Point(currentLocation.getLatitude(), currentLocation.getLongitude());
                for (int i = 0; i < geofenceList.size(); i++) {
                    GeofenceGeometry geofence = geofenceList.get(i);
                    if (isPointInPolygon(p, geofence.getPoints())) {
                        result.msg = "INSIDE GEOFENCE";
                        result.inGeofence = true;
                        result.audibleAlert = geofence.getAudible();
                        result.notifyOperator = geofence.getNotify();
                        result.geofenceId = geofence.getID();
                        result = GeofenceValidation(result); //Validate Geofence interaction (HDOP, VDOP, FixType, Satellite Count, SNR checks) - set inGeofence flag to false if not valid
                        currentlyInGeofence = result.inGeofence;
                        currentGeofenceId = geofence.getID();
                        if (!result.ignore && previousLocation != null) {
                            //Check if this is when it entered the geofence
                            Point prev = new Point(previousLocation.getLatitude(), previousLocation.getLongitude());
                            Point current = new Point(currentLocation.getLatitude(), currentLocation.getLongitude());

                            result.enteredGeofence = hasCrossedLine(geofence.getPoints(), prev, current);
                            if (result.enteredGeofence) {
                                result.msg = "ENTERED GEOFENCE";
                            }
                        }

                        //if geofence is not active or visible, disable flags
                        if (geofence.getActive() == false) {
                            result.inGeofence = false;
                            result.enteredGeofence = false;
                            result.leftGeofence = false;
                            currentlyInGeofence = false;
                            currentGeofenceId = "";
                        }
                        break;
                    };
                }
            }
        }

        if (isGpxFix) {
            String isIgnore = result.ignore ? "FAILED" : "VALID";
            String formattedLastHdop = String.format("%.2f", GeofenceHelper.lastHDOP);
            String formattedLastSNR = String.format("%.2f", GeofenceHelper.averageTop3SNR);
            String formattedFixHDOP = String.format("%.2f", GeofenceHelper.fixHdopThreshold);
            String formattedFixSNR = String.format("%.2f", GeofenceHelper.fixSnrThreshold);
            String formattedGeoHdop = String.format("%.2f", GeofenceHelper.geofenceHdopThreshold);
            String formattedGeoSNR = String.format("%.2f", GeofenceHelper.geofenceSnrThreshold);
            
            CartControlHelper.sendLogMessageToCartControlDebugConsole("2W -> Current: "+formattedLastHdop+" / "+ formattedLastSNR+" ("+isIgnore+")   |  Thresholds Fix: "+formattedFixHDOP+"/"+formattedFixSNR+"  Geo: "+formattedGeoHdop+"/"+formattedGeoSNR);
        }
        
        return result;
    }

    public static void setThresholds(double fixHdop, double fixSnr, double geoHdop, double geoSnr) {
        //Log.d("GEOGT", "Setting thresholds: " + fixHdop + " " + fixSnr + " " + geoHdop + " " + geoSnr);
        if (fixHdop > 0) fixHdopThreshold = fixHdop;
        if (fixSnr > 0) fixSnrThreshold = fixSnr;
        if (geoHdop > 0) geofenceHdopThreshold = geoHdop;
        if (geoSnr > 0) geofenceSnrThreshold = geoSnr;
    }

    private static GpsResponse FixValidation(GpsResponse data) {
        data.ignore = false;
        if (data.hdop > fixHdopThreshold || data.vdop > 2.8) data.ignore = true;
        if (snrValuesReady && averageTop3SNR < fixSnrThreshold) data.ignore = true;
        if (data.fixType != 3) data.ignore = true;
        return data;
    }

    private static GpsResponse GeofenceValidation(GpsResponse data) {
        data.inGeofence = true;
        if (data.hdop > geofenceHdopThreshold || data.vdop > 2.8) data.inGeofence = false;
        if (data.fixType != 3) data.inGeofence = false;
        if (data.satcount < 6) data.inGeofence = false;
        if (averageTop3SNR < geofenceSnrThreshold) data.inGeofence = false;
        return data;
    }

    private static GpsResponse MockLocation(GpsResponse data) {
        if (mockingLocation) {
            
            if (mockLocationSubIndex > 60) { //76 for Westlake
                mockingLocationInReverse = true; //30 covers both zones // 55
            }
            if (mockingLocationInReverse && mockLocationSubIndex < 1) {
                mockingLocationInReverse = false;
                mockLoopCount += 1;
            }

            if (mockingLocationInReverse) {
                mockLocationSubIndex -= 1; 
            } else {
                mockLocationSubIndex += 1;
            }

            data.location.setLatitude(-33.92194);  //Town
            data.location.setLongitude(18.41842);
            
            //data.location.setLatitude(-34.08297);  //1st Tee Westlake
            //data.location.setLongitude(18.44441);

//            data.location.setLatitude(28.33620);  //1st Fairway Rockledge
//            data.location.setLongitude(-80.74401);

            //data.location.setLatitude(36.56941);  //1st Fairway Pebble Beach
            //data.location.setLongitude(-121.94950);

            //data.location.setLatitude(-34.01391);  //1st Hole Royal Cape
            //data.location.setLongitude(18.48598);
            
            
            //data.location.setLatitude(-34.08307); //1st Tee
            //data.location.setLongitude(18.44455);

//            data.location.setLatitude(data.location.getLatitude() - (((double) mockLocationSubIndex / 180000) * 0.5)*8);
//            data.location.setLongitude(data.location.getLongitude() + (((double) mockLocationSubIndex / 70000) * 0.5)*8);

            data.location.setLatitude(data.location.getLatitude() - (((double) mockLocationSubIndex / 180000) * 0.5)*8);
            data.location.setLongitude(data.location.getLongitude() + (((double) mockLocationSubIndex / 70000) * 0.5)*8);
            
            //Log.d("GEOGT", "Mocking Location: " + data.location.getLatitude() + " " + data.location.getLongitude()  + "  SubIndex:" + mockLocationSubIndex+ "  LoopCount:" + mockLoopCount);
        }
        return data;
    }

    private static double parseCoordinate(String coordinate, String direction) {
        if (coordinate == null || coordinate.isEmpty() || direction == null || direction.isEmpty()) {
            return 0.0;
        }

        double decimalDegrees = 0.0;

        try {
            double raw = Double.parseDouble(coordinate);
            int degrees = (int) (raw / 100);
            double minutes = raw - (degrees * 100);

            decimalDegrees = degrees + (minutes / 60.0);

            if (direction.equalsIgnoreCase("S") || direction.equalsIgnoreCase("W")) {
                decimalDegrees *= -1;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return decimalDegrees;
    }

    public static boolean isPointInPolygon(Point point, List<Point> polygon) {
        int n = polygon.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygon.get(i);
            Point pj = polygon.get(j);

            if ((pi.getLatitude() > point.getLatitude()) != (pj.getLatitude() > point.getLatitude()) &&
                    (point.getLongitude() < (pj.getLongitude() - pi.getLongitude()) * (point.getLatitude() - pi.getLatitude()) / (pj.getLatitude() - pi.getLatitude()) + pi.getLongitude())) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static boolean hasCrossedLine(List<Point> points, Point prev, Point current) {
        boolean foundCrossing = false;
        int n = points.size();
        for (int a = 0, b = n - 1; a < n; b = a++) {
            Point pa = points.get(a);
            Point pb = points.get(b);

            if (checkForCrossing(pa, pb, prev, current)) foundCrossing = true;
        }
        return foundCrossing;
    }

    private static boolean checkForCrossing(Point lineStart, Point lineEnd, Point prev, Point current) {
        Point p0 = lineStart;
        Point p1 = lineEnd;
        Point p2 = prev;
        Point p3 = current;

        double crossProduct1 = (p1.getLatitude() - p0.getLatitude()) * (p2.getLongitude() - p0.getLongitude()) - (p1.getLongitude() - p0.getLongitude()) * (p2.getLatitude() - p0.getLatitude());
        double crossProduct2 = (p1.getLatitude() - p0.getLatitude()) * (p3.getLongitude() - p0.getLongitude()) - (p1.getLongitude() - p0.getLongitude()) * (p3.getLatitude() - p0.getLatitude());

        return (crossProduct1 * crossProduct2 < 0);
    }

    public static boolean isNullOrEmpty(String param) {
        return param == null || param.trim().length() == 0;
    }

    public static void parseGPGSV(GPGSVData gpgsvData, String sentence) {
        String[] parts = sentence.split(",");

        int totalSentences = Integer.parseInt(parts[1]);
        int currentSentence = Integer.parseInt(parts[2]);

        if (currentSentence == 1) {
            gpgsvData.clear();
        }

        gpgsvData.totalSentences = totalSentences;
        gpgsvData.currentSentence = currentSentence;

        for (int i = 4; i < parts.length - 1; i += 4) {
            String satelliteId = parts[i];
            String snrValue = parts[i + 3];

            double snr;
            try {
                snr = snrValue.isEmpty() ? 0 : Double.parseDouble(snrValue);
            } catch (NumberFormatException e) {
                //Invalid SNR format
                snr = 0;  // Set to 0 or ignore as needed
            }

            gpgsvData.addSatellite(new SatelliteInfo(satelliteId, snr));
        }
    }

    private static GPGSAData parseGPGSA(String sentence) {
        GPGSAData gpgsaData = new GPGSAData();
        String[] parts = sentence.split(",");

        for (int i = 3; i <= 14; i++) {
            if (!parts[i].isEmpty()) {
                gpgsaData.addSatellite(parts[i]);
            }
        }

        return gpgsaData;
    }

    private static double calculateAverageSNR(GPGSVData gpgsvData, GPGSAData gpgsaData) {
        double totalSNR = 0;
        int satelliteCount = 0;

        for (String satelliteId : gpgsaData.satellitesUsed) {
            for (SatelliteInfo satellite : gpgsvData.satellitesInfo) {
                if (satellite.satelliteId.equals(satelliteId)) {
                    totalSNR += satellite.snr;
                    satelliteCount++;
                    break;
                }
            }
        }

        //Log.d("GEOGT","Total SNR: " + totalSNR + " Satellite Count: " + satelliteCount);
        return (satelliteCount > 0) ? totalSNR / satelliteCount : 0;
    }

}

class Point {
    private double latitude;
    private double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

class CircularBuffer {
    private final double[] buffer;
    private int insertIndex;
    private int count;

    public CircularBuffer(int size) {
        buffer = new double[size];
        insertIndex = 0;
        count = 0;
    }

    // Insert a new double, replacing the oldest if buffer is full
    public void insert(double value) {
        buffer[insertIndex] = value;
        insertIndex = (insertIndex + 1) % buffer.length;
        if (count < buffer.length) {
            count++;
        }
    }

    // Calculate the average of the current values in the buffer
    public double average() {
        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += buffer[i];
        }
        return count == 0 ? 0 : sum / count;
    }

    public double topThreeAverage() {
        if (count == 0) {
            return 0;  // No values in buffer
        }

        // Create an array to store the top 3 largest values
        double[] topThree = new double[Math.min(3, count)];
        Arrays.fill(topThree, Double.NEGATIVE_INFINITY);

        // Find the top 3 largest values in the buffer
        for (int i = 0; i < count; i++) {
            double currentValue = buffer[i];

            // Insert current value in the correct position in the top 3 array
            for (int j = 0; j < topThree.length; j++) {
                if (currentValue > topThree[j]) {
                    // Shift elements to the right to make room for the new value
                    for (int k = topThree.length - 1; k > j; k--) {
                        topThree[k] = topThree[k - 1];
                    }
                    topThree[j] = currentValue;
                    break;
                }
            }
        }

        // Calculate the average of the top 3 values
        double sum = 0;
        for (double val : topThree) {
            sum += val;
        }

        return sum / topThree.length;
    }

    public String getBufferContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(buffer[i]);
            if (i < count - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}


// Class to hold satellite info from GPGSV
class SatelliteInfo {
    public String satelliteId;
    public double snr;

    public SatelliteInfo(String satelliteId, double snr) {
        this.satelliteId = satelliteId;
        this.snr = snr;
    }
}

class GPGSVData {
    public List<SatelliteInfo> satellitesInfo = new ArrayList<>();
    public int totalSentences = 0;  // Total number of sentences in this GPGSV cycle
    public int currentSentence = 0; // Current sentence number in the cycle

    public void addSatellite(SatelliteInfo satellite) {
        this.satellitesInfo.add(satellite);
    }

    public void clear() {
        this.satellitesInfo.clear();
        this.totalSentences = 0;
        this.currentSentence = 0;
    }
}

class GPGSAData {
    public List<String> satellitesUsed = new ArrayList<>();

    public void addSatellite(String satelliteId) {
        satellitesUsed.add(satelliteId);
    }

    public void clear() {
        satellitesUsed.clear();
    }
}