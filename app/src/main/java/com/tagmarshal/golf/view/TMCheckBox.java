package com.tagmarshal.golf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.tagmarshal.golf.R;

public class TMCheckBox extends AppCompatCheckBox {
    public TMCheckBox(Context context) {
        super(context);
        setFontFamily(context, null);

    }

    public TMCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFontFamily(context, attrs);

    }

    public TMCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFontFamily(context, attrs);

    }


    private void setFontFamily(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TMCheckBox);
        int font = a.getInt(R.styleable.TMTextView_fontFam, 0);

        Typeface typeface = null;

        switch (font) {
            case 0:
                typeface = Typeface.createFromAsset(context.getAssets(),
                        "osp_din.ttf");
                break;
            case 1:
                typeface = Typeface.createFromAsset(context.getAssets(),
                        "league_gothic_regular.otf");
                break;
        }

        setTypeface(typeface);

        a.recycle();
    }
}
