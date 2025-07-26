package com.tagmarshal.golf.adapter;

import com.tagmarshal.golf.rest.model.FoodModel;

public interface OnOptionClickListener {
    void onCheckItem(FoodModel.AdditionModel option);

    void onUncheckItem(FoodModel.AdditionModel option);
}
