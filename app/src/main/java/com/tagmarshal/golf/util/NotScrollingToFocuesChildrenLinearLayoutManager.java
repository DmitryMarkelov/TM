package com.tagmarshal.golf.util;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class NotScrollingToFocuesChildrenLinearLayoutManager extends LinearLayoutManager {
    public NotScrollingToFocuesChildrenLinearLayoutManager(Context context) {
        super(context);
    }

    public NotScrollingToFocuesChildrenLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public NotScrollingToFocuesChildrenLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onRequestChildFocus(RecyclerView parent, RecyclerView.State state, View child, View focused) {
        //return super.onRequestChildFocus(parent, state, child, focused);
        return true;
    }

    @Override
    public boolean onRequestChildFocus(RecyclerView parent, View child, View focused) {
        //return super.onRequestChildFocus(parent, child, focused);
        return true;
    }
}
