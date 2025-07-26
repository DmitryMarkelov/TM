package com.tagmarshal.golf.fragment.cortshop;

import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.KitchenTimeModel;
import com.tagmarshal.golf.rest.model.MenuGroup;
import com.tagmarshal.golf.rest.model.MenuItem;
import com.tagmarshal.golf.rest.model.OrderItem;

import java.util.List;

import io.reactivex.Observable;

public interface FoodMenuContract {

    interface View {
        void setMenuData(List<MenuGroup> menu, List<OrderItem> orderItems);

        void showError(String message);

        void showLoader(boolean b);

        void showClosedKitchenDialog();
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void getMenu();

        List<OrderItem> getOrderItems();

        void getKitchenHours();
    }

    interface Model {
        Observable<List<MenuGroup>> getMenuGroup();

        Observable<List<ActiveTimeDaysItemModel>> getKitchenHours();
    }

}
