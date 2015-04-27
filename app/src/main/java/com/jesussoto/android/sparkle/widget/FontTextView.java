package com.jesussoto.android.sparkle.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jesussoto.android.sparkle.R;
import com.jesussoto.android.sparkle.util.FontUtils;

public class FontTextView extends TextView {
    public FontTextView(Context context) {
        this(context, null, 0);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.FontTextView, defStyleAttr, 0);

            String typefaceName = a.getString(R.styleable.FontTextView_typeface);
            if (typefaceName != null) {
                FontUtils.setTypeface(this, typefaceName);
            }

            a.recycle();
        }
    }
}
