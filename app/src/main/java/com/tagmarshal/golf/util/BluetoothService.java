package com.tagmarshal.golf.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.manager.PreferenceManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BluetoothService {
    private static final UUID ESP32_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    
    public  static Boolean hasFoundPairedCartControlDevice = false; //if listed in pairings
    public static Boolean inGeofence = false;
    public static Queue<String> messageQueue = new LinkedList<>();
    private StringBuilder messageBuffer = new StringBuilder();

    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("MissingPermission")
    public boolean connect() {
        
        String cartControlMac = PreferenceManager.getInstance().getCartControlMac();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if (device.getName().startsWith("TMCART_")) {
                Log.d("GEOGT", "Detected cart control device: " + device.getName());
                hasFoundPairedCartControlDevice = true;
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID);
                    if (bluetoothSocket == null) {
                        return false;
                    }
                    bluetoothSocket.connect();
                    if (bluetoothSocket.isConnected()) {
                        outputStream = bluetoothSocket.getOutputStream();
                        inputStream = bluetoothSocket.getInputStream();
                        sendMessage("$prd#");
                        return true;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    public void sendMessage(String message) {
        Log.d("GEOGT", "Sending:" + message);
        try {
            if (outputStream != null) {
                outputStream.write(message.getBytes());
            }
        } catch (IOException e) {
            Log.d("GEOGT", "Unable to send msg. Attempting reconnection");
            if (FragmentMap.instance != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        CartControlHelper.inCartControlGeofence = false;
                        FragmentMap.instance.hideCartControlGeofenceAlertDialog();
                    } catch (Exception e2) {
                    }
                });
            }
            connect();
        }
    }

    public String receiveMessage() {
        try {
            if (inputStream != null && inputStream.available() > 0) {
                while (inputStream.available() > 0) {
                    // Read the next byte
                    char nextChar = (char) inputStream.read();

                    // If the start character is detected, reset the buffer
                    if (nextChar == '$') {
                        messageBuffer.setLength(0); // Clear the buffer
                    }

                    // Append the character to the buffer
                    messageBuffer.append(nextChar);

                    // If the end character is detected, process the completed message
                    if (nextChar == '#') {
                        String completeMessage = messageBuffer.toString();
                        messageBuffer.setLength(0); // Clear the buffer for the next message

                        // Add the completed message to the queue
                        if (messageQueue.size() >= 1) {
                            messageQueue.poll(); // Remove the oldest message if the queue is full
                        }
                        messageQueue.add(completeMessage);
                        return completeMessage; // Optionally return the completed message immediately
                    }
                }
            }
        } catch (IOException e) {
        }
        return null; // Return null if no complete message is available
    }

    public static String popLastMessage() {
        if (!messageQueue.isEmpty()) {
            String lastMessage = ((LinkedList<String>) messageQueue).getLast();
            ((LinkedList<String>) messageQueue).removeLast();
            return lastMessage;
        } else {
            return "";
        }
    }

    public void disconnect() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
        }
    }
}
