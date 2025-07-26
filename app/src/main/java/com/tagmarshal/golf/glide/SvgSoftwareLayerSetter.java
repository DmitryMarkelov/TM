package com.tagmarshal.golf.glide;

import android.graphics.drawable.PictureDrawable;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.tagmarshal.golf.rest.model.PointOfInterest;

/**
 * Listener which updates the {@link ImageView} to be software rendered, because
 * {@link com.caverock.androidsvg.SVG SVG}/{@link android.graphics.Picture Picture} can't render on
 * a hardware backed {@link android.graphics.Canvas Canvas}.
 */
public abstract class SvgSoftwareLayerSetter implements RequestListener<PictureDrawable> {

     public PointOfInterest iteeem;


    public SvgSoftwareLayerSetter(PointOfInterest item) {
        this.iteeem = item;
    }
}
