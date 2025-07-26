package com.tagmarshal.golf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.util.TMUtil;

public class TMButton extends FrameLayout {

    public TMButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.button_layout, this, false);
        addView(view);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TMButton);

        String text = a.getString(R.styleable.TMButton_buttonText);
        ((TMTextView) findViewById(R.id.text)).setText(text);

        int color = a.getResourceId(R.styleable.TMButton_buttonColor, R.color.green);

        int backgroundDrawableResId = a.getResourceId(R.styleable.TMButton_buttonBackgroundDrawable, 0);
        if (backgroundDrawableResId != 0) {
            setBackground(ContextCompat.getDrawable(context, backgroundDrawableResId));
        } else {
            setBackgroundColor(ContextCompat.getColor(context, color));
        }

        int textSize = a.getInt(R.styleable.TMButton_buttonTextSize, 0);

        switch (textSize) {
            case 0:
                ((TMTextView) findViewById(R.id.text)).setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_3));
                break;
            case 1:
                ((TMTextView) findViewById(R.id.text)).setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_1));
                break;
        }

        int btnSize = a.getInt(R.styleable.TMButton_TMButtonSize, 0);

        switch (btnSize) {
            case 0:
                findViewById(R.id.parent).getLayoutParams().height = TMUtil.getPxFromDp(context, R.integer.button_size_large_4_int);
                break;
            case 1:
                findViewById(R.id.parent).getLayoutParams().height = TMUtil.getPxFromDp(context, R.integer.button_size_large_5_int);
                break;
        }

        if (color == R.color.white) {
            ((ImageView) findViewById(R.id.icon)).setColorFilter(ContextCompat.getColor(context, R.color.gray));
            ((TMTextView) findViewById(R.id.text)).setTextColor(ContextCompat.getColor(context, R.color.gray));
        } else {
            ((ImageView) findViewById(R.id.icon)).setColorFilter(ContextCompat.getColor(context, R.color.white));
            ((TMTextView) findViewById(R.id.text)).setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        int icon = a.getResourceId(R.styleable.TMButton_buttonIcon, 0);

        if (icon == 0) {
            findViewById(R.id.icon).setVisibility(GONE);
        } else {
            findViewById(R.id.icon).setVisibility(VISIBLE);
            ((ImageView) findViewById(R.id.icon)).setImageResource(icon);
        }

        setClickable(true);

        //do something with str

        a.recycle();
    }

    public void showLoading(boolean showLoading) {
        setAlpha(showLoading ? 0.5f : 1f);
        setEnabled(!showLoading);
        findViewById(R.id.progress).setVisibility(showLoading ? VISIBLE : GONE);
    }

    public void setActive(boolean active) {
        setAlpha(!active ? 0.5f : 1f);
        setEnabled(active);
    }

    public void setText(String text) {
        ((TMTextView) findViewById(R.id.text)).setText(text);
    }
}
