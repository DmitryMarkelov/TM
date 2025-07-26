package com.tagmarshal.golf.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tagmarshal.golf.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GolfSwitchView extends FrameLayout {

    @BindView(R.id.icon_1)
    ImageView mIcon1;

    @BindView(R.id.icon_2)
    ImageView mIcon2;

    @BindView(R.id.icon_3)
    ImageView mIcon3;

    @BindView(R.id.text_1)
    TMTextView mText1;

    @BindView(R.id.text_2)
    TMTextView mText2;

    @BindView(R.id.text_3)
    TMTextView mText3;

    @BindView(R.id.switcher)
    View mSwitcher;

    private int switcherWidth;
    private int checked = 0;
    private int vars = 2;
    private int icon1, icon2, icon3;
    private String text1, text2, text3;

    public interface OnSwitchViewClick {
        void onSwitch(int i);
    }

    private OnSwitchViewClick onSwitchViewClick;


    public GolfSwitchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public GolfSwitchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public GolfSwitchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    public void setOnSwitchViewClick(OnSwitchViewClick onSwitchViewClick) {
        this.onSwitchViewClick = onSwitchViewClick;
    }

    private void initView(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_switch, this, false);
        addView(view);
        ButterKnife.bind(this, view);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GolfSwitchView);

        vars = a.getInteger(R.styleable.GolfSwitchView_golfSwitchVars, 2);

        icon1 = a.getResourceId(R.styleable.GolfSwitchView_golfSwitch1Icon, 0);
        icon2 = a.getResourceId(R.styleable.GolfSwitchView_golfSwitch2Icon, 0);
        icon3 = a.getResourceId(R.styleable.GolfSwitchView_golfSwitch3Icon, 0);

        text1 = a.getString(R.styleable.GolfSwitchView_golfSwitch1Text);
        text2 = a.getString(R.styleable.GolfSwitchView_golfSwitch2Text);
        text3 = a.getString(R.styleable.GolfSwitchView_golfSwitch3Text);

        a.recycle();

        init();
    }

    private void init() {
        if (icon1 == 0) mIcon1.setVisibility(GONE);
        if (icon2 == 0) mIcon2.setVisibility(GONE);
        if (icon3 == 0) mIcon3.setVisibility(GONE);

        if (vars == 2) {
            findViewById(R.id.switch_3).setVisibility(GONE);
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switch_1).getLayoutParams()).matchConstraintPercentWidth = 0.5f;
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switch_2).getLayoutParams()).matchConstraintPercentWidth = 0.5f;
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switch_3).getLayoutParams()).matchConstraintPercentWidth = 0.5f;
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switcher).getLayoutParams()).matchConstraintPercentWidth = 0.5f;
        } else {
            findViewById(R.id.switch_3).setVisibility(VISIBLE);
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switch_1).getLayoutParams()).matchConstraintPercentWidth = 0.33f;
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switch_2).getLayoutParams()).matchConstraintPercentWidth = 0.33f;
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switch_3).getLayoutParams()).matchConstraintPercentWidth = 0.33f;
            ((ConstraintLayout.LayoutParams) findViewById(R.id.switcher).getLayoutParams()).matchConstraintPercentWidth = 0.33f;
        }

        mIcon1.setImageResource(icon1);
        mIcon2.setImageResource(icon2);
        mIcon3.setImageResource(icon3);

        mText1.setText(text1);
        mText2.setText(text2);
        mText3.setText(text3);

        mSwitcher.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                switcherWidth = mSwitcher.getWidth();

                mSwitcher.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (checked == 0) {
                    onSwitch1Click();
                } else if (checked == 1) {
                    onSwitch2Click();
                } else {
                    onSwitch3Click();
                }
            }
        });
    }

    public void setVars(int vars) {
        this.vars = vars;
        init();
        requestLayout();
    }

    @OnClick(R.id.switch_1)
    void onSwitch1Click() {
        checked = 0;
        mSwitcher.animate().translationX(0).setDuration(275).start();

        if (onSwitchViewClick != null) {
            onSwitchViewClick.onSwitch(0);
        }
    }

    @OnClick(R.id.switch_2)
    void onSwitch2Click() {
        checked = 1;
        mSwitcher.animate().translationX(switcherWidth).setDuration(275).start();

        if (onSwitchViewClick != null) {
            onSwitchViewClick.onSwitch(1);
        }
    }

    @OnClick(R.id.switch_3)
    void onSwitch3Click() {
        checked = 2;
        mSwitcher.animate().translationX(switcherWidth * 2).setDuration(275).start();

        if (onSwitchViewClick != null) {
            onSwitchViewClick.onSwitch(2);
        }
    }

    public void setChecked(int check) {
        if (check == 0) {
            checked = 0;
            onSwitch1Click();
        } else if (check == 1) {
            checked = 1;
            onSwitch2Click();
        } else {
            checked = 2;
            onSwitch3Click();
        }
    }

    public int getChecked() {
        return checked;
    }


}
