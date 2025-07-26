package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SendOrderItem {

    @SerializedName("id")
    private String id;

    @SerializedName("modifiers")
    private List<SendOrderModifier> modifiers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SendOrderModifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<SendOrderModifier> modifiers) {
        this.modifiers = modifiers;
    }
}
