package com.tagmarshal.golf.fragment.roundrate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.roundend.FragmentRoundEnd;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.SnapToPlayHelper;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.RateRoundView;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RoundRateFragment extends BaseFragment implements RateRoundView.RateRoundListener,
        RoundRateFragmentContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.rate_bar)
    RateRoundView mRateRoundBar;

    @BindView(R.id.input_message_field)
    ConstraintLayout mInputMessageField;

    @BindView(R.id.question_tv)
    TMTextView mQuestionTextView;

    @BindView(R.id.rate_tv)
    TMTextView mRateRaundTv;

    @BindView(R.id.input_message)
    EditText mInputMessage;

    private RoundRateFragmentPresenter presenter;

    private RestInRoundModel model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rate_round, container, false);

        ButterKnife.bind(this, view);

        presenter = new RoundRateFragmentPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        mRateRoundBar.setOnRateRoundListener(this);

        if (model != null && model.isNineHole()) {
            mRateRaundTv.setText(R.string.rate_your_round_so_far);
        } else {
            mRateRaundTv.setText(R.string.rate_your_round);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (presenter != null) {
            presenter.closeTimer();
        }
    }

    public void bindRoundModel(RestInRoundModel model) {
        this.model = model;
    }

    @Override
    public void onRate(int stars) {
        try {
            mInputMessageField.setVisibility(View.VISIBLE);
            mQuestionTextView.setVisibility(View.VISIBLE);

            if (stars > 3) {
                mQuestionTextView.setText(R.string.awesome_what_do_you_love_the_best);
            } else {
                mQuestionTextView.setText(R.string.please_tell_us_how_we_can_do_better);
            }
        } catch (Throwable e) {
            Toast.makeText(requireContext(), "Happened exception during the rate", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.cancel)
    void onCancelClick() {
        openEndOfRoundScreen();
    }

    @OnClick(R.id.submit)
    void onSubmitClick() {
        if (mRateRoundBar.getStars() != 0) {
            getBaseActivity().showPleaseWaitDialog(true);
            if (mRateRoundBar.getStars() <= 2 && presenter != null) {
                presenter.sendBadRound(TMUtil.getDeviceIMEI());
            } else {
                rateRound();
            }
        }
    }

    @Override
    public void onSuccessBadRating() {
        rateRound();
    }

    private void rateRound() {
        if (presenter != null) {
            presenter.rateRound(model.getId(),
                    model.isNineHole() ? "halfway" : "full",
                    mRateRoundBar.getStars(),
                    !mInputMessage.getText().toString().isEmpty() ? mInputMessage.getText().toString() : " ");
        }
    }

    @Override
    public void openEndOfRoundScreen() {

        //Shutdown SnapToPlaySpeakers and Clear the Bluetooth Audio ID
        SnapToPlayHelper.turnSnapToPlayPowerOff();
        PreferenceManager.getInstance().setAudioID("");

        if (model != null) {
            FragmentRoundEnd fragment = new FragmentRoundEnd();
            fragment.bindRoundModel(model);

            if (!model.isNineHole()) {
                clearAndAddRootFragment(fragment);
            } else {
                replaceThisFragment(this, fragment);
            }
        } else {
            getBaseActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onRateSuccess() {
        getBaseActivity().showPleaseWaitDialog(false);
        openEndOfRoundScreen();
    }

    @Override
    public void onRateFailure(String message) {
        getBaseActivity().showPleaseWaitDialog(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;

        super.onDestroyView();
    }
}
