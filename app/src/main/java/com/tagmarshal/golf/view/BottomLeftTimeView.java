package com.tagmarshal.golf.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.util.TMUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BottomLeftTimeView extends FrameLayout {

    @BindView(R.id.icon)
    ImageView mIcon;

    @BindView(R.id.left_tv)
    TMTextView mLeftText;

    @BindView(R.id.right_tv)
    TMTextView mRightText;

    public BottomLeftTimeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_left_time_view, this, false);
        addView(view);

        ButterKnife.bind(this, this);
    }

    public void setLeftTime(int secs) {
        mLeftText.setText(getContext().getString(R.string.left_time_pace, TMUtil.covertSecToMinutes(secs)));
    }

    public void setRightText(String text) {
        mRightText.setText(text);
    }

    public void setStartingIn(int startLeftTime) {
        mLeftText.setVisibility(GONE);
        mLeftText.setText("");

        mIcon.setImageResource(R.drawable.ic_delay);
        mRightText.setText(getContext().getString(R.string.starting_in_var, startLeftTime));
    }

    public void setTimeOnPace(int timeOnPace) {
        mLeftText.setVisibility(VISIBLE);
        mRightText.setVisibility(VISIBLE);
        mIcon.setVisibility(VISIBLE);

        mIcon.setImageResource(R.drawable.ic_delay);
        mLeftText.setText(getContext().getString(R.string.left_time_pace, TMUtil.covertSecToMinutes(timeOnPace)));
        mRightText.setText(getContext().getString(R.string.on_pace));
    }
    public void setTimeOnPace(long timeOnPace) {
        mLeftText.setVisibility(VISIBLE);
        mRightText.setVisibility(VISIBLE);
        mIcon.setVisibility(VISIBLE);

        mIcon.setImageResource(R.drawable.ic_delay);
        mLeftText.setText(getContext().getString(R.string.left_time_pace, timeOnPace));
        mRightText.setText(getContext().getString(R.string.on_pace));
    }
}
