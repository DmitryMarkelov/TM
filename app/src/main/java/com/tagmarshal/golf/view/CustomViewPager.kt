package com.tagmarshal.golf.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

open class CustomViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean =
            try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                false
            }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean =
            try {
                super.onTouchEvent(ev)
            } catch (e: java.lang.IllegalArgumentException) {
                true
            }

}