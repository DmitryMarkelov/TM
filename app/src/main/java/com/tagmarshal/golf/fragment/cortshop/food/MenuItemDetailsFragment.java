package com.tagmarshal.golf.fragment.cortshop.food;

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
import com.tagmarshal.golf.rest.model.MenuItem;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.util.TMUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MenuItemDetailsFragment extends BaseFragment implements MenuItemDetailsContract.View {
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
    @BindView(R.id.add_to_order_button)
    Button addToOrderButton;
    @BindView(R.id.add_to_cart_container)
    LinearLayout addToCardButtonsContainer;
    private MenuItemDetailsPresenter presenter;
    private OrderItem orderItem;
    private MenuItemOptionsAdapter menuItemOptionsAdapter;
    private double orderItemTotalPrice = 0.0;

    public static MenuItemDetailsFragment getInstance(MenuItem menuItem) {
        MenuItemDetailsFragment menuItemDetailsFragment = new MenuItemDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("menuItem", menuItem);
        menuItemDetailsFragment.setArguments(args);
        return menuItemDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_item_details, container, false);
        setRetainInstance(true);
        Unbinder unbinder = ButterKnife.bind(this, view);
        presenter = new MenuItemDetailsPresenter(this);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        addToCardButtonsContainer.setVisibility(View.VISIBLE);
        menuItemCountCircle.setVisibility(View.GONE);
        menuItemCount.setText("1");
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

    void setAddToOrderButtonText() {
        addToOrderButton.setText(String.format("Add %d to order - %s%.2f", orderItem.getQuantity(), PreferenceManager.getInstance().getMapBounds().getCurrency(), orderItemTotalPrice)
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuItem menuItem = getArguments().getParcelable("menuItem");
        backButton.setOnClickListener(view1 -> popFragment());
        presenter.showMenuItemDetails(menuItem);
    }

    @Override
    public void updateUI(MenuItem menuItem) {
        orderItem = new OrderItem();
        orderItem.setId(menuItem.getId());
        orderItem.setName(menuItem.getName());
        orderItem.setPrice(menuItem.getPrice());
        orderItem.setModifiers(menuItem.getModifiers());
        orderItem.setQuantity(1);
        orderItemTotalPrice = orderItem.getPrice();
        setAddToOrderButtonText();

        menuItemName.setText(menuItem.getName());
        menuItemPrice.setText(String.format("TOTAL : %s%.2f", PreferenceManager.getInstance().getCurrency(), menuItem.getPrice()));
        menuItemDescription.setText(menuItem.getDescription());

        item_options_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        menuItemOptionsAdapter = new MenuItemOptionsAdapter(menuItem.getModifiers(), new ArrayList<>(), getContext(), new MenuItemOptionsAdapter.OnMenuOptionClickListener() {
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

    @OnClick(R.id.add_to_order_button)
    void onAddToOrder() {
        orderItem.setInstructions(specialInstructions.getText().toString());
        BigDecimal roundedPrice = new BigDecimal(orderItemTotalPrice).setScale(2, RoundingMode.HALF_UP);
        orderItem.setPrice(roundedPrice.doubleValue());

        if (menuItemOptionsAdapter.selectedOptionsValid()) {
            orderItem.setSelectedOptions(menuItemOptionsAdapter.getSelectedOptions());
            List<OrderItem> orderItems = new ArrayList<>();
            orderItems.add(orderItem);
            PreferenceManager.getInstance().setOrderItems(orderItems);
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

            }

            @Override
            public void onBottomBtnClick() {

            }
        });

        dialog.show();
    }

    @Override
    public void showLoader(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }
}
