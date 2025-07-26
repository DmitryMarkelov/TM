package com.tagmarshal.golf.fragment.cortshop.order;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.rest.model.MenuItemModifier;
import com.tagmarshal.golf.rest.model.MenuItemModifierOption;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.rest.model.SendOrderItem;
import com.tagmarshal.golf.rest.model.SendOrderModel;
import com.tagmarshal.golf.rest.model.SendOrderModifier;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderPresenter implements OrderContract.Presenter {

    private final CompositeDisposable disposable;
    private final OrderContract.View view;
    private final OrderModel model;
    private List<OrderItem> orderItems = new ArrayList<>();
    private double orderTotalPrice = 0.0;

    public OrderPresenter(OrderContract.View view) {
        this.disposable = new CompositeDisposable();
        this.model = new OrderModel();
        this.view = view;
    }

    @Override
    public void clearOrder() {
        PreferenceManager.getInstance().clearOrderItems();
    }

    @Override
    public void getOrderItems() {
        orderItems = PreferenceManager.getInstance().getOrderItems();
        orderTotalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            orderTotalPrice += (orderItem.getPrice() * orderItem.getQuantity());
        }
        view.onOrderItemsReceived(orderItems, orderTotalPrice);
    }

    private SendOrderItem convertToSendOrderItem(OrderItem orderItem) {
        SendOrderItem sendOrderItem = new SendOrderItem();
        sendOrderItem.setId(orderItem.getId());

        // Create a list to hold ModifierDto
        List<SendOrderModifier> sendOrderModifiers = new ArrayList<>();

        // Iterate through each modifier in the order item
        for (MenuItemModifier modifier : orderItem.getModifiers()) {
            SendOrderModifier sendOrderModifier = new SendOrderModifier();
            sendOrderModifier.setId(modifier.getId());

            List<String> options = orderItem.getSelectedOptions().stream()
                    .filter(option -> modifier.getOptions().contains(option))
                    .map(MenuItemModifierOption::getId)
                    .collect(Collectors.toList());

            if (!options.isEmpty()) {
                sendOrderModifier.setOptions(options);
                sendOrderModifiers.add(sendOrderModifier);
            }
        }

        sendOrderItem.setModifiers(sendOrderModifiers);
        return sendOrderItem;
    }

    private void processOrder(String orderNote) {
        String roundID = PreferenceManager.getInstance().getRoundID();
        if (roundID == null) {
            roundID = null;
        }

        List<SendOrderItem> sendOrderItems = orderItems.stream().map(this::convertToSendOrderItem).collect(Collectors.toList());
        SendOrderModel sendOrderModel = new SendOrderModel();

        // Update Quantities
        List<SendOrderItem> quantitySendOrderItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getQuantity() > 1) {
                for (SendOrderItem sendOrderItem : sendOrderItems) {
                    if (orderItem.getId().equalsIgnoreCase(sendOrderItem.getId())) {
                        for (int i = 0; i < orderItem.getQuantity()-1; i++) {
                            quantitySendOrderItems.add(sendOrderItem);
                        }
                    }
                }
            }
        }
        sendOrderItems.addAll(quantitySendOrderItems);

        sendOrderModel.setItems(sendOrderItems);
        sendOrderModel.setRound(roundID);
        sendOrderModel.setSpecialInstructions(orderNote);

        disposable.add(
                model.sendOrder(sendOrderModel)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(__ -> view.showLoader(true))
                        .doOnTerminate(() -> view.showLoader(false))
                        .subscribe(view::onSendSuccess, e -> view.onError(e.getMessage()))
        );
    }

    @Override
    public void sendOrder(String orderNote) {
        disposable.add(
                model.getKitchenHours()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(__ -> view.showLoader(true))
                        .doOnTerminate(() -> view.showLoader(false))
                        .subscribe(kitchenHours -> {
                                    if (!TMUtil.activeTimeDaysPass(kitchenHours)) {
                                        view.onKitchenClosed();
                                    } else {
                                        processOrder(orderNote);
                                    }
                                },
                                e -> view.onError(e.getMessage())
                        )
        );
    }
}
