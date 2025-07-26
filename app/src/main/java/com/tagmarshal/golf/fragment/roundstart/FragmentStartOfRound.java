package com.tagmarshal.golf.fragment.roundstart;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentStartOfRound extends BaseFragment implements FragmentStartOfRoundContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.start_device_tag)
    TMTextView mStartDeviceTag;

    @BindView(R.id.hole_tv)
    TMTextView mHole;

    @BindView(R.id.starting_time)
    TMTextView mStartingTime;

    @BindView(R.id.group_tv)
    TMTextView groupTv;

    private Unbinder unbinder;

    private FragmentStartOfRoundPresenter presenter;

    private String tag;
    private String hole;
    private String startTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_of_round, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentStartOfRoundPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        presenter.onCreate();

        if(PreferenceManager.getInstance().isDeviceMobile()){
            groupTv.setText(getString(R.string.tag));
        }else{
            groupTv.setText(getString(R.string.cart));
        }

        mStartDeviceTag.setText(tag);
        GradientDrawable bgShape = (GradientDrawable) mStartDeviceTag.getBackground();
        bgShape.setColor(ContextCompat.getColor(getContext(), R.color.green));
        PreferenceManager.getInstance().setRatedRound(false);

        mHole.setText(getString(R.string.hole_number, hole));

        return view;
    }

    @OnClick(R.id.btn_view_course_map)
    void onViewCourseMapClick() {
        openMapFragment();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        unbinder.unbind();

        presenter = null;
        unbinder = null;
        super.onDestroyView();
    }

    private void openMapFragment() {
        clearAndAddRootFragment(new FragmentMap());
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setHole(String hole) {
        this.hole = hole;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public void countDownTimerTick() {
        if (TMUtil.getLeftMinutes(startTime) <= 0) {
            openMapFragment();
            return;
        }

        mStartingTime.setText(getString(R.string.left_time, TMUtil.getLeftMinutes(startTime)));
    }
}
