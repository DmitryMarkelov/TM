package com.tagmarshal.golf.view;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tagmarshal.golf.R;

public class InactiveMapVIew extends ConstraintLayout {
    public InactiveMapVIew(Context context) {
        super(context);
        initLayout();
    }

    private void initLayout() {
        inflate(getContext(), R.layout.view_inactive_map, this);
    }
}
