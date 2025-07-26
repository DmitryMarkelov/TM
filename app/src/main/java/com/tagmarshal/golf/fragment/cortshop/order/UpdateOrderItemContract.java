package com.tagmarshal.golf.fragment.cortshop.order;

import com.tagmarshal.golf.rest.model.OrderItem;

public class UpdateOrderItemContract {
    interface View {
        void showError(String error);
        void updateUI(OrderItem orderItem);

        void showLoader(boolean show);
    }

    interface Presenter {
        void showOrderItemDetails(OrderItem orderItem);
        void updateOrderItem(OrderItem orderItem);
    }
}
