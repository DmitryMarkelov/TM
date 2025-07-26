package com.tagmarshal.golf.fragment.emergency;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.util.TMUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentEmergency extends BaseFragment implements FragmentEmergencyContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.medical_assistance_checkbox)
    CheckBox mMedicalCheckBox;

    @BindView(R.id.other_checkbox)
    CheckBox mOtherCheckBoz;

    @BindView(R.id.group_safety_checkbox)
    CheckBox mGroupSafetyCheckbox;

    @BindView(R.id.group_safety_tv)
    TextView mGroupSafetyTv;

    private Unbinder unbinder;

    private static final String groupSafetyBundle = "group_safety";

    private FragmentEmergencyPresenter presenter;

    public static FragmentEmergency getInstance(boolean groupSafety) {
        FragmentEmergency fragment = new FragmentEmergency();

        Bundle args = new Bundle();
        args.putBoolean(groupSafetyBundle, groupSafety);

        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentEmergencyPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();




        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean groupsEnabled = getArguments().getBoolean(groupSafetyBundle);
        if(groupsEnabled){
            setViewEnabled(mGroupSafetyCheckbox, true);
            mGroupSafetyCheckbox.setChecked(false);
            mGroupSafetyCheckbox.setVisibility(View.VISIBLE);
            mGroupSafetyTv.setVisibility(View.VISIBLE);
        }else {
            setViewEnabled(mGroupSafetyCheckbox, false);
            mGroupSafetyTv.setVisibility(View.GONE);
            mGroupSafetyCheckbox.setVisibility(View.GONE);
            mGroupSafetyCheckbox.setChecked(false);
        }
    }

    private void setViewEnabled(View view, boolean enabled) {
        view.setClickable(enabled);
        view.setFocusable(enabled);
        view.setEnabled(enabled);
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;

        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    @OnClick(R.id.cancel)
    void onCancelClick() {
        getActivity().onBackPressed();
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.submit)
    void onSubmitClick() {
        getBaseActivity().showPleaseWaitDialog(true);

        String message = "";
        if (mMedicalCheckBox.isChecked()) {
            message += (message.length() == 0 ? "" : ", ") + getString(R.string.message_medical_assistance);
        }
        if (mGroupSafetyCheckbox.isChecked()) {
            message += (message.length() == 0 ? "" : ", ") + getString(R.string.message_group_safety);
        }
        if (mOtherCheckBoz.isChecked()) {
            message += (message.length() == 0 ? "" : ", ") + getString(R.string.message_other);
        }

        if (message.length() == 0) {
            getBaseActivity().showPleaseWaitDialog(false);
            Toast.makeText(getContext(), getString(R.string.choose_at_least_one_of_the_types), Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("message", message);

        presenter.sendMessage(body);
    }

    @Override
    public void showWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }

    @Override
    public void onMessageSent() {
        Toast.makeText(GolfApplication.context,
                GolfApplication.context.getString(R.string.message_sent),
                Toast.LENGTH_LONG).show();

        getBaseActivity().onBackPressed();
    }

    @Override
    public void onRequestFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
