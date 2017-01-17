package com.aqilix.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class LatoTextView extends TextView {

    public LatoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LatoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LatoTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Light.ttf");
            setTypeface(tf);
        }
    }

}