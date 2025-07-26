package com.tagmarshal.golf.fragment.selectcourse;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.CourseAdapter;
import com.tagmarshal.golf.constants.GolfConstants;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.groups.pager.FragmentGroupsPager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMButton;
import com.tagmarshal.golf.view.TMTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentSelectCourse extends BaseFragment implements FragmentSelectCourseContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.info_tv)
    TMTextView mInfo;

    @BindView(R.id.recyclerview)
    RecyclerView mRecycler;

    @BindView(R.id.try_again_btn)
    TMButton mTryAgainBtn;

    @BindView(R.id.btn_cancel)
    TMButton mCancelBtn;

    @BindView(R.id.btn_permanently_assign_device)
    TMButton mPermAssignDevice;

    Unbinder unbinder;

    private LinearLayoutManager layoutManager;
    private CourseAdapter courseAdapter;
    private FragmentSelectCoursePresenter presenter;

    public static FragmentSelectCourse newInstance(boolean showCancelButton) {
        FragmentSelectCourse fragment = new FragmentSelectCourse();
        Bundle args = new Bundle();
        args.putBoolean(GolfConstants.SHOW_CANCEL_BUTTON, showCancelButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assign_screen, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentSelectCoursePresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ", new ImageSpan(getActivity(), R.drawable.ic_help_text), 0)
                .append(" Assign the device to the course it will be used on. Permanently assigning" +
                        " a device can be undone through Admin in the menu.");

        mInfo.setText(builder);

        getBaseActivity().setMapEnabled(false);
        getBaseActivity().setContactUsEnabled(false);
        getBaseActivity().setEmergencyEnabled(false);
        getBaseActivity().setGroupInfoEnabled(false);
        getBaseActivity().setSpeakersEnabled(false);
        getBaseActivity().clearLogo();

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.onCreate();

        tryToGetCourses();
        getBaseActivity().setKeepScreenOn(false);
        boolean showCancel = getArguments() != null && getArguments().getBoolean(GolfConstants.SHOW_CANCEL_BUTTON, false);
        if (showCancel) {
            mCancelBtn.setVisibility(View.VISIBLE);
        } else {
            mCancelBtn.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.try_again_btn)
    void onTryAgainClick() {
        mTryAgainBtn.setVisibility(View.GONE);
        tryToGetCourses();
    }

    @OnClick(R.id.btn_cancel)
    void onCancelClick() {
        popFragment();
    }

    private void tryToGetCourses() {
        presenter.getCourses();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;

        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    @OnClick(R.id.btn_permanently_assign_device)
    void onPermanentlyAssignDeviceClick() {
        if (courseAdapter != null && courseAdapter.getSelectedItem() != null) {
            presenter.attachToCourse(courseAdapter.getSelectedItem(), true);
        } else {
            Toast.makeText(getContext(), getString(R.string.pls_select_the_course), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setCourses(List<CourseModel> courses) {
        layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);

        courseAdapter = new CourseAdapter(courses);

        mRecycler.setAdapter(courseAdapter);

        // Auto select the course
        if (courses != null && courses.size() == 1) {
            presenter.attachToCourse(courses.get(0), true);
        }
    }

    @Override
    public void getCoursesFail(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        mTryAgainBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRegisterDeviceSuccess(String url) {
        PreferenceManager.getInstance().clearZoneInfo();
        getBaseActivity().sendFirebaseToken();
        if (getActivity() != null) {
            getBaseActivity().setClearedData(true);
        }
        getBaseActivity().enableCheckingForNewZone(true);
        getBaseActivity().refreshCourse();
    }

    @Override
    public void onRegisterDeviceFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessSaveConfig() {
        FragmentGroupsPager fragmentGroupsPager = new FragmentGroupsPager();
        addFragment(this, fragmentGroupsPager, true, FragmentGroupsPager.class.getName());
    }

    @Override
    public void showWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }

    @Override
    public void onFailureRequest(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}