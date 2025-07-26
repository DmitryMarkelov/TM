package com.tagmarshal.golf.fragment.cortshop.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.MenuItemOptionsAdapter;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.util.TMUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UpdateOrderItemFragment extends BaseFragment implements UpdateOrderItemContract.View {
    @BindView(R.id.item_options_recyclerview)
    RecyclerView item_options_recyclerview;
    @BindView(R.id.specialInstructions)
    EditText specialInstructions;
    @BindView(R.id.food_name_tv)
    TextView menuItemName;
    @BindView(R.id.food_price_tv)
    TextView menuItemPrice;
    @BindView(R.id.food_description_tv)
    TextView menuItemDescription;
    @BindView(R.id.item_count_textview)
    TextView menuItemCount;
    @BindView(R.id.item_count_circle)
    TextView menuItemCountCircle;
    @BindView(R.id.add_to_cart_button)
    ImageButton addToCartButton;
    @BindView(R.id.add_item_button)
    ImageButton addItemButton;
    @BindView(R.id.remove_item_button)
    ImageButton removeItemButton;
    @BindView(R.id.backButton)
    ImageButton backButton;
    @BindView(R.id.top_margin)
    View mTopMargin;
    @BindView(R.id.update_order_button)
    Button updateOrderButton;
    @BindView(R.id.add_to_cart_container)
    LinearLayout addToCardButtonsContainer;
    private OrderItem orderItem;
    private MenuItemOptionsAdapter menuItemOptionsAdapter;
    private double orderItemTotalPrice = 0.0;
    private UpdateOrderItemPresenter presenter;

    public static UpdateOrderItemFragment getInstance(OrderItem orderItem) {
        UpdateOrderItemFragment updateOrderItemFragment = new UpdateOrderItemFragment();
        Bundle args = new Bundle();
        args.putParcelable("orderItem", orderItem);
        updateOrderItemFragment.setArguments(args);
        return updateOrderItemFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.update_order_item_fragment, container, false);
        setRetainInstance(true);
        Unbinder unbinder = ButterKnife.bind(this, view);
        presenter = new UpdateOrderItemPresenter(this);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        addToCardButtonsContainer.setVisibility(View.VISIBLE);
        menuItemCountCircle.setVisibility(View.GONE);
        return view;
    }

    @OnClick(R.id.add_item_button)
    void onAddItem() {
        int quantity = orderItem.getQuantity() + 1;
        orderItem.setQuantity(quantity);
        menuItemCount.setText(String.valueOf(quantity));
        setAddToOrderButtonText();
    }

    @OnClick(R.id.remove_item_button)
    void onRemoveItem() {
        int quantity = orderItem.getQuantity() - 1;
        if (quantity != 0) {
            orderItem.setQuantity(quantity);
            menuItemCount.setText(String.valueOf(quantity));
            setAddToOrderButtonText();
        }
    }

    @OnClick(R.id.backButton)
    void onBackButton() {
        popFragment();
    }

    void setAddToOrderButtonText() {
        updateOrderButton.setText(String.format("Update Item - %d %s%.2f",
                orderItem.getQuantity(),
                PreferenceManager.getInstance().getMapBounds().getCurrency(),
                orderItemTotalPrice)
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OrderItem orderItem = getArguments().getParcelable("orderItem");
        presenter.showOrderItemDetails(orderItem);
    }

    @Override
    public void updateUI(OrderItem item) {
        orderItem = item;
        orderItemTotalPrice = orderItem.getPrice();
        setAddToOrderButtonText();

        menuItemName.setText(orderItem.getName());
        menuItemPrice.setText(String.format("TOTAL : %s%.2f", PreferenceManager.getInstance().getCurrency(), orderItem.getPrice()));
        menuItemDescription.setText(orderItem.getDescription());
        menuItemCount.setText(String.valueOf(orderItem.getQuantity()));
        specialInstructions.setText(orderItem.getInstructions());

        item_options_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        menuItemOptionsAdapter = new MenuItemOptionsAdapter(orderItem.getModifiers(), orderItem.getSelectedOptions(), getContext(), new MenuItemOptionsAdapter.OnMenuOptionClickListener() {
            @Override
            public void addOptionPrice(Double price) {
                orderItemTotalPrice += price;
                setAddToOrderButtonText();
            }

            @Override
            public void removeOptionPrice(Double price) {
                orderItemTotalPrice = orderItemTotalPrice - price;
                setAddToOrderButtonText();
            }
        }, getBaseActivity());
        item_options_recyclerview.setAdapter(menuItemOptionsAdapter);
    }

    @OnClick(R.id.update_order_button)
    void onUpdateOrderItem() {
        orderItem.setInstructions(specialInstructions.getText().toString());
        BigDecimal roundedPrice = new BigDecimal(orderItemTotalPrice).setScale(2, RoundingMode.HALF_UP);
        orderItem.setPrice(roundedPrice.doubleValue());

        if (menuItemOptionsAdapter.selectedOptionsValid()) {
            orderItem.setSelectedOptions(menuItemOptionsAdapter.getSelectedOptions());
            presenter.updateOrderItem(orderItem);
            popFragment();
        }
    }


    public void showError(String message) {
        GolfAlertDialog dialog = new GolfAlertDialog(getActivity())
                .setBottomBtnText(getString(R.string.pls_assist_us))
                .setCancelable(false)
                .hideBottomButton()
                .setOkText(getString(R.string.okay))
                .setIcon(R.drawable.ic_info)
                .setMessage(message)
                .setTitle(getString(R.string.server_error));

        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                dialog.dissmiss();
            }

            @Override
            public void onBottomBtnClick() {
                dialog.dissmiss();
            }
        });

        dialog.show();
    }

    @Override
    public void showLoader(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }
}
