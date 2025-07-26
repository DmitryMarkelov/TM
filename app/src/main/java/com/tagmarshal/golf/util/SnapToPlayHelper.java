package com.tagmarshal.golf.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

//import com.tagmarshal.golf.rest.model.SnapToPlayStatusModel;

import com.tagmarshal.golf.manager.PreferenceManager;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SnapToPlayHelper {

    public static Context powerContext = null;
    public static boolean hasMusic = true;  //Mocked until snapToPlayModel is included in REST Bounds //
    public static String speakerBluetoothClassicMacAddress = "";
    public static String currentActiveAudioID = "";

    private static final int MAX_VOLUME = 85;
    private static final double AUDIO_GAIN = 0;

    public static boolean resetSnapToPlay() {
        if (powerContext == null) return false;
        turnSnapToPlayPowerOff();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        turnSnapToPlayPowerOn();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        return true;
    }

    public static boolean turnSnapToPlayPowerOff() {
        if (powerContext == null) return false;
        
        PowerManager pm = (PowerManager) powerContext.getSystemService(Context.POWER_SERVICE);
        PowerManagerUtils.close(pm, 18); //On 2023 firmware the POWER UART is 2
        return true;
    }

    public static boolean turnSnapToPlayPowerOn() {
        if (powerContext == null) return false;
        PowerManager pm = (PowerManager) powerContext.getSystemService(Context.POWER_SERVICE);
        PowerManagerUtils.open(pm, 18);
        return true;
    }

    @SuppressLint("MissingPermission")
    public static boolean checkSpeakerPairing() {
        resetSnapToPlay();
        if (PreferenceManager.getInstance().getSpeakerMac() != "") {
            if (speakerBluetoothClassicMacAddress == "") {
                speakerBluetoothClassicMacAddress = PreferenceManager.getInstance().getSpeakerMac();
                Log.d("SNAP", "checkSpeakerPairing() Using stored MAC - " + speakerBluetoothClassicMacAddress);
                return true;
            }
        } else {
            Log.d("SNAP", "checkSpeakerPairing() No MAC - requesting pairSpeakersIfAvailable()");
            pairSpeakersIfAvailable();
        }

        return false;
    }

    @SuppressLint("MissingPermission")
    public static boolean removeSpeakerPairing() {
        PreferenceManager.getInstance().setAudioID("");
        PreferenceManager.getInstance().setSpeakerMac("");
        return false;
    }

    @SuppressLint("MissingPermission")
    public static boolean pairSpeakersIfAvailable() {

        Log.d("SNAP", "pairSpeakersIfAvailable() called");
            
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = null;
        for (BluetoothDevice pairedDevice : adapter.getBondedDevices()) {
            if (pairedDevice.getName().startsWith("TM_PAIR")) {
                Log.d("SNAP", "Found TM_PAIR device - Name:" + pairedDevice.getName() + " MAC:" + pairedDevice.getAddress());
                device = pairedDevice;
            }
        }

        BluetoothSocket socket = null;
        if (device != null) {
            try {
                //BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                socket.connect();
                PreferenceManager.getInstance().setSpeakerMac(device.getAddress());
                speakerBluetoothClassicMacAddress = PreferenceManager.getInstance().getSpeakerMac();
                Log.d("SNAP", "Pairing with - " + device.getName() + " and storing MAC:" + speakerBluetoothClassicMacAddress);
                byte[] bytes = ("$prd#").getBytes(StandardCharsets.UTF_8);
                socket.getOutputStream().write(bytes);

                //timeout the thread for 500ms
                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    Log.d("SNAP", "PairSpeakersIfAvailable() Exception:" + e.getMessage());
                    return false;
                }
                Log.d("SNAP", "Paired with - " + device.getName() + " and storing MAC:" + speakerBluetoothClassicMacAddress);
                PreferenceManager.getInstance().setAudioID("");
                return true;
            } catch (Exception e) {
                Log.d("SNAP", "PairSpeakersIfAvailable() Exception:" + e.getMessage());
                return false;
            } finally {
                Log.d("SNAP", "Closing Socket");
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
                Log.d("SNAP", "Socket Closed");
            }
        } else {
            Log.d("SNAP", "No TM_PAIR device found");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean sendMessageToSpeakerMac(String speakerBluetoothClassicMacAddress, String audioID) throws IOException {
        Log.d("SNAP", "sendMessageToSpeakerMac() called with audioID:" + audioID + " at MAC:" + speakerBluetoothClassicMacAddress);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        String esp32MacAddress = speakerBluetoothClassicMacAddress;
        BluetoothDevice device = adapter.getRemoteDevice(esp32MacAddress);
        BluetoothSocket socket = null;
        if (device != null) {
            Log.d("SNAP", "SendMessage Device found");
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                socket.connect();
                if (socket.isConnected()) {
                    Log.d("SNAP", "Connected to device - setting audio ID: " + audioID + " at MAC:" + speakerBluetoothClassicMacAddress);
                    byte[] bytes = ("$aon" + audioID + "," + String.valueOf(MAX_VOLUME) + "," + String.valueOf(AUDIO_GAIN) + "#").getBytes(StandardCharsets.UTF_8);
                    socket.getOutputStream().write(bytes);
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (Exception e) {
                    }
                    Log.d("SNAP", "Connected to device - set audio ID: " + audioID + " at MAC:" + speakerBluetoothClassicMacAddress);
                    return true;
                } else {
                    Log.d("SNAP", "Socket not connected to device");
                    return false;
                }
            } catch (InterruptedException e) {
                Log.d("SNAP", "Exception:" + e.getMessage());
                return false;
            } finally {
                if (socket != null) {
                    socket.close();
                    Log.d("SNAP", "Closed Socket");
                }
            }
        } else {
            Log.d("SNAP", "SendMessage Device not found");
            return false;
        }
    }

    public static boolean turnOnPairingMode() {
        if (powerContext == null) return false;

        Log.d("SNAP", "turnOnPairingMode() called ");
        
        PreferenceManager.getInstance().setSpeakerMac("");
        PreferenceManager.getInstance().setAudioID("pairmode");

        SnapToPlayHelper.turnSnapToPlayPowerOff();
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Start power cycle
        //Power #1
        SnapToPlayHelper.turnSnapToPlayPowerOn();
        try {
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SnapToPlayHelper.turnSnapToPlayPowerOff();
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Power #2
        SnapToPlayHelper.turnSnapToPlayPowerOn();
        try {
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SnapToPlayHelper.turnSnapToPlayPowerOff();
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Power #3
        SnapToPlayHelper.turnSnapToPlayPowerOn();
        try {
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public static String setNewAudioID() {
        String audioID = RandomStringGenerator.generateRandomString();
        PreferenceManager.getInstance().setAudioID(audioID);
        return audioID;
    }

    public static boolean clearAudioID() {
        PreferenceManager.getInstance().setAudioID("");
        return true;
    }

    @SuppressLint("MissingPermission")
    public static Boolean turnSnapToPlayAudioOn() {
        String audioID = PreferenceManager.getInstance().getAudioID();
        Log.d("SNAP", "turnSnapToPlayAudioOn() called with audioID:" + audioID);
        String speakerBluetoothClassicMacAddress = PreferenceManager.getInstance().getSpeakerMac();
        if (speakerBluetoothClassicMacAddress != "") {
            resetSnapToPlay();
            try {
                TimeUnit.MILLISECONDS.sleep(1500);
            } catch (Exception e) {
            }
            try {
                sendMessageToSpeakerMac(speakerBluetoothClassicMacAddress, audioID);
            } catch (IOException e) {
                Log.d("SNAP", "turnSnapToPlay Audio on Exception:" + e.getMessage());
                return false;
            }
        }
        currentActiveAudioID = audioID;
        return true;
    }

    public class RandomStringGenerator {
        public static void main(String[] args) {
            String randomString = generateRandomString();
        }

        public static String generateRandomString() {
            Random random = new Random();
            char randomLetter = (char) (random.nextInt(26) + 'A');
            StringBuilder randomNumbers = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int randomDigit = random.nextInt(10);
                randomNumbers.append(randomDigit);
            }
            return randomLetter + randomNumbers.toString();
        }
    }
}
