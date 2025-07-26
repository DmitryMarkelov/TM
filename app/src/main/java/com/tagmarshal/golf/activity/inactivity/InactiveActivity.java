package com.tagmarshal.golf.activity.inactivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

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

public class InactiveActivity extends AppCompatActivity {

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

    Unbinder unbinder;

    Disposable checkBatteryTimer;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inactive);

        getWindow().getDecorView().setKeepScreenOn(true);

        unbinder = ButterKnife.bind(this);

        mCourseName.setText(PreferenceManager.getInstance().getCourseName());
        mTag.setText(PreferenceManager.getInstance().getDeviceTag());

        checkBatteryTimer = Observable.interval(0, 350, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (!isCharging(getApplicationContext())) {
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
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @OnClick(R.id.back)
    void onBackClick() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (checkBatteryTimer != null && !checkBatteryTimer.isDisposed()) {
            checkBatteryTimer.dispose();
            checkBatteryTimer = null;
        }

        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }
}
