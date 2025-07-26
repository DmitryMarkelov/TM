package com.tagmarshal.golf.fragment.cortshop.order;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.OrderItem;

public class UpdateOrderItemPresenter implements UpdateOrderItemContract.Presenter{
    private UpdateOrderItemContract.View view;
    public UpdateOrderItemPresenter(UpdateOrderItemContract.View view)
    {
        this.view = view;
    }
    @Override
    public void showOrderItemDetails(OrderItem orderItem) {
        view.updateUI(orderItem);
    }

    @Override
    public void updateOrderItem(OrderItem orderItem) {
        PreferenceManager.getInstance().updateOrderItem(orderItem);
    }
}
