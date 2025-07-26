package com.tagmarshal.golf.enums;

import java.util.Calendar;

public class DayEnum {
    public static int getDayIndex(String dayString) {
        switch (dayString) {
            case "Mon":
                return Calendar.MONDAY;
            case "Tue":
                return Calendar.TUESDAY;
            case "Wed":
                return Calendar.WEDNESDAY;
            case "Thu":
                return Calendar.THURSDAY;
            case "Fri":
                return Calendar.FRIDAY;
            case "Sat":
                return Calendar.SATURDAY;
            default:
                return Calendar.SUNDAY;
        }
    }
}
