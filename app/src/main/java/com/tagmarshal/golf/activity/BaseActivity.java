package com.tagmarshal.golf.activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;

public class BaseActivity extends AppCompatActivity {

    public boolean isResumed = false;

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void addScoreCardFragment(BaseFragment baseFragment) {
        isResumed = true;
        addFragment(baseFragment);
    }

    // only if you from map
    public void addFragment(BaseFragment baseFragment) {
        if (!isResumed) return;

        if (baseFragment.isAdded()) {
            return;
        }

        BaseFragment visibleFragment =
                (BaseFragment) getSupportFragmentManager().findFragmentByTag(baseFragment.getClass().getName());

        if (visibleFragment != null && getSupportFragmentManager().getFragments().contains(visibleFragment))
            return;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        BaseFragment fragmentToHide = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        fragmentTransaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.container, baseFragment, baseFragment.getClass().getName())
                .addToBackStack(baseFragment.getClass().getName());

        if (fragmentToHide != null)
            fragmentTransaction.hide(fragmentToHide);

        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }

    public void addFragment(BaseFragment baseFragment, boolean backStack) {
        if (!isResumed) return;

        if (baseFragment.isAdded()) {
            return;
        }

        BaseFragment visibleFragment =
                (BaseFragment) getSupportFragmentManager().findFragmentByTag(baseFragment.getClass().getName());

        if (visibleFragment != null && getSupportFragmentManager().getFragments().contains(visibleFragment))
            return;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.replace(R.id.container, baseFragment, baseFragment.getClass().getName());

        if (backStack)
            fragmentTransaction.addToBackStack(baseFragment.getClass().getName());

        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        addFragment(baseFragment);
    }

    public void backToRootFragmentOrAddRootFragmentIfNotExists(Class tag) {
        if (!isResumed) return;

        try {
            if (getSupportFragmentManager().findFragmentByTag(tag.getName()) != null) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                Class classFragment = Class.forName(tag.getName());
                BaseFragment baseFragment = (BaseFragment) classFragment.newInstance();

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, baseFragment, baseFragment.getClass().getName())
                        .commitAllowingStateLoss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backToRootFragmentOrAddRootFragmentIfNotExistsNoResume(Class tag) {

        try {
            if (getSupportFragmentManager().findFragmentByTag(tag.getName()) != null) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                if (getSupportFragmentManager().getFragments() != null) {
                    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                    getSupportFragmentManager().getFragments().clear();
                }
            } else {
                Class classFragment = Class.forName(tag.getName());
                BaseFragment baseFragment = (BaseFragment) classFragment.newInstance();
                if (getSupportFragmentManager().getFragments() != null) {
                    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                    getSupportFragmentManager().getFragments().clear();
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, baseFragment, baseFragment.getClass().getName())
                        .commitAllowingStateLoss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFragment(String name) {
        if(!isResumed) return;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(name);
        if(fragment != null )
            getSupportFragmentManager().popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    //Allows main to reference the base activity for setting the power context
    public BaseActivity getBaseActivity() {
        return this;
    }
}
