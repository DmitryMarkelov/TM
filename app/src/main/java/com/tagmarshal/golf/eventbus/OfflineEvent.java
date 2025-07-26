package com.tagmarshal.golf.eventbus;

import java.util.Date;

public class OfflineEvent {
    private Date setDate;

    public OfflineEvent(Date setDate) {
        this.setDate = setDate;
    }

    public Date getSetDate() {
        return setDate;
    }
}
