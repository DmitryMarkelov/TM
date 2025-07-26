package com.tagmarshal.golf.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.service.GolfService;

public class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (PreferenceManager.getInstance().getDeviceType().equals(PreferenceManager.DEVICE_TYPE_CART)) {
            getBaseActivity().setKeepScreenOn(false);
        } else {
            getBaseActivity().setKeepScreenOn(true);
        }
        if (getGolfService() == null) {
            Log.i(getTag(), "Base Fragment: onCreate: getGolfService = null");
            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getTag() + " " + "Base Fragment: onCreate: getGolfService = truenull");
        }
        if (getGolfService() != null) {
            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + getTag() + " " + "Base Fragment: onCreate: getGolfService != null");
        }
    }

    public GolfService getGolfService() {
        if (getActivity() == null) return null;
        return ((MainActivity) getActivity()).getGolfService();
    }

    public void popFragment() {
        getBaseActivity().getSupportFragmentManager().popBackStack();
    }

    public MainActivity getBaseActivity() {
        return (MainActivity) getActivity();
    }

    public void replaceThisFragment(BaseFragment thisFragment, BaseFragment baseFragment) {
        if (getBaseActivity() == null || !getBaseActivity().isResumed) return;

        getBaseActivity().getSupportFragmentManager().popBackStack(thisFragment.getClass().getName(), 1);

        FragmentTransaction fragmentTransaction = getBaseActivity().getSupportFragmentManager().beginTransaction();

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction
                .replace(R.id.container, baseFragment, baseFragment.getClass().getName())
                .addToBackStack(baseFragment.getClass().getName());

        fragmentTransaction.commitAllowingStateLoss();
    }

    public void clearAndAddRootFragment(BaseFragment baseFragment) {
        if (getBaseActivity() == null) return;
        if (!getBaseActivity().isResumed) return;
        if (baseFragment.isAdded()) return;

        if (PreferenceManager.getInstance().getMaintenance() != null) {
            getBaseActivity().openStartScreen();
            return;
        }

        getBaseActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getBaseActivity().getSupportFragmentManager().beginTransaction();

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.replace(R.id.container, baseFragment, baseFragment.getClass().getName());

        fragmentTransaction.commitAllowingStateLoss();
    }

    public void backToRootFragment() {
        try {
            getBaseActivity().getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
    }

    @Deprecated
    public void addFragment(BaseFragment thisFragment, BaseFragment baseFragment, boolean backStack, String tag) {
        if (getBaseActivity() == null) return;
        if (!getBaseActivity().isResumed) return;
        if (baseFragment.isAdded()) return;
        FragmentTransaction fragmentTransaction = getBaseActivity().getSupportFragmentManager().beginTransaction();

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.replace(R.id.container, baseFragment, tag);


        if (backStack)
            fragmentTransaction.addToBackStack(tag);

        fragmentTransaction.commitAllowingStateLoss();
    }

    @Deprecated
    public void addFragmentWithoutResume(BaseFragment thisFragment, BaseFragment baseFragment, boolean backStack, String tag) {
        if (getBaseActivity() == null) return;
        if (baseFragment.isAdded()) return;
        if (getBaseActivity().getSupportFragmentManager().findFragmentByTag(tag) != null) {
            return;
        }

        FragmentTransaction fragmentTransaction = getBaseActivity().getSupportFragmentManager().beginTransaction();

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.replace(R.id.container, baseFragment, tag);


        if (backStack)
            fragmentTransaction.addToBackStack(tag);

        fragmentTransaction.commitAllowingStateLoss();
    }
}
