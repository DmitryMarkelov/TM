package com.tagmarshal.golf.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.eventbus.MovingCartEvent;
import com.tagmarshal.golf.eventbus.UserInteractionEvent;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentInAcitivty extends BaseFragment {

    @BindView(R.id.course_name)
    TMTextView mCourseName;

    @BindView(R.id.tv_battery_level_percent)
    TMTextView mBatteryPercent;

    @BindView(R.id.tv_battery_level_static)
    TMTextView mBatteryLevelStatic;

    @BindView(R.id.tag)
    TMTextView mTag;

    @BindView(R.id.iv_battery)
    ImageView mBattery;

    @BindView(R.id.view_to_gone)
    View goneView;

    Unbinder unbinder;

    Disposable checkBatteryTimer;
    private int curBrightnessValue;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    public static FragmentInAcitivty getInstance(boolean blackSreen) {
        FragmentInAcitivty fragment = new FragmentInAcitivty();
        Bundle args = new Bundle();
        args.putBoolean("blackScreen", blackSreen);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_inactive, container, false);

        unbinder = ButterKnife.bind(this, view);

        if (getArguments() != null && getArguments().getBoolean("blackScreen")) {
            goneView.setVisibility(View.VISIBLE);
        } else {
            goneView.setVisibility(View.GONE);
        }
//        getBaseActivity().getWindow().getDecorView().setKeepScreenOn(true);

        mCourseName.setText(PreferenceManager.getInstance().getCourseName());
        mTag.setText(PreferenceManager.getInstance().getDeviceTag());

        checkBatteryTimer = Observable.interval(0, 350, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (!isCharging(getContext())) {
                            mBatteryLevelStatic.setText(getString(R.string.battery_level));
                            if (TMUtil.getBatteryLevel() < 17) {
                                mBattery.setImageResource(R.drawable.ic_battery_6);
                            } else if (TMUtil.getBatteryLevel() < 34) {
                                mBattery.setImageResource(R.drawable.ic_battery_5);
                            } else if (TMUtil.getBatteryLevel() < 58) {
                                mBattery.setImageResource(R.drawable.ic_battery_4);
                            } else if (TMUtil.getBatteryLevel() < 70) {
                                mBattery.setImageResource(R.drawable.ic_battery_3);
                            } else if (TMUtil.getBatteryLevel() < 88) {
                                mBattery.setImageResource(R.drawable.ic_battery_2);
                            } else {
                                mBattery.setImageResource(R.drawable.ic_battery_1);
                            }
                        } else {
                            mBattery.setImageResource(R.drawable.ic_battery_charging);
                            mBatteryLevelStatic.setText(getString(R.string.charging));
                        }

                        if (TMUtil.getBatteryLevel() != 0) {
                            mBatteryPercent.setVisibility(View.VISIBLE);
                            mBatteryLevelStatic.setVisibility(View.VISIBLE);
                            mBattery.setVisibility(View.VISIBLE);
                            mBatteryPercent.setText(TMUtil.getBatteryLevel() + "%");
                        } else {
                            mBatteryPercent.setVisibility(View.INVISIBLE);
                            mBatteryLevelStatic.setVisibility(View.INVISIBLE);
                            mBattery.setVisibility(View.INVISIBLE);
                        }

                        mBatteryPercent.setText(TMUtil.getBatteryLevel() + "%");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        curBrightnessValue = android.provider.Settings.System.getInt(getBaseActivity().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, -1);
    }


    private void changeScreenBrightness(int screenBrightnessValue) {
        WindowManager.LayoutParams lp = getBaseActivity().getWindow().getAttributes();

        lp.screenBrightness = screenBrightnessValue;
        getBaseActivity().getWindow().setAttributes(lp);

    }

    public static boolean isCharging(Context context) {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = context.registerReceiver(null, filter);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
                return true;
            }
        } catch (Exception e) {
            //
        }

        try {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.isCharging();
        } catch (Exception e) {
            //
        }
        return false;
    }

    @OnClick(R.id.back)
    void onBackClick() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.inactivity_frame))
                .commitAllowingStateLoss();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCartMoving(MovingCartEvent event) {
        onBackClick();
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe
    public void onInteraction(UserInteractionEvent event) {
        onBackClick();
        EventBus.getDefault().removeStickyEvent(event);

    }

    @Override
    public void onDestroyView() {
        if (checkBatteryTimer != null && !checkBatteryTimer.isDisposed()) {
            checkBatteryTimer.dispose();
            checkBatteryTimer = null;
        }

        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }
}
