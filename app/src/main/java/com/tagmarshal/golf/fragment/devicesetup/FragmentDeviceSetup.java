package com.tagmarshal.golf.fragment.devicesetup;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.fragment.roundstart.FragmentStartOfRound;
import com.tagmarshal.golf.fragment.selectcourse.FragmentSelectCourse;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.GolfSwitchView;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentDeviceSetup extends BaseFragment implements FragmentDeviceSetupContract.View {

    @BindView(R.id.top_margin)
    View mTop;

    @BindView(R.id.type_switch)
    GolfSwitchView mTypeSwitch;

    @BindView(R.id.metric_switch)
    GolfSwitchView mMetricSwitch;

    @BindView(R.id.unit_metric_tv)
    TMTextView mUnitMetricTV;

    Unbinder unbinder;

    private FragmentDeviceSetupPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setup, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentDeviceSetupPresenter(this);

        if (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_METER)) {
            mMetricSwitch.setChecked(1);
        } else if (PreferenceManager.getInstance().getDeviceUnitMetric().equals(PreferenceManager.UNIT_METRIC_YARD)) {
            mMetricSwitch.setChecked(0);
        }

        if (PreferenceManager.getInstance().isFirstLaunch()) {
            mTypeSwitch.setVars(3);
        } else {
            mTypeSwitch.setVars(2);
            mMetricSwitch.setVisibility(View.GONE);
            mUnitMetricTV.setVisibility(View.GONE);
        }

        if (PreferenceManager.getInstance().getDeviceType().equals(PreferenceManager.DEVICE_TYPE_CART)) {
            mTypeSwitch.setChecked(1);
        } else if (PreferenceManager.getInstance().getDeviceType().equals(PreferenceManager.DEVICE_TYPE_MOBILE)) {
            mTypeSwitch.setChecked(0);
        }

        mTop.getLayoutParams().height = TMUtil.getTopMargin();

        getBaseActivity().setContactUsEnabled(false);
        getBaseActivity().setEmergencyEnabled(false);
        getBaseActivity().setMapEnabled(false);
        getBaseActivity().setSpeakersEnabled(false);
        getBaseActivity().setGroupInfoEnabled(false);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getBaseActivity().setKeepScreenOn(false);
    }

    @OnClick(R.id.confirm)
    void onConfirmClick() {
        switch (mTypeSwitch.getChecked()) {
            case 0:
                PreferenceManager.getInstance().setDeviceType(PreferenceManager.DEVICE_TYPE_MOBILE);
                break;
            case 1:
                PreferenceManager.getInstance().setDeviceType(PreferenceManager.DEVICE_TYPE_CART);
                break;
            case 2:
                PreferenceManager.getInstance().setDeviceVariable(true);
                break;
        }

        PreferenceManager.getInstance().setDeviceUnitMetric(mMetricSwitch.getChecked() == 0 ?
                PreferenceManager.UNIT_METRIC_YARD : PreferenceManager.UNIT_METRIC_METER);

        if (PreferenceManager.getInstance().isFirstLaunch()) {
            clearAndAddRootFragment(FragmentSelectCourse.newInstance(false));
            PreferenceManager.getInstance().setFirstLaunch(false);
        } else {
            presenter.registerVariableDevice(TMUtil.getDeviceIMEI(), PreferenceManager.getInstance().getDeviceType(), Build.DISPLAY);
        }
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;
        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    @Override
    public void onVariableRegistrationSuccess(String tag) {
        PreferenceManager.getInstance().setDeviceTag(tag, PreferenceManager.getInstance().getCourse());

        if (TMUtil.getLeftMinutes(PreferenceManager.getInstance().getCourseTeeStartTime()) > 0) {
            FragmentStartOfRound fragmentStartOfRound = new FragmentStartOfRound();
            fragmentStartOfRound.setTag(PreferenceManager.getInstance().getDeviceTag());
            fragmentStartOfRound.setHole(String.valueOf(PreferenceManager.getInstance().getStartHole()));
            fragmentStartOfRound.setStartTime(PreferenceManager.getInstance().getCourseTeeStartTime());

            clearAndAddRootFragment(fragmentStartOfRound);
        } else {
            clearAndAddRootFragment(new FragmentMap());
        }
    }

    @Override
    public void onVariableRegistrationFailure(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }
}
