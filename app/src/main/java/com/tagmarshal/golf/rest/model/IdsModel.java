package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IdsModel {
    @SerializedName("ids")
    private List<String> ids;


    public List<String> getIds() {
        return ids;
    }

    public void setFood(List<String> ids) {
        this.ids = ids;
    }

    public IdsModel(List<String> ids) {
        this.ids = ids;
    }
}
