package com.tagmarshal.golf.fragment.maintenance;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.eventbus.SetMaintenanceEvent;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MaintenanceFragment extends BaseFragment implements MaintenanceView {
    private final static int LAYOUT = R.layout.fragment_maintenance;

    private MaintenancePresenter presenter;

    @BindView(R.id.top_margin)
    View mTopMargin;

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, view);
        presenter = new MaintenancePresenter(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        presenter.startTimer();
    }


    @Override
    public void onTimerFinish() {
        PreferenceManager.getInstance().clearMaintenance();
        ((MainActivity) getBaseActivity()).openStartScreen();
    }

    @Override
    public void onTimerError(Throwable e) {
        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshMaintence(SetMaintenanceEvent event) {
        if (isVisible()) {
            presenter.startTimer();
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Override
    public void onDestroyView() {
        if(getBaseActivity()!=null) {
            getBaseActivity().showMapButton();
        }
        presenter.onDestroy();
        super.onDestroyView();
    }
}
