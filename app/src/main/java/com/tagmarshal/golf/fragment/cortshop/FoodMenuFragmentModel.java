package com.tagmarshal.golf.fragment.cortshop;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.MenuGroup;

import java.util.List;

import io.reactivex.Observable;

public class FoodMenuFragmentModel implements FoodMenuContract.Model {
    @Override
    public Observable<List<MenuGroup>> getMenuGroup() {
        return GolfAPI.getGolfCourseApi().getFood();
    }

    @Override
    public Observable<List<ActiveTimeDaysItemModel>> getKitchenHours() {
        return GolfAPI.getGolfCourseApi().getKitchenHours();
    }
}
