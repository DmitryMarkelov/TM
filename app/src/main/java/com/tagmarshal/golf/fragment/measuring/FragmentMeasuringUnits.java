package com.tagmarshal.golf.fragment.measuring;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.GolfSwitchView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentMeasuringUnits extends BaseFragment {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.metric_switch)
    GolfSwitchView mMetricSwitch;

    Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measuring_units, container, false);

        unbinder = ButterKnife.bind(this, view);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        if (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER)) {
            mMetricSwitch.setChecked(1);
        } else if (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_YARD)) {
            mMetricSwitch.setChecked(0);
        }

        return view;
    }

    @OnClick(R.id.confirm)
    void onConfirmClick() {
        PreferenceManager.getInstance().setDeviceUnitMetric(mMetricSwitch.getChecked() == 0 ?
                PreferenceManager.UNIT_METRIC_YARD : PreferenceManager.UNIT_METRIC_METER);
        getBaseActivity().onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }

        super.onDestroy();
    }
}
