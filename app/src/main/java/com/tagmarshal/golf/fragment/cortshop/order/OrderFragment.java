package com.tagmarshal.golf.fragment.cortshop.order;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.OrderAdapter;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.util.TMUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;

public class OrderFragment extends BaseFragment implements OrderContract.View {

    @BindView(R.id.order_recycler)
    RecyclerView orderRecycler;

    @BindView(R.id.backButton)
    ImageButton backButton;

    @BindView(R.id.returnHomeButton)
    Button returnHomeButton;

    @BindView(R.id.placeOrderButton)
    Button placeOrderButton;

    @BindView(R.id.top_margin)
    View topMarginView;

    @BindView(R.id.orderTotal)
    TextView orderTotal;

    @BindView(R.id.emptyOrderView)
    LinearLayout emptyOrderView;

    @BindView(R.id.orderTotalContainer)
    LinearLayout orderTotalContainer;

    @BindView(R.id.returnToMenu)
    Button returnToMenu;

    private OrderPresenter presenter;
    private OrderAdapter orderAdapter;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        setRetainInstance(true);
        Unbinder unbinder = ButterKnife.bind(this, view);
        topMarginView.getLayoutParams().height = TMUtil.getTopMargin();
        presenter = new OrderPresenter(this);
        backButton.setOnClickListener(view1 -> popFragment());
        presenter.getOrderItems();
        return view;
    }

    @OnClick(R.id.returnToMenu)
    void onReturnToMenu() {
        popFragment();
    }

    @OnClick(R.id.placeOrderButton)
    void onPlaceClick() {
        GolfAlertDialog dialog = new GolfAlertDialog(getActivity())
                .setBottomBtnText(getString(R.string.cancel))
                .setCancelable(false)
                .setOkText(getString(R.string.okay))
                .setIcon(R.drawable.ic_info)
                .setMessage(getString(R.string.confirm_order_place))
                .setTitle(getString(R.string.order_placeholder));

        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                presenter.sendOrder(orderAdapter.getOrderNote());
            }

            @Override
            public void onBottomBtnClick() {
                dialog.dissmiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSendSuccess(ResponseBody responseBody) {
        GolfAlertDialog dialog = new GolfAlertDialog(getActivity())
                .setBottomBtnText(getString(R.string.pls_assist_us))
                .setCancelable(false)
                .hideBottomButton()
                .setOkText(getString(R.string.okay))
                .setIcon(R.drawable.ic_info)
                .setMessage(getString(R.string.order_is_placed))
                .setTitle(getString(R.string.info_popup));

        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                presenter.clearOrder();
                popFragment();
            }

            @Override
            public void onBottomBtnClick() {
                dialog.dissmiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onKitchenClosed() {
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.food_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @OnClick(R.id.returnHomeButton)
    void onBackHome() {
        popFragment();
    }

    @Override
    public void onOrderItemsReceived(List<OrderItem> orderItems, double totalPrice) {
        orderRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(orderItems, getContext(), getBaseActivity(), orderItems1 -> {
            if (orderItems1.isEmpty()) {
                emptyOrderView.setVisibility(View.VISIBLE);
                orderRecycler.setVisibility(View.GONE);
                orderTotal.setText(String.format("TOTAL : %s%.2f", PreferenceManager.getInstance().getCurrency(), 0.00));
                orderTotalContainer.setVisibility(View.GONE);
            } else {
                emptyOrderView.setVisibility(View.GONE);
                orderRecycler.setVisibility(View.VISIBLE);
                orderTotalContainer.setVisibility(View.VISIBLE);
            }
            presenter.getOrderItems();
        });
        orderRecycler.setAdapter(orderAdapter);
        orderTotal.setText(String.format("TOTAL : %s%.2f", PreferenceManager.getInstance().getCurrency(), totalPrice));
    }

    @Override
    public void onError(String message) {
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
            }
        });
        dialog.show();
    }

    @Override
    public void showLoader(boolean b) {
        getBaseActivity().showPleaseWaitDialog(b);
    }
}

