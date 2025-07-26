package com.tagmarshal.golf.fragment.roundend;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.maps.android.PolyUtil;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.dialog.GolfSendMessageDialog;
import com.tagmarshal.golf.eventbus.EventAdminScreenEntered;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.alert.FragmentAlert;
import com.tagmarshal.golf.fragment.groups.pager.FragmentGroupsPager;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.manager.GameManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.DataEntryStatus;
import com.tagmarshal.golf.rest.model.Disclaimer;
import com.tagmarshal.golf.rest.model.DisclaimerStatus;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestGeoZoneModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMButton;
import com.tagmarshal.golf.view.TMTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentRoundEnd extends BaseFragment implements FragmentRoundEndContract.View {

    @BindView(R.id.top_element)
    View mTopMargin;

    @BindView(R.id.round_time)
    TMTextView mRoundTime;

    @BindView(R.id.time_tv)
    TMTextView mStartTime;

    @BindView(R.id.progress)
    ProgressBar mProgress;

    @BindView(R.id.holes_tv)
    TMTextView mHoles;

    @BindView(R.id.left_tv)
    TMTextView mLeft;

    @BindView(R.id.btn_back)
    TMButton mBackBtn;

    @BindView(R.id.right_tv)
    TMTextView mRight;

    @BindView(R.id.group_tv)
    TMTextView groupTv;

    @BindView(R.id.group_number)
    TMTextView mTag;

    private RestInRoundModel roundModel;

    private FragmentRoundEndContract.Presenter presenter;

    Unbinder unbinder;
    private boolean roundModelIsNull = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round_end, container, false);

        unbinder = ButterKnife.bind(this, view);

        EventBus.getDefault().register(this);

        presenter = new FragmentRoundEndPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        mTag.setText(PreferenceManager.getInstance().getDeviceTag());
        GradientDrawable bgShape = (GradientDrawable) mTag.getBackground();
        bgShape.setColor(ContextCompat.getColor(requireContext(), R.color.green));

        if (PreferenceManager.getInstance().isDeviceMobile()) {
            groupTv.setText(getString(R.string.tag));
        } else {
            groupTv.setText(getString(R.string.cart));
        }

        if (roundModel != null) {
            mRoundTime.setText(TMUtil.getHourTimeFromSecs(roundModel.getPlayTime()));
            mStartTime.setText(roundModel.getStartTime());
            mHoles.setText(getString(R.string.holes_number, roundModel.isNineHole() ? "9" : "18"));
            mProgress.setProgress(roundModel.isNineHole() ? 9 : 18);
            if (!roundModel.isNineHole()) {
                GameManager.setCurrentPlayHole(0);
            }

            int seconds = 900;
            if (roundModel.isNineHole() && PreferenceManager.getInstance().getMapBounds() != null) {
                seconds = PreferenceManager.getInstance().getMapBounds().getTurnTime();
            }
            presenter.startRoundInfoDismiss(seconds);

            mLeft.setText(getString(R.string.left_time_pace, TMUtil.covertSecToMinutes(Math.abs(roundModel.getPace()))));

            if (roundModel.isNineHole()) {
                mBackBtn.setText(getString(R.string.back_to_round));
            } else {
                mBackBtn.setText(getString(R.string.back));
            }

            if (roundModel != null && roundModel.isComplete() && !roundModel.isNineHole()) {
                getBaseActivity().setMapEnabled(false);
                getBaseActivity().setGroupInfoEnabled(false);
                getBaseActivity().setEmergencyEnabled(true);
                getBaseActivity().setContactUsEnabled(true);

                Disclaimer disclaimer = PreferenceManager.getInstance().getDisclaimer();
                if (disclaimer != null) {
                    disclaimer.setLastShownTime(null);
                    disclaimer.setStatus(DisclaimerStatus.NONE);
                    disclaimer.setDataEntryStatus(DataEntryStatus.NONE);
                    PreferenceManager.getInstance().setDisclaimer(disclaimer);
                }
            }
        }
        presenter.startChargeCheckTimer(getContext());

        getBaseActivity().loadClubLogo();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int color = 0;

        if (roundModel != null) {
            if (roundModel.getPace() >= 0) {
                color = ContextCompat.getColor(getContext(), R.color.green);
                mRight.setText(getString(R.string.on_pace));
            } else if (roundModel.isHeldUp()) {
                color = ContextCompat.getColor(getContext(), R.color.orange);
                mRight.setText(getString(R.string.delayed));
            } else if (roundModel.isHoldingUp()) {
                color = ContextCompat.getColor(getContext(), R.color.magenta);
                mRight.setText(getString(R.string.delayer));
            } else {
                color = ContextCompat.getColor(getContext(), R.color.red);
                mRight.setText(getString(R.string.slow));
            }

            mProgress.getProgressDrawable().setColorFilter(color,
                    PorterDuff.Mode.SRC_IN);

            presenter.startGettingRoundInfo();

            if (!roundModel.isNineHole()) {
                if (PreferenceManager.getInstance().isDeviceMobile() &&
                        PreferenceManager.getInstance().getReturnDeviceStatus()) {
                    presenter.startShowingReturnDevicePopup();
                }
                presenter.startPlugInInterval();
            }
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        presenter.onDestroy();
        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    public void bindRoundModel(RestInRoundModel roundModel) {
        this.roundModel = roundModel;
    }

    @OnClick(R.id.btn_back)
    void onOkClick() {
        if (roundModel == null) {
            getBaseActivity().onBackPressed();
        }

        if (!PreferenceManager.getInstance().isDeviceMobile() && !roundModel.isNineHole()) {
            getBaseActivity().backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
            PreferenceManager.getInstance().setRatedRound(true);
            return;
        }
        if (!roundModel.isNineHole() && roundModel.isComplete() && !checkIsInSection() && PreferenceManager.getInstance().getReturnDeviceStatus()) {
            onShowReturnDeviceScreen();
        } else if ((!roundModel.isNineHole() && roundModel.isComplete() && !checkIsInSection() && !PreferenceManager.getInstance().getReturnDeviceStatus())) {
            getBaseActivity().backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
            PreferenceManager.getInstance().setRatedRound(true);
        } else if (checkIsInSection() && !roundModel.isNineHole()) {
            getBaseActivity().backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
            PreferenceManager.getInstance().setRatedRound(true);
        } else {
            getBaseActivity().onBackPressed();
        }
    }

    private boolean checkIsInSection() {
        try {
            if (getGolfService() == null || getGolfService().getLastKnownMyLocationLatLng() == null)
                return false;

            for (RestGeoZoneModel model : PreferenceManager.getInstance().getGeoZones()) {
                if (model != null
                        && PolyUtil.containsLocation(getGolfService().getLastKnownMyLocationLatLng().latitude,
                        getGolfService().getLastKnownMyLocationLatLng().longitude,
                        model.getGeometry().getPolygon().getPoints(), false)
                        && Integer.valueOf(model.getProperties().getHole()) > 0) {
                    Log.d(getClass().getName(), "You're in section now!");
                    return true;
                }

            }
            return false;
        } catch (Throwable throwable) {
            return false;
        }
    }

    private void showReturnDeviceScreen() {
        final FragmentAlert fragmentAlert = new FragmentAlert();
        fragmentAlert.setOkText(getString(R.string.ok_no_problem));
        fragmentAlert.setWhatText(getString(R.string.request_assistance));

        RestAlertModel alertModel = new RestAlertModel();
        alertModel.setMessage(getString(R.string.please_remember_to_return_device));
        alertModel.setImage(getString(R.string.return_device));

        fragmentAlert.bindAlert(alertModel);

        fragmentAlert.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
            }

            @Override
            public void onBottomBtnClick() {
                GolfSendMessageDialog messageDialog = new GolfSendMessageDialog(getContext());
                messageDialog.show();
            }
        });

        getBaseActivity().addFragment(fragmentAlert);
    }

    @Override
    public void dismissRoundInfo() {
        onOkClick();
    }

    @Override
    public void onGetRoundInfo(RestInRoundModel inRoundModel) {
        if (inRoundModel.isNullModel()) {
            roundModelIsNull = inRoundModel.isNullModel();
        } else if (inRoundModel.isInRound()) {
            getBaseActivity().backToRootFragmentOrAddRootFragmentIfNotExists(FragmentMap.class);
        } else {
            roundModelIsNull = false;
        }
    }

    @Override
    public void onShowReturnDevicePopup(boolean canMakeNoise) {
        if (!checkIsInSection()) {
            if (canMakeNoise)
                TMUtil.vibrateAndMakeNoiseDevice(requireContext());

            GolfAlertDialog dialog = new GolfAlertDialog(getActivity())
                    .setBottomBtnText(getString(R.string.pls_assist_me))
                    .setCancelable(false)
                    .setOkText(getString(R.string.ok_no_problem))
                    .setIcon(R.drawable.ic_info)
                    .setMessage(getString(R.string.please_remember_to_return_device))
                    .setTitle(getString(R.string.reminder));

            dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                @Override
                public void onOkClick() {

                }

                @Override
                public void onBottomBtnClick() {
                    new GolfSendMessageDialog(getContext()).show();
                }
            });

            dialog.show();
        }
    }

    @Override
    public void goToGroupPageOrBackToRoot() {
        if (roundModelIsNull) {
            clearAndAddRootFragment(new FragmentGroupsPager());
        } else if (getBaseActivity().getSupportFragmentManager().findFragmentByTag(FragmentAlert.class.getName()) != null &&
                getBaseActivity().getSupportFragmentManager().findFragmentByTag(FragmentAlert.class.getName()).isVisible()) {
            backToRootFragment();
        }
    }

    @Override
    public void onShowReturnDeviceScreen() {
//        TMUtil.vibrateAndMakeNoiseDevice(Objects.requireNonNull(getContext()));
        if (isVisible() && !checkIsInSection() && !TMUtil.isCharging(getContext()) && PreferenceManager.getInstance().isDeviceMobile()) {
            showReturnDeviceScreen();
            Map<String, String> body = new HashMap<>();
            body.put("message", "The handheld device has not been returned 15 minutes after round ended.");
            getBaseActivity().startTimerSendToClubNotification(body);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdminScreenEntered(EventAdminScreenEntered event) {
        presenter.stopShowingReturnDeviceNotifications();
    }

    @Override
    public void disableIntervals() {
        presenter.stopShowingReturnDeviceNotifications();
    }
}
