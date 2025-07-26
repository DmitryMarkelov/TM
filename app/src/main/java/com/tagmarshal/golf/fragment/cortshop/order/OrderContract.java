package com.tagmarshal.golf.fragment.cortshop.order;

import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.rest.model.SendOrderModel;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

public interface OrderContract {

    interface View{
        void onOrderItemsReceived(List<OrderItem> orderItems, double totalPrice);
        void onError(String message);
        void onSendSuccess(ResponseBody responseBody);
        void onKitchenClosed();
        void showLoader(boolean b);
    }
    interface Model{
        Observable<ResponseBody> sendOrder(SendOrderModel sendOrderModel);
        Observable<List<ActiveTimeDaysItemModel>> getKitchenHours();
    }
    interface Presenter{
        void clearOrder();
        void getOrderItems();
        void sendOrder(String orderNote);
    }
}
