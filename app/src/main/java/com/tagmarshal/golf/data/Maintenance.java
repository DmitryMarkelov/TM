package com.tagmarshal.golf.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Maintenance {
    private long fromTime;
    private long toTime;

    public Maintenance(String from_time, String toTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date from = dateFormat.parse(from_time);
            this.fromTime = from.getTime();
            if (toTime != null) {
                Date to = dateFormat.parse(toTime);
                this.toTime = to.getTime();
            } else {
                this.toTime = 0L;
            }
        } catch (ParseException e) {
            this.fromTime = 0L;
            this.toTime = 0L;
        }
    }

    public long getFromTime() {
        return fromTime;
    }

    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    public long getToTime() {
        return toTime;
    }

    public void setToTime(long toTime) {
        this.toTime = toTime;
    }
}
