package com.tagmarshal.golf.rest.model;

import java.util.List;

public class MenuGroup {
    private String name;
    private List<MenuItem> items;
    private List<ActiveTimeDaysItemModel> activeTimeDays;

    public String getName() {
        return name;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public List<ActiveTimeDaysItemModel> getActiveTimeDays() {
        return activeTimeDays;
    }
}