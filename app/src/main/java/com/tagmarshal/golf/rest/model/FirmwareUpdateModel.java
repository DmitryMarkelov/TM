package com.tagmarshal.golf.rest.model;

public class FirmwareUpdateModel {

    private String fileName; // name of the file from the server
    private long downloadID;

    private int status;
    private int progress;

    // Statuses
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOAD_COMPLETE = 4;
    public static final int INSTALLED = 5;

    public FirmwareUpdateModel(String fileName) {
        this.fileName = fileName;
        this.status = DOWNLOADING;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getFileName() {
        return fileName;
    }

    public long getDownloadID() {
        return downloadID;
    }

    public void setDownloadID(long downloadID) {
        this.downloadID = downloadID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
