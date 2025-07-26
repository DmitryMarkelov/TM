package com.tagmarshal.golf.fragment.cortshop.order;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.SendOrderModel;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

public class OrderModel implements OrderContract.Model {
    @Override
    public Observable<ResponseBody> sendOrder(SendOrderModel sendOrderModel) {
        return GolfAPI.getGolfCourseApi().sendOrder(sendOrderModel);
    }
    @Override
    public Observable<List<ActiveTimeDaysItemModel>> getKitchenHours() {
        return GolfAPI.getGolfCourseApi().getKitchenHours();
    }
}
