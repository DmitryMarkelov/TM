package com.tagmarshal.golf.fragment.waiver;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.dataentry.FragmentDataEntry;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.DataEntryStatus;
import com.tagmarshal.golf.rest.model.Disclaimer;
import com.tagmarshal.golf.rest.model.DisclaimerStatus;
import com.tagmarshal.golf.util.TMUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentWaiver extends BaseFragment implements FragmentWaiverContract.View {

    @BindView(R.id.waiver_text)
    TextView waiverText;

    @BindView(R.id.agreeButton)
    Button AgreeButton;

    @BindView(R.id.cancelButton)
    Button cancelButton;

    @BindView(R.id.top_margin)
    View mTopMargin;

    private FragmentWaiverPresenter fragmentWaiverPresenter;
    private Disclaimer mDisclaimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull @io.reactivex.annotations.NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waiver, container, false);
        ButterKnife.bind(this, view);
        mDisclaimer = PreferenceManager.getInstance().getDisclaimer();
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        fragmentWaiverPresenter = new FragmentWaiverPresenter(this);
        fragmentWaiverPresenter.getWaiver();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @io.reactivex.annotations.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Handle back press in this fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mDisclaimer.setStatus(DisclaimerStatus.DISMISSED);
                mDisclaimer.setLastShownTime(System.currentTimeMillis());
                updateDisclaimer(mDisclaimer);
                popFragment();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    @OnClick(R.id.cancelButton)
    void onCancel() {
        mDisclaimer.setStatus(DisclaimerStatus.DISMISSED);
        mDisclaimer.setLastShownTime(System.currentTimeMillis());
        updateDisclaimer(mDisclaimer);
        popFragment();
    }

    @OnClick(R.id.agreeButton)
    void onAgree() {
        mDisclaimer.setStatus(DisclaimerStatus.ACCEPTED);
        mDisclaimer.setLastShownTime(System.currentTimeMillis());
        updateDisclaimer(mDisclaimer);
        fragmentWaiverPresenter.onAgree();
        Toast.makeText(getContext(), "Waiver Successfully Submitted", Toast.LENGTH_LONG).show();
        popFragment();

//        if (mDisclaimer.getDataEntryStatus() != DataEntryStatus.SUBMITTED) {
//            addFragment(this, new FragmentDataEntry(), true, FragmentDataEntry.class.getName());
//        }
    }

    private void updateDisclaimer(Disclaimer disclaimer) {
        PreferenceManager.getInstance().setDisclaimer(disclaimer);
    }

    @Override
    public void showWaiver(Disclaimer disclaimer) {
        try {
            mDisclaimer = disclaimer;
            waiverText.setText(Html.fromHtml(disclaimer.getText()));
        } catch (Exception e) {
            Log.d("GEOGT", "Error in waiver text: " + e.getMessage());
        }
    }
}
