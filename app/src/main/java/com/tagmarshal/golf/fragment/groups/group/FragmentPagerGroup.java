package com.tagmarshal.golf.fragment.groups.group;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.devicesetup.FragmentDeviceSetup;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.fragment.roundstart.FragmentStartOfRound;
import com.tagmarshal.golf.manager.GameManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentPagerGroup extends BaseFragment implements FragmentPagerGroupContract.View {

    @BindView(R.id.top_element)
    View mTopElement;

    @BindView(R.id.first_player)
    TMTextView mPlayer1;

    @BindView(R.id.second_player)
    TMTextView mPlayer2;

    @BindView(R.id.third_player)
    TMTextView mPlayer3;

    @BindView(R.id.fourth_player)
    TMTextView mPlayer4;

    @BindView(R.id.hole_number)
    TMTextView mHoleNumber;

    @BindView(R.id.start_date)
    TMTextView mStartDate;

    private GolfCallback.OnGroupSwipePageListener onSwipePage;

    private RestTeeTimesModel group;

    private GolfCallback.OnGroupPagerRefreshListener onGroupPagerRefreshListener;

    private FragmentPagerGroupContract.Presenter presenter;

    GolfAlertDialog dialog;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentPagerGroupPresenter(this);

        GameManager.setCurrentPlayHole(0);
        mTopElement.getLayoutParams().height = TMUtil.getTopMargin();

        return view;
    }

    public void bindSwipeChangeListener(GolfCallback.OnGroupSwipePageListener onSwipePage) {
        this.onSwipePage = onSwipePage;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(group != null) {
            mPlayer1.setText(Html.fromHtml("<b>1. </b>" + group.getPlayer1()));
            mPlayer2.setText(Html.fromHtml("<b>2. </b>" + group.getPlayer2()));
            mPlayer3.setText(Html.fromHtml("<b>3. </b>" + group.getPlayer3()));
            mPlayer4.setText(Html.fromHtml("<b>4. </b>" + group.getPlayer4()));
            mHoleNumber.setText(getString(R.string.hole_number, group.getHole()));
            mStartDate.setText(group.getTeeTime());
        } else {
            mPlayer1.setText(Html.fromHtml("<b>1. </b>"));
            mPlayer2.setText(Html.fromHtml("<b>2. </b>"));
            mPlayer3.setText(Html.fromHtml("<b>3. </b>"));
            mPlayer4.setText(Html.fromHtml("<b>4. </b>"));
            mHoleNumber.setText(getString(R.string.hole_number, "1"));
            mStartDate.setText("--:--");
        }
    }

    public void bindOnRefreshGroupsListener(GolfCallback.OnGroupPagerRefreshListener onGroupPagerRefreshListener) {
        this.onGroupPagerRefreshListener = onGroupPagerRefreshListener;
    }

    public void bindGroup(RestTeeTimesModel model) {
        this.group = model;
    }

    @Override
    public void onDestroyView() {
        if(dialog!=null){
            dialog.dissmiss();
        }
        presenter.onDestroy();
        presenter = null;

        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @OnClick(R.id.btn_left)
    void onLeftClick() {
        if (onSwipePage != null) {
            onSwipePage.onPrevPage();
        }
    }

    @OnClick(R.id.btn_right)
    void onRightClick() {
        if (onSwipePage != null) {
            onSwipePage.onNextPage();
        }
    }

    @OnClick(R.id.btn_correct)
    void onCorrectClick() {
        dialog = new GolfAlertDialog(requireContext());
        dialog.setTitle(getString(R.string.confirm_tee_time));
        dialog.setMessage(getString(R.string.tee_confirm_message));
        dialog.setOkText(getString(R.string.confirm));
        dialog.setBottomBtnText(getString(R.string.cancel));
        dialog.setCancelable(false);
        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                presenter.confirmOrRemoveGroup(group.getId(), "assign");
                dialog.dissmiss();
            }

            @Override
            public void onBottomBtnClick() {
                dialog.dissmiss();
            }
        });

        dialog.show();

    }

    @OnClick(R.id.btn_refresh)
    void onRefreshGroupsClick() {
        if (onGroupPagerRefreshListener != null) {
            onGroupPagerRefreshListener.onRefreshGroupsClick();
        }
    }

    @OnClick(R.id.btn_skip)
    void onBackClick() {
        GolfAlertDialog golfAlertDialog = new GolfAlertDialog(getActivity());
        golfAlertDialog.setCancelable(false);
        golfAlertDialog.setMessage(getString(R.string.about_to_skip_message));
        golfAlertDialog.setOkText(getString(R.string.okay));
        golfAlertDialog.setIcon(R.drawable.ic_danger_small);
        golfAlertDialog.setTitle(getString(R.string.about_to_skip));
        golfAlertDialog.setBottomBtnText(getString(R.string.cancel));
        golfAlertDialog.setAllCaps(true);

        golfAlertDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                onSkipMessageSuccess();
            }

            @Override
            public void onBottomBtnClick() {
                golfAlertDialog.dissmiss();
            }
        });

        golfAlertDialog.show();
    }



    @Override
    public void onSkipMessageSuccess(){
        if (isDetached()) return;

        clearAndAddRootFragment(new FragmentMap());
    }
    @Override
    public void onConfirmationSuccess() {
        PreferenceManager.getInstance().setCourseTeeStartTime(group.getTeeTime());
        PreferenceManager.getInstance().setCourseGroupId(group.getId());
        PreferenceManager.getInstance().setStartHole(Integer.valueOf(group.getHole()));

        if (!PreferenceManager.getInstance().isDeviceVariable()) {
            if (TMUtil.getLeftMinutes(group.getTeeTime()) > 0) {
                FragmentStartOfRound fragmentStartOfRound = new FragmentStartOfRound();
                fragmentStartOfRound.setTag(PreferenceManager.getInstance().getDeviceTag());
                fragmentStartOfRound.setHole(group.getHole());
                fragmentStartOfRound.setStartTime(group.getTeeTime());

                clearAndAddRootFragment(fragmentStartOfRound);
            } else {
                clearAndAddRootFragment(new FragmentMap());
            }
        } else {
            clearAndAddRootFragment(new FragmentDeviceSetup());
        }

    }

    @Override
    public void onConfirmationFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }



    @Override
    public void showWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }
}
