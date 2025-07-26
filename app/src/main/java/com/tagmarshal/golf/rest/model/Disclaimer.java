package com.tagmarshal.golf.rest.model;

import java.util.Date;
import java.util.List;

public class Disclaimer {
    private String text;
    private boolean isActive;
    private List<Fields> fields;

    private DisclaimerStatus status;
    private DataEntryStatus dataEntryStatus;
    private Long lastShownTime;

    public String getText() {
        return text;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<Fields> getFields() {
        return fields;
    }

    public DisclaimerStatus getStatus() {
        return status;
    }

    public void setStatus(DisclaimerStatus status) {
        this.status = status;
    }

    public DataEntryStatus getDataEntryStatus() {
        return dataEntryStatus;
    }

    public void setDataEntryStatus(DataEntryStatus dataEntryStatus) {
        this.dataEntryStatus = dataEntryStatus;
    }

    public Long getLastShownTime() {
        return lastShownTime;
    }

    public void setLastShownTime(Long lastShownTime) {
        this.lastShownTime = lastShownTime;
    }
}
