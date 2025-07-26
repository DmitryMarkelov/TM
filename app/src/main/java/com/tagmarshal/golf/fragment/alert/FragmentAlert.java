package com.tagmarshal.golf.fragment.alert;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMButton;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentAlert extends BaseFragment implements FragmentAlertContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.title)
    TMTextView mTitle;

    @BindView(R.id.info)
    TMTextView mMessage;

    @BindView(R.id.image)
    ImageView mIcon;

    @BindView(R.id.ok)
    TMButton mOk;

    @BindView(R.id.what)
    TMButton mWhat;
    @BindView(R.id.alert_background)
    ConstraintLayout mBackground;

    private RestAlertModel alertModel;

    private String okText, whatText = "";

    private GolfCallback.GolfDialogListener dialogListener;

    private FragmentAlertPresenter presenter;


    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);
        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentAlertPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        updateView();
        return view;
    }

    void updateView() {
        if (alertModel != null && alertModel.getImage() != null) {
            mTitle.setText(alertModel.getImage());
        }
        if (alertModel != null && alertModel.getMessage() != null) {
            mMessage.setText(alertModel.getMessage());
        }


        mOk.setText(okText);
        mWhat.setText(whatText);

        mWhat.setVisibility(alertModel != null && alertModel.isVisible() ? View.VISIBLE : View.GONE);

        if (alertModel != null && alertModel.getImage() != null && alertModel.getImage().equals(getString(R.string.geo_fence_alert))) {
            ConstraintSet set = new ConstraintSet();
            set.clone(mBackground);
            set.constrainPercentWidth(R.id.what, 0.4f);
            set.constrainPercentWidth(R.id.ok, 0.6f);
            set.connect(R.id.what, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(R.id.what, ConstraintSet.END, R.id.ok, ConstraintSet.START);
            set.connect(R.id.ok, ConstraintSet.START, R.id.what, ConstraintSet.END, 8);
            set.connect(R.id.ok, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.connect(R.id.ok, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
            set.applyTo(mBackground);
            mWhat.setText(getString(R.string.assist_me));
        }
        if (alertModel != null && alertModel.getImage() != null && alertModel.getImage().equals(getString(R.string.return_device))) {
            presenter.startMakingNotification();
            mBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
        } else if (alertModel != null && alertModel.getImage() != null) {
            if(alertModel.isSound()) {
                makeNotification();
                mBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
            } else {
                mBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
            }

            switch (alertModel.getImage()) {
                case "Weather Alert": {
                    mIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_weather_alert));
                    break;
                }
                case "General Message": {
                    mIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_alert_general_message));
                    break;
                }
                case "Pace Warning": {
                    mIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning_alert));
                    break;
                }
                case "Safety Alert": {
                    mIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_safety_alert));
                    break;
                }
                default: {
                    mIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_danger));
                    break;
                }
            }
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getBaseActivity()!=null){
            getBaseActivity().deviceInteraction();
        }
    }

    public void bindAlert(RestAlertModel restAlertModel) {
        this.alertModel = restAlertModel;
    }

    public void updateAlert(RestAlertModel restAlertModel) {
        this.alertModel = restAlertModel;
        updateView();
    }

    public void setOnDialogListener(GolfCallback.GolfDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public void setOkText(String okText) {
        this.okText = okText;
    }

    public void setWhatText(String whatText) {
        this.whatText = whatText;
    }

    @OnClick(R.id.ok)
    void onOkClick() {
        if (isAdded()) {
            presenter.stopMakingNotification();
            getBaseActivity().getSupportFragmentManager().popBackStack();
            if (dialogListener != null) {
                dialogListener.onOkClick();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (isAdded()) {
            super.onHiddenChanged(hidden);
            if (alertModel != null && alertModel.getImage() != null && alertModel.getImage().equals(getString(R.string.return_device))) {
                if (hidden && presenter != null) {
                    presenter.stopMakingNotification();
                } else if (presenter != null) {
                    presenter.startMakingNotification();
                }
            }
        }
    }

    @OnClick(R.id.what)
    void onWhatClick() {
        if (dialogListener != null) {
            dialogListener.onBottomBtnClick();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (alertModel != null && alertModel.getImage() != null && (alertModel.getImage().equals(getString(R.string.return_device)) ||
                (alertModel.getImage().equals(getString(R.string.geo_fence_alert)) && alertModel.isSound()))) {
            presenter.startMakingNotification();
        }
    }


    @Override
    public void onPause() {
        presenter.stopMakingNotification();
        super.onPause();

    }


    @Override
    public void onDestroyView() {
        presenter.onDestroy();

        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @Override
    public void goBack() {
        if(presenter != null)
            presenter.stopMakingNotification();
        else
            return; // do not dismiss notification so user can click ok

        if(getBaseActivity() != null)
            getBaseActivity().onBackPressed();
    }

    @Override
    public void makeNotification() {
        TMUtil.vibrateAndMakeNoiseDevice(requireContext());
    }
}
