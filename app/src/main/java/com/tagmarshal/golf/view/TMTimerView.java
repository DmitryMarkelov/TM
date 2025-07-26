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

public class TMTimerView extends FrameLayout {
    public TMTimerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.timer_view, this, false);
        addView(view);
    }

    public void setIcon(int icon) {
        ((ImageView) findViewById(R.id.icon)).setImageResource(icon);
    }

    public void setTime(String time) {
        ((TMTextView) findViewById(R.id.time)).setText(time);
    }

    public void setTitle(String title) {
        ((TMTextView) findViewById(R.id.title)).setText(title);
    }
}
