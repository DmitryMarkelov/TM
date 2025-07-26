package com.tagmarshal.golf.fragment.cortshop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmadhamwi.tabsync.TabbedListMediator;
import com.google.android.material.tabs.TabLayout;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.MenuGroupAdapter;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.dialog.PleaseWaitDialog;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.cortshop.kitchen.KitchenFragment;
import com.tagmarshal.golf.fragment.cortshop.order.OrderFragment;
import com.tagmarshal.golf.rest.model.MenuGroup;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FoodMenuFragment extends BaseFragment implements FoodMenuContract.View {

    @BindView(R.id.shop_tabs)
    TabLayout menuGroupTabs;

    @BindView(R.id.menu_recyclerview)
    RecyclerView menuRecyclerview;

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.viewOrderButton)
    Button viewOrderButton;

    private FoodMenuPresenter presenter;
    private Unbinder unbinder;
    private MenuGroupAdapter menuGroupAdapter;
    private PleaseWaitDialog waitDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cortshop, container, false);
        unbinder = ButterKnife.bind(this, view);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        menuRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        menuGroupAdapter = new MenuGroupAdapter(getContext(), getBaseActivity());
        menuRecyclerview.setAdapter(menuGroupAdapter);
        waitDialog = new PleaseWaitDialog(getContext());

        return view;
    }

    @OnClick(R.id.viewOrderButton)
    void onViewOrder() {
        getBaseActivity().addFragment(new OrderFragment(), true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new FoodMenuPresenter(this);
        presenter.attachView(this);
        presenter.getKitchenHours();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setMenuData(List<MenuGroup> menuGroupList, List<OrderItem> orderItems) {

        if (!menuGroupList.isEmpty()) {
            initTabs(menuGroupList);
            initMediator(menuGroupList);
        }

        if (!orderItems.isEmpty()) {
            viewOrderButton.setVisibility(View.VISIBLE);
        } else {
            viewOrderButton.setVisibility(View.GONE);
        }
        menuGroupAdapter.updateAdapterData(menuGroupList, orderItems);
        menuGroupAdapter.notifyDataSetChanged();
    }

    private void initMediator(List<MenuGroup> menuGroupList) {
        new TabbedListMediator(menuRecyclerview, menuGroupTabs, getMenuGroupIndices(menuGroupList), true).attach();
    }

    private List<Integer> getMenuGroupIndices(List<MenuGroup> menuGroupList) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < menuGroupList.size(); i++) {
            indices.add(i);
        }
        return indices;
    }

    private void initTabs(List<MenuGroup> menuGroupList) {
        menuGroupTabs.removeAllTabs();
        for (MenuGroup group : menuGroupList) {
            menuGroupTabs.addTab(menuGroupTabs.newTab().setText(group.getName()));
        }
    }

    @Override
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

    public void showPleaseWaitDialog(boolean show) {
        if (show) {
            waitDialog.show();
        } else {
            waitDialog.dismiss();
        }
    }

    @Override
    public void showLoader(boolean b) {
        showPleaseWaitDialog(b);
    }

    @Override
    public void showClosedKitchenDialog() {
        replaceThisFragment(this, new KitchenFragment());
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        presenter = null;
        super.onDestroyView();
    }
}
