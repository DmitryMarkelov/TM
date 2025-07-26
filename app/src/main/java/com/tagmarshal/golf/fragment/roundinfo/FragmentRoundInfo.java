package com.tagmarshal.golf.fragment.roundinfo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentRoundInfo extends BaseFragment implements FragmentRoundInfoContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.pace_info_time_tv)
    TMTextView mPaceInfo;

    @BindView(R.id.current_round_time_time_tv)
    TMTextView mCurrentRoundTime;

    @BindView(R.id.target_round_time_time_tv)
    TMTextView mTargetTime;

    @BindView(R.id.offline_time)
    TMTextView offlineTv;

    Unbinder unbinder;

    private FragmentRoundInfoContract.Presenter presenter;


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static FragmentRoundInfo getInstance(@Nullable Date offlineDate) {
        FragmentRoundInfo fragmentRoundInfo = new FragmentRoundInfo();
        Bundle args = new Bundle();
        if (offlineDate != null)
            args.putString("date", TMUtil.getHourTimeFromSecs(offlineDate));

        fragmentRoundInfo.setArguments(args);

        return fragmentRoundInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round_info, container, false);

        unbinder = ButterKnife.bind(this, view);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        presenter = new FragmentRoundInfoPresenter(this);
        EventBus.getDefault().register(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.getRoundInfo();
        if (getArguments().getString("date") != null) {
            offlineTv.setText("Device synchronizing. Status as at " + getArguments().getString("date"));
            offlineTv.setVisibility(View.VISIBLE);
        } else {
            offlineTv.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.ok)
    void onOkClick() {
        getActivity().onBackPressed();
    }

    @Override
    public void onGetRoundInfo(RestInRoundModel roundModel) {
        String paceInfoText = null;

        if (roundModel.getPace() >= 0) {
            paceInfoText = getString(R.string.on_pace);
        } else if (roundModel.isHeldUp()) {
            paceInfoText = getString(R.string.delayed);
        } else if (roundModel.isHoldingUp()) {
            paceInfoText = getString(R.string.delayer);
        } else {
            paceInfoText = getString(R.string.slow);

        }

        if (roundModel.getPace() == 0) {
            mPaceInfo.setText(getString(R.string.screen_round_info_pace_info,
                    String.valueOf(0),
                    paceInfoText));
        } else {
            mPaceInfo.setText(getString(R.string.screen_round_info_pace_info,
                    String.valueOf(TMUtil.covertSecToMinutes(Math.abs(roundModel.getPace()))),
                    paceInfoText));
        }
        mCurrentRoundTime.setText(TMUtil.getHourAndSecondsTimeFromSecs(roundModel.getPlayTime()));
        mTargetTime.setText(roundModel.getGoalTime());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoundInfoGot(RoundInfoEvent roundModel) {
        onGetRoundInfo(roundModel.getRoundModel());
        EventBus.getDefault().removeStickyEvent(roundModel);
    }


    @Override
    public void showLoadingDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onGetRoundInfoFailure(String message) {

    }
}
