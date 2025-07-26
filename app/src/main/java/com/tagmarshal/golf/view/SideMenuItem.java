package com.tagmarshal.golf.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tagmarshal.golf.R;

public class SideMenuItem extends FrameLayout {


    public SideMenuItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.side_menu_item, this, false);
        addView(view);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SideMenuItem);

        String text = a.getString(R.styleable.SideMenuItem_sideMenuText);
        ((TMTextView) findViewById(R.id.text)).setText(text);


        boolean light = a.getBoolean(R.styleable.SideMenuItem_sideMenuLight, false);

        if (!light) {
            ((ImageView) findViewById(R.id.icon)).setColorFilter(ContextCompat.getColor(context, R.color.gray));
            ((TMTextView) findViewById(R.id.text)).setTextColor(ContextCompat.getColor(context, R.color.gray));
        } else {
            ((ImageView) findViewById(R.id.icon)).setColorFilter(ContextCompat.getColor(context, R.color.white));
            ((TMTextView) findViewById(R.id.text)).setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        findViewById(R.id.divider).setVisibility(a.getBoolean(R.styleable.SideMenuItem_sideMenuTopDivider, true) ? VISIBLE : INVISIBLE);

        ((ImageView) findViewById(R.id.icon)).setImageResource(a.getResourceId(R.styleable.SideMenuItem_sideMenuIcon, 0));

        //do something with str

        a.recycle();
    }

    public void setActive(boolean active) {
        setEnabled(active);
        setClickable(active);
        findViewById(R.id.icon).setAlpha(active ? 1f : 0.3f);
        findViewById(R.id.text).setAlpha(active ? 1f : 0.3f);
    }
}
