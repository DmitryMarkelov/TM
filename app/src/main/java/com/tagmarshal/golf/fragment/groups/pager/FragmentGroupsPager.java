package com.tagmarshal.golf.fragment.groups.pager;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.eventbus.FirebaseTeeRefresh;
import com.tagmarshal.golf.eventbus.UserInteractionEvent;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.FragmentInAcitivty;
import com.tagmarshal.golf.fragment.groups.group.FragmentPagerGroup;
import com.tagmarshal.golf.fragment.map.FragmentMap;
import com.tagmarshal.golf.manager.GameManager;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestTeeTimesModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.CustomViewPager;
import com.tagmarshal.golf.view.circularviewpager.BaseCircularViewPagerAdapter;
import com.tagmarshal.golf.view.circularviewpager.CircularViewPagerHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentGroupsPager extends BaseFragment implements GolfCallback.OnGroupSwipePageListener,
        FragmentGroupsPagerContract.View, GolfCallback.OnGroupPagerRefreshListener {

    @BindView(R.id.viewpager)
    CustomViewPager mViewPager;

    private FragmentGroupsPagerPresenter presenter;

    Unbinder unbinder;

    private androidx.viewpager.widget.PagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups_viewpager, container, false);

        unbinder = ButterKnife.bind(this, view);

        pagerAdapter = new PagerAdapter(getChildFragmentManager());

        getBaseActivity().loadClubLogo();

        getBaseActivity().setMapEnabled(false);
        getBaseActivity().setContactUsEnabled(false);
        getBaseActivity().setEmergencyEnabled(false);
        getBaseActivity().setGroupInfoEnabled(false);
        getBaseActivity().setSpeakersEnabled(false);

        PreferenceManager.getInstance().setStartHole(1);
        PreferenceManager.getInstance().setCourseTeeStartTime(null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new FragmentGroupsPagerPresenter(this);
        presenter.getGroups();
        GameManager.setCurrentPlayHole(0);
        EventBus.getDefault().register(this);

//        presenter.startInactiveStateTimer();
    }

    @Subscribe
    public void onInteraction(UserInteractionEvent event) {
//        presenter.startInactiveStateTimer();
        PreferenceManager.getInstance().setRatedRound(false);

    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        presenter.onDestroy();
        presenter = null;

        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    @Override
    public void onPrevPage() {
        mViewPager.arrowScroll(View.FOCUS_LEFT);
    }

    @Override
    public void onNextPage() {
        mViewPager.arrowScroll(View.FOCUS_RIGHT);
    }

    @Override
    public void showGroups(List<RestTeeTimesModel> groups) {
        pagerAdapter.notifyDataSetChanged();

        if (groups.size() == 0) {
            clearAndAddRootFragment(new FragmentMap());
        } else if (groups.size() == 1) {
            com.tagmarshal.golf.fragment.group.FragmentGroup fragment =
                    new com.tagmarshal.golf.fragment.group.FragmentGroup();
            fragment.bindGroup(groups.get(0));
            fragment.bindOnRefreshGroupsListener(this);
            pagerAdapter = new PagerAdapter(getChildFragmentManager());
            ((PagerAdapter) pagerAdapter).addFragment(fragment);
            mViewPager.setAdapter(pagerAdapter);
        } else {
            pagerAdapter = new PagerAdapterCircular(getChildFragmentManager(), groups, this, this);
            mViewPager.setAdapter(pagerAdapter);
            mViewPager.addOnPageChangeListener(new CircularViewPagerHandler(mViewPager));


        }


    }

    @Override
    public void showWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }

    @Override
    public void onInactiveState() {
        if (getBaseActivity().getSupportFragmentManager().findFragmentById(R.id.inactivity_frame) == null) {
            getBaseActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.inactivity_frame, new FragmentInAcitivty())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void showFailureMessage(String message) {
        Toast.makeText(getContext(), getString(R.string.could_not_get_the_groups), Toast.LENGTH_SHORT).show();
        clearAndAddRootFragment(new FragmentMap());
    }

    @Override
    public void onRefreshGroupsClick() {
        presenter.getGroups();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<BaseFragment> fragments = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(BaseFragment baseFragment) {
            fragments.add(baseFragment);
            notifyDataSetChanged();
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private class PagerAdapterCircular extends BaseCircularViewPagerAdapter<RestTeeTimesModel> {

        GolfCallback.OnGroupSwipePageListener onGroupSwipePageListener;
        GolfCallback.OnGroupPagerRefreshListener OnGroupPagerRefreshListener;


        public PagerAdapterCircular(FragmentManager childFragmentManager, List<RestTeeTimesModel> groups, GolfCallback.OnGroupSwipePageListener onGroupSwipePageListener, GolfCallback.OnGroupPagerRefreshListener OnGroupPagerRefreshListener) {
            super(childFragmentManager, groups);
            this.OnGroupPagerRefreshListener = OnGroupPagerRefreshListener;
            this.onGroupSwipePageListener = onGroupSwipePageListener;
        }

        @Override
        protected Fragment getFragmentForItem(RestTeeTimesModel restTeeTimesModel) {
            FragmentPagerGroup fragment = new FragmentPagerGroup();
            fragment.bindSwipeChangeListener(onGroupSwipePageListener);
            fragment.bindGroup(restTeeTimesModel);
            fragment.bindOnRefreshGroupsListener(OnGroupPagerRefreshListener);
            return fragment;
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTeeRefresh(FirebaseTeeRefresh event){
        onRefreshGroupsClick();
        EventBus.getDefault().removeStickyEvent(event);

    }


}
