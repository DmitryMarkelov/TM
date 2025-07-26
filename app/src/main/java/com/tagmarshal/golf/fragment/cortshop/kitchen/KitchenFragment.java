package com.tagmarshal.golf.fragment.cortshop.kitchen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.util.TMUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KitchenFragment extends BaseFragment {

    @BindView(R.id.top_margin)
    View topMargin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kitchen_closed, container, false);
        ButterKnife.bind(this, view);
        topMargin.getLayoutParams().height = TMUtil.getTopMargin();
        return view;
    }

    @OnClick(R.id.close_ok_btn)
    void onOkClick() {
        getBaseActivity().onBackPressed();
    }
}
