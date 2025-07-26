package com.tagmarshal.golf.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.android.datatransport.runtime.dagger.multibindings.ElementsIntoSet;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.tagmarshal.golf.activity.DebugConsoleActivity;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestGeoFenceModel;
import com.tagmarshal.golf.rest.model.SendOrderModel;

import org.apache.commons.collections4.functors.FalsePredicate;

import timber.log.Timber;

public class CartControlHelper {

    public static BluetoothService bluetoothService = new BluetoothService();
    public static Boolean hasBluetoothPairing = false;
    public static Boolean inCartControlGeofence = false;
    public static Boolean isCartControlThreadActive = false;
    public static Boolean isCartControlLoggingActive = false; //if listed in pairings
    public static Boolean hasFirmwareUpdateBeenRequested = false;
    public static Boolean hasCartControlDataClearBeenRequested = false;
    public static String breachedCartGeofenceID = "";
    public static boolean breachCartGeofenceIsAudible = false;
    public static List<GeofenceGeometry> geofenceList = new ArrayList<>();
    public static Queue<GeofenceGeometry> geofenceSendQueue = new LinkedList<>();
    public static String cartControlVersion = "";
    private static MainActivity mainActivity;
    private static int cartControlHeartbeatIntervalCount = 0;
    private static int cartControlHeartbeatTriggerAt = 120;
    private static boolean shouldNotifyOperator = false;

    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public static boolean connectToCartControl() throws IOException {
        if (bluetoothService.connect()) {
            while (true) {

                hasBluetoothPairing = true;

                cartControlHeartbeatIntervalCount = cartControlHeartbeatIntervalCount + 1;
                if (cartControlHeartbeatIntervalCount >= cartControlHeartbeatTriggerAt) {
                    bluetoothService.sendMessage("$hb#");
                    cartControlHeartbeatIntervalCount = 0;
                    Log.d("GEOGT", "CartControl Heartbeat Sent " + cartControlHeartbeatIntervalCount + ":" + cartControlHeartbeatTriggerAt);
                }

                String messageIn = bluetoothService.receiveMessage();

                if (hasFirmwareUpdateBeenRequested) {
                    sendLogMessageToCartControlDebugConsole("EVENT -> FIRMWARE CHECK IS PENDING");
                }

                if (messageIn != null) {
                    sendLogMessageToCartControlDebugConsole("IN -> " + messageIn);
                    Log.d("GEOGT", "IN -> " + messageIn);

                    if (messageIn.contains("$geo,1")) {
                        Log.d("GEOGT", messageIn);
                        String[] parts = messageIn.split(",");
                        String breachedId = "";
                        shouldNotifyOperator = false; //flag to determine if dialog is shown
                        double breachedLat;
                        double breachedLon;
                        double breachedHdop;
                        double breachedSnr;
                        if (parts.length >= 3) {
                            breachedId = parts[2];
                        }
                        if (parts.length >= 4) {
                            breachedLat = Double.parseDouble(parts[3]);
                        } else {
                            breachedLat = 0;
                        }
                        if (parts.length >= 5) {
                            breachedLon = Double.parseDouble(parts[4]);
                        } else {
                            breachedLon = 0;
                        }
                        if (parts.length >= 6) {
                            breachedHdop = Double.parseDouble(parts[5]);
                        } else {
                            breachedHdop = 0;
                        }
                        if (parts.length >= 7) {
                            breachedSnr = Double.parseDouble(parts[6].replace("#", ""));
                        } else {
                            breachedSnr = 0;
                        }
                        if (breachedId != "") { //found geofence breached, notify user
                            for (GeofenceGeometry geofence : geofenceList) {
                                if (geofence.getID().equals(breachedId)) {
                                    shouldNotifyOperator = geofence.getNotify();
                                    breachedCartGeofenceID = breachedId;
                                    breachCartGeofenceIsAudible = geofence.getAudible();
                                    break;
                                }
                            }
                            Log.d("GEOGT", "CartControl Geofence Breached: " + breachedId + " BreachedCartGeofenceID: " + breachedCartGeofenceID + "   Ignore: " + GeofenceHelper.ignoreAllGeofences + "   Audible: " + breachCartGeofenceIsAudible);
                            if (FragmentMap.instance != null && breachedCartGeofenceID != "") {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    try {
                                        inCartControlGeofence = true;
                                        if (!GeofenceHelper.ignoreAllGeofences) {
                                            Log.d("GEOGT", "CartControl Geofence Detected - Showing Alert Dialog");
                                            sendLogMessageToCartControlDebugConsole("EVENT -> IN GEOFENCE (SHOWING DIALOG)");
                                            FragmentMap.instance.showCartControlGeofenceAlertDialog(breachedLat, breachedLon, breachedHdop, breachedSnr);
                                        } else {
                                            Log.d("GEOGT", "CartControl Geofence Detected - BUT NOT Showing Alert Dialog!!");
                                            sendLogMessageToCartControlDebugConsole("EVENT -> IN GEOFENCE (IGNORE GEOFENCES = TRUE)");
                                            bluetoothService.sendMessage("$clear#");
                                            breachCartGeofenceIsAudible = false;
                                            FragmentMap.instance.hideCartControlGeofenceAlertDialog();
                                        }
                                    } catch (Exception e) {
                                    }
                                });
                            }
                        }

                        sendGeofenceToCartControl();
                    } else if (messageIn.contains("$geo,0")) {
                        if (FragmentMap.instance != null) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                try {
                                    if (inCartControlGeofence) {
                                        inCartControlGeofence = false;
                                        breachedCartGeofenceID = "";
                                        breachCartGeofenceIsAudible = false;
                                        sendLogMessageToCartControlDebugConsole("EVENT -> EXIT GEOFENCE (HIDING DIALOG)");
                                        FragmentMap.instance.hideCartControlGeofenceAlertDialog();
                                    }
                                } catch (Exception e) {
                                }
                            });
                        }
                        sendGeofenceToCartControl();
                    }

                    if (messageIn.contains("$prd_ok")) {
                        //Reset and Resend Geofences to CartControl
                        String trimmedInput = messageIn.substring(1, messageIn.length() - 1);
                        String[] parts = trimmedInput.split(",");
                        if (parts.length == 2) {
                            cartControlVersion = parts[1];
                        }

                        bluetoothService.sendMessage("$clear#");
                        inCartControlGeofence = false;
                        breachedCartGeofenceID = "";
                        breachCartGeofenceIsAudible = false;
                        geofenceList.clear();
                        geofenceSendQueue.clear();
                        sendLogMessageToCartControlDebugConsole("EVENT -> CART CONNECTED / CLEAR GEOFENCE CACHE");
                        updateAppString();
                        configureCartControlGeofences();
                    }

                    if (messageIn.contains("$hb_ok")) {
                        Log.d("GEOGT", "CartControl Heartbeat response OK");
                        sendLogMessageToCartControlDebugConsole("EVENT -> HEARTBEAT RESPONSE OK");
                        sendLogMessageToCartControlDebugConsole("DATA -> HDOP:" + GeofenceHelper.geofenceHdopThreshold + " SNR:" + GeofenceHelper.geofenceSnrThreshold);

                        //Clear Cart Control hardware if there are no Geofences found
                        var geoFences = PreferenceManager.getInstance().getGeoFences();
                        boolean foundActiveCartControlGeofence = false;
                        for (RestGeoFenceModel geofence : geoFences) {
                            if (geofence.isCartControlActive() && geofence.isActive()) {
                                foundActiveCartControlGeofence = true;
                                break;
                            }
                        }

                        if (foundActiveCartControlGeofence == false || GeofenceHelper.ignoreAllGeofences) {
                            inCartControlGeofence = false;
                            breachedCartGeofenceID = "";
                            breachCartGeofenceIsAudible = false;
                            CartControlHelper.geofenceSendQueue.clear();
                            CartControlHelper.geofenceList.clear();
                            bluetoothService.sendMessage("$clear#");
                            if (GeofenceHelper.ignoreAllGeofences) {
                                sendLogMessageToCartControlDebugConsole("EVENT -> IGNORE GEOFENCES ENABLED (CLEARING DEVICE)");
                            } else {
                                sendLogMessageToCartControlDebugConsole("EVENT -> NO ACTIVE GEOFENCES FOUND (CLEARING DEVICE)");
                            }
                        }

                        //If there are Geofences to send, clear the existing list on the device and update the thresholds
//                            if (!geofenceSendQueue.isEmpty() && (geofenceSendQueue.size() == geofenceList.size())) {
//                                inCartControlGeofence = false;
//                                breachedCartGeofenceID = "";
//                                breachCartGeofenceIsAudible = false;
//                                bluetoothService.sendMessage("$clear#");
//                                bluetoothService.sendMessage("$gps,"+GeofenceHelper.geofenceHdopThreshold+","+GeofenceHelper.geofenceSnrThreshold+"#");
//                            }

//                            if (!geofenceSendQueue.isEmpty()) {
//                                inCartControlGeofence = false;
//                                breachedCartGeofenceID = "";
//                                breachCartGeofenceIsAudible = false;
//                                bluetoothService.sendMessage("$clear#");
//                                bluetoothService.sendMessage("$gps,"+GeofenceHelper.geofenceHdopThreshold+","+GeofenceHelper.geofenceSnrThreshold+"#");
//                            }

                        //Send the geofences to Cart Control if the SendQueue is not empty
//                            while (!geofenceSendQueue.isEmpty()) {
//                                Gson gson = new Gson();
//                                String json;
//                                GeofenceGeometry geofence = geofenceSendQueue.poll(); // Removes and retrieves the head of the queue
//                                try {
//                                    json= gson.toJson(geofence);
//                                    bluetoothService.sendMessage("$gfd,"+json+"#");
//                                    Thread.sleep(1500); //1350
//                                } catch (InterruptedException e) {
//                                }
//                                updateAppString();
//                            }

                        //Send one at a time

                        updateAppString();
                        if (hasFirmwareUpdateBeenRequested) {
                            hasFirmwareUpdateBeenRequested = false;
                            bluetoothService.sendMessage("$check_for_update#");
                            sendLogMessageToCartControlDebugConsole("EVENT -> FIRMWARE VERSION CHECK REQUESTED (CART CONTROL MAY NOT RESPOND FOR A FEW MINUTES AND THE APP WILL NEED TO BE RESTARTED)");
                        }
                    }
                }
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                }
            }
        } else {
            Log.d("GEOGT", "FAILED TO PAIR WITH ESP32 FOR CART CONTROL");
            sendLogMessageToCartControlDebugConsole("EVENT -> CART CONTROL N/A");
            sendLogMessageToCartControlDebugConsole("INIT VALUES -> Fix:" + GeofenceHelper.fixHdopThreshold + "/" + GeofenceHelper.fixSnrThreshold + " Geo:" + GeofenceHelper.geofenceHdopThreshold + "/" + GeofenceHelper.geofenceSnrThreshold);
            hasBluetoothPairing = false;
            cartControlVersion = "N/A";
            updateAppString();
        }
        return true;
    } //Sends message

    public static void sendGeofenceToCartControl() {

        if (geofenceSendQueue.isEmpty()) return;

        if (geofenceSendQueue.size() == geofenceList.size()) {
            inCartControlGeofence = false;
            breachedCartGeofenceID = "";
            breachCartGeofenceIsAudible = false;
            bluetoothService.sendMessage("$clear#");
            bluetoothService.sendMessage("$gps," + GeofenceHelper.geofenceHdopThreshold + "," + GeofenceHelper.geofenceSnrThreshold + "#");
        }

        if (!geofenceSendQueue.isEmpty()) {
            Gson gson = new Gson();
            String json;
            GeofenceGeometry geofence = geofenceSendQueue.poll(); // Removes and retrieves the head of the queue
            try {
                json = gson.toJson(geofence);
                bluetoothService.sendMessage("$gfd," + json + "#");
                Thread.sleep(1500); //1350
            } catch (InterruptedException e) {
            }
            updateAppString();
        }
    }

    public static void configureCartControlGeofences() {
        if (!CartControlHelper.geofenceList.isEmpty())
            return; //This only updates the list of CartControlGeofences once. Clear the list to allow it to refresh.
        if (PreferenceManager.getInstance().getGeoFences() == null) return; //Not initialized yet

        var geoFences = PreferenceManager.getInstance().getGeoFences();

        CartControlHelper.geofenceSendQueue.clear();

        Gson gson = new Gson();
        String json;

        for (RestGeoFenceModel geofence : geoFences) {

            if (geofence.isCartControlActive() && geofence.isActive()) {
                var geometryCoordinates = geofence.getGeometry().getLatLngCoordinates();
                com.tagmarshal.golf.util.GeofenceGeometry newGeofence = new GeofenceGeometry();
                newGeofence.setAudible(geofence.playSound());
                //test - MUST be removed for production
                //newGeofence.setAudible(true);
                newGeofence.setActive(geofence.isActive());
                newGeofence.setNotify(geofence.isNotify());
                newGeofence.setID(geofence.getId());

                for (LatLng latLng : geometryCoordinates) {
                    newGeofence.addPoint(latLng.latitude, latLng.longitude);
                }

                if (geometryCoordinates.size() <= 25) {
                    Log.d("GEOGT", "Adding Cart Control Geofence ID: " + geofence.getId() + " Active: " + geofence.isCartControlActive());
                    CartControlHelper.geofenceList.add(newGeofence);
                    CartControlHelper.geofenceSendQueue.add(newGeofence);
                } else {
                    Log.d("GEOGT", "Geofence has more than 25 points - ignoring");
                }

                //json= gson.toJson(newGeofence);
                //Log.d("GEOGT","CartGeofence:"+json);
            }
        }

        if (!CartControlHelper.isCartControlThreadActive) startCartControlThread();

        updateAppString();
    }


    private static synchronized void startCartControlThread() {
        CartControlHelper.isCartControlThreadActive = true; // Set the flag to prevent multiple threads
        new Thread(() -> {
            try {
                CartControlHelper.connectToCartControl();
            } catch (IOException e) {
            }
        }).start();
    }

    private static void updateAppString() {
        if (mainActivity != null) {
            int geofenceCount = CartControlHelper.geofenceList.size();
            int geoFenceQueueCount = CartControlHelper.geofenceSendQueue.size();
            int geoFencesAlreadySent = geofenceCount - geoFenceQueueCount;
            String cartControlInformation = "   |   GEOFENCES: " + GeofenceHelper.geofenceList.size() + "   CART STOP: " + cartControlVersion + "  " + geoFencesAlreadySent + "/" + geofenceCount;
            sendLogMessageToCartControlDebugConsole("FW -> " + cartControlInformation);
            new Handler(Looper.getMainLooper()).post(() -> {
                mainActivity.updateAppVersionWithCartControlInformation(cartControlInformation);
            });
        }
    }

    public static void sendLogMessageToCartControlDebugConsole(String message) {
        if (CartControlHelper.isCartControlLoggingActive) {
            DebugConsoleActivity.getInstance().writeToLog(message);
        }
    }

    public static void showDebugConsole(Context context) {
        if (context != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Log.d("GEOGT", "Attempting to start DebugConsoleActivity");
                    Intent intent = new Intent(context, DebugConsoleActivity.class);
                    context.startActivity(intent);
                    Log.d("GEOGT", "DebugConsoleActivity started successfully");

                    // Delay the writeToLog call to ensure DebugConsoleActivity is initialized
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        DebugConsoleActivity instance = DebugConsoleActivity.getInstance();
                        if (instance != null) {
                            instance.writeToLog("Debug Console Started");
                            isCartControlLoggingActive = true;
                        } else {
                            Log.e("GEOGT", "DebugConsoleActivity instance is null");
                        }
                    }, 500); // Adjust the delay as needed
                } catch (Exception e) {
                    Log.e("GEOGT", "Failed to start DebugConsoleActivity", e);
                }
            });
        } else {
            Log.d("GEOGT", "Context is null, cannot start DebugConsoleActivity");
        }
    }
}
