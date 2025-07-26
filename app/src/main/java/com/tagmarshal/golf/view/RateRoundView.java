package com.tagmarshal.golf.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.tagmarshal.golf.R;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RateRoundView extends FrameLayout {

    @BindViews({R.id.star_1,
            R.id.star_2,
            R.id.star_3,
            R.id.star_4,
            R.id.star_5})
    List<ImageButton> starsButtons;

    RateRoundListener rateRoundListener;

    int stars = 0;

    public RateRoundView(@NonNull Context context) {
        super(context);
    }

    public RateRoundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RateRoundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public RateRoundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_rate_round, this, false);

        addView(view);

        ButterKnife.bind(this, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RateRoundView);

        int stars = a.getInt(R.styleable.RateRoundView_RateRoundView_stars, 0);

        setStars(stars);

        a.recycle();
    }

    public void setOnRateRoundListener(RateRoundListener rateRoundListener) {
        this.rateRoundListener = rateRoundListener;
    }

    private void setStars(int stars) {
        for (int i = 0; i < starsButtons.size(); i++) {
            if (i < stars) {
                starsButtons.get(i).setImageResource(R.drawable.ic_star);
            } else {
                starsButtons.get(i).setImageResource(R.drawable.ic_star_border);
            }
        }

        this.stars = stars;
    }

    public int getStars() {
        return stars;
    }

    @OnClick({R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5})
    void onStarClick(View view) {
        for (int i = 0; i < starsButtons.size(); i++) {
            if (starsButtons.get(i).getId() == view.getId()) {
                setStars(i + 1);
                break;
            }
        }

        if (rateRoundListener != null) {
            rateRoundListener.onRate(stars);
        }
    }

    public interface RateRoundListener {
        void onRate(int stars);
    }
}
