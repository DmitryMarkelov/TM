package com.tagmarshal.golf.fragment.refreshments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.util.TMUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentRefreshments extends BaseFragment {

    @BindView(R.id.top_margin)
    View mTopMargin;

    Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refreshments, container, false);

        unbinder = ButterKnife.bind(this, view);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    @OnClick(R.id.cancel)
    void onCancelClick() {
        getActivity().onBackPressed();
    }
}
