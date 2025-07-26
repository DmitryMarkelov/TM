package com.tagmarshal.golf.fragment.group;

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
import com.tagmarshal.golf.view.TMButton;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentGroup extends BaseFragment implements FragmentGroupContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

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

    @BindView(R.id.btn_skip)
    TMButton btnSkip;

    private Unbinder unbinder;
    private boolean isSameGroup = false;

    private GolfCallback.OnGroupPagerRefreshListener onGroupPagerRefreshListener;

    private FragmentGroupContract.Presenter presenter;

    private RestTeeTimesModel group;

    private GolfAlertDialog sameGroupDialog;
    private GolfAlertDialog notSameDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_names, container, false);

        unbinder = ButterKnife.bind(this, view);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        presenter = new FragmentGroupPresenter(this);

        isSameGroup = PreferenceManager.getInstance().getCourseGroupId() != null && group.getId().equals(PreferenceManager.getInstance().getCourseGroupId());
        GameManager.setCurrentPlayHole(0);
        if (isSameGroup) {
            btnSkip.setText(getString(R.string.change_group));
        } else {
            btnSkip.setText(getString(R.string.skip));
        }

        initGroup(group);

        return view;
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;

        if (sameGroupDialog != null && sameGroupDialog.isShowing()) {
            sameGroupDialog.dissmiss();
        }
        if (notSameDialog != null && notSameDialog.isShowing()) {
            notSameDialog.dissmiss();
        }

        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    private void initGroup(RestTeeTimesModel group) {
        mPlayer1.setText(Html.fromHtml("<b>1. </b>" + group.getPlayer1()));
        mPlayer2.setText(Html.fromHtml("<b>2. </b>" + group.getPlayer2()));
        mPlayer3.setText(Html.fromHtml("<b>3. </b>" + group.getPlayer3()));
        mPlayer4.setText(Html.fromHtml("<b>4. </b>" + group.getPlayer4()));
        mHoleNumber.setText(getString(R.string.hole_number, group.getHole()));
        mStartDate.setText(group.getTeeTime());
    }

    public void bindGroup(RestTeeTimesModel group) {
        this.group = group;
    }

    @OnClick(R.id.btn_correct)
    void onConfirmClick() {
        GolfAlertDialog dialog = new GolfAlertDialog(getActivity());
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

    @OnClick(R.id.btn_skip)
    void onBackClick() {
        if (isSameGroup) {
            sameGroupDialog = new GolfAlertDialog(getActivity());
            sameGroupDialog.setCancelable(false);
            sameGroupDialog.setMessage(getString(R.string.about_to_change_message));
            sameGroupDialog.setOkText(getString(R.string.okay));
            sameGroupDialog.setBottomBtnText(getString(R.string.cancel));
            sameGroupDialog.setIcon(R.drawable.ic_danger_small);
            sameGroupDialog.setTitle(getString(R.string.change_group));
            sameGroupDialog.setAllCaps(true);

            sameGroupDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                @Override
                public void onOkClick() {
                    presenter.confirmOrRemoveGroup(group.getId(), "unassign");
                }

                @Override
                public void onBottomBtnClick() {
                    sameGroupDialog.dissmiss();
                }
            });

            sameGroupDialog.show();
        } else {
            notSameDialog = new GolfAlertDialog(getActivity());
            notSameDialog.setCancelable(false);
            notSameDialog.setMessage(getString(R.string.about_to_skip_message));
            notSameDialog.setOkText(getString(R.string.okay));
            notSameDialog.setIcon(R.drawable.ic_danger_small);
            notSameDialog.setTitle(getString(R.string.about_to_skip));
            notSameDialog.setBottomBtnText(getString(R.string.cancel));
            notSameDialog.setAllCaps(true);

            notSameDialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
                @Override
                public void onOkClick() {
                    onSkipMessageSuccess();
                }

                @Override
                public void onBottomBtnClick() {
                    notSameDialog.dissmiss();
                }
            });

            notSameDialog.show();
        }
    }

    @Override
    public void onChangeGroupSuccess() {
        PreferenceManager.getInstance().setCourseGroupId(null);
        onGroupPagerRefreshListener.onRefreshGroupsClick();
    }

    @Override
    public void onSkipMessageSuccess() {
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

    public void bindOnRefreshGroupsListener(GolfCallback.OnGroupPagerRefreshListener onGroupPagerRefreshListener) {
        this.onGroupPagerRefreshListener = onGroupPagerRefreshListener;
    }

    @OnClick(R.id.btn_refresh)
    void onRefreshGroupsClick() {
        if (onGroupPagerRefreshListener != null) {
            onGroupPagerRefreshListener.onRefreshGroupsClick();
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
