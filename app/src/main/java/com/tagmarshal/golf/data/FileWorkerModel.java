package com.tagmarshal.golf.data;

import android.os.Environment;

import com.tagmarshal.golf.rest.GolfAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

import io.reactivex.Completable;

public class FileWorkerModel implements FileWorkerContract.Model {


    @Override
    public Completable writeToFile(String tag, String time, String battery, String isOnline, String inActive) {
        return Completable.create(emitter -> {
            File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            try {
            String log = tag + " : " + time + " : " + battery + " : " + isOnline + " : " + inActive + " : " + GolfAPI.getUsedMemorySize();
            writer.append(log);
            writer.append("\n");

            writer.close();
            fileOutputStream.close();
                emitter.onComplete();
            } catch (Throwable e) {
                emitter.onError(e);
            } finally {
                writer.close();
                fileOutputStream.close();
            }
        });
    }

    @Override
    public Completable writeToFile(String tag, String time, String battery) {
        return Completable.create(emitter -> {
            File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            try {
                String log = tag + " : " + time + " : " + battery + " : " + GolfAPI.getUsedMemorySize();
                writer.append(log);
                writer.append("\n");

                writer.close();
                fileOutputStream.close();
                emitter.onComplete();
            } catch (Throwable e) {
                emitter.onError(e);
            } finally {
                writer.close();
                fileOutputStream.close();
            }
        });
    }

    @Override
    public Completable writeToFile(String tag, String time, String battery, String
            isOnline, String inActive, String lat, String lon, String accuracy) {
        return Completable.create(emitter -> {
            File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            try {

                String log = tag + " : " + time + " : " + battery + " : " + isOnline + " : " + inActive + " : " + lat + "," + lon + "," + accuracy + " : " + GolfAPI.getUsedMemorySize();
                writer.append(log);
                writer.append("\n");

                writer.close();
                fileOutputStream.close();
                emitter.onComplete();
            } catch (Throwable e) {
                emitter.onError(e);
            } finally {
                writer.close();
                fileOutputStream.close();
            }
        });
    }

    @Override
    public Completable writeInAccuracyToFile(String tag, String time, String battery, String
            isOnline, String inActive, String lat, String lon, String accuracy) {
        return Completable.create(emitter -> {
            File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            try {


                String tail = tail(file);

                if (tail != null && !tail.contains(lat) && !tail.contains(lon)) {
                    String log = tag + " : " + time + " : " + battery + " : " + isOnline + " : " + inActive + " : " + lat + "," + lon + "," + accuracy;
                    writer.append(log);
                    writer.append("\n");

                    writer.close();
                    fileOutputStream.close();
                    emitter.onComplete();
                }
            } catch (Throwable e) {
                emitter.onError(e);
            } finally {
                writer.close();
                fileOutputStream.close();
            }
        });
    }

    private String tail(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
        }
    }

}
