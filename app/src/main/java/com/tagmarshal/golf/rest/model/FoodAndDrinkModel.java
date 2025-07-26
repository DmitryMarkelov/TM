package com.tagmarshal.golf.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FoodAndDrinkModel {

    @SerializedName("food")
    private List<FoodModel> food;

    @SerializedName("beverage")
    private List<FoodModel> beverage;


    public List<FoodModel> getFood() {
        return food;
    }

    public List<FoodModel> getBeverage() {
        return beverage;
    }
}
