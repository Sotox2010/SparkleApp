package com.jesussoto.android.sparkle.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jesussoto.android.sparkle.R;


public class FloatingLabelEditText extends FloatingLabelControl<EditText> {

    private static final String TAG = "FloatingLabelEditText";

    /**
     * The default main EditText text size.
     */
    private static final int DEFAULT_TEXT_SIZE = 18;

    private final EditText mEditText = (EditText) getControlView();
    private int mTextAppearance;

    public FloatingLabelEditText(Context context) {
        this(context, null);
    }

    public FloatingLabelEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.floatingLabelEditTextStyle);
    }

    public FloatingLabelEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mEditText.addTextChangedListener(mTextWatcher);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.FloatingLabelEditText, defStyle, 0);

            String hint = a.getString(R.styleable.FloatingLabelEditText_android_hint);

            if (!TextUtils.isEmpty(hint)) {
                mEditText.setHint(hint);
                setFloatingLabelText(hint);
            }

            mEditText.setText(a.getString(R.styleable.FloatingLabelEditText_android_text));

            mTextAppearance = a.getResourceId(
                    R.styleable.FloatingLabelEditText_android_textAppearance, -1);

            if (mTextAppearance != -1) {
                mEditText.setTextAppearance(context, mTextAppearance);
            }

            mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    a.getDimensionPixelSize(R.styleable.FloatingLabelEditText_android_textSize,
                        (int) (DEFAULT_TEXT_SIZE * getResources().getDisplayMetrics().density)));

            int inputType = a.getInteger(R.styleable.FloatingLabelEditText_android_inputType, -1);

            if (inputType != -1) {
                mEditText.setInputType(inputType);
            }

            int hintColor = a.getColor(R.styleable.FloatingLabelEditText_android_textColorHint, -1);
            if (hintColor != -1) {
                mEditText.setHintTextColor(hintColor);
            }

            ColorStateList textColor = a.getColorStateList(
                    R.styleable.FloatingLabelEditText_android_textColor);
            if (textColor != null) {
                mEditText.setTextColor(textColor);
            }

            int textGravity = a.getInteger(R.styleable.FloatingLabelEditText_textGravity, -1);
            if (textGravity != -1) {
                mEditText.setGravity(textGravity);
            }

            a.recycle();
        }

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                updateFloatingLabelColor();
            }
        });

    }

    @Override
    protected void inflateControlView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.floating_label_edit_text_merge, this);
    }

    public CharSequence getText() {
        return mEditText.getText();
    }

    public void setText(int resId) {
        setText(getContext().getString(resId));
    }

    public void setText(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            mEditText.setText(text);
        }
    }

    public CharSequence getHint() {
        return mEditText.getHint();
    }

    public void setHint(int resId) {
        setHint(getContext().getString(resId));
    }

    public void setHint(CharSequence hint) {
        if (!TextUtils.isEmpty(hint)) {
            mEditText.setHint(hint);
            setFloatingLabelText(hint);
        }
    }

    public void setTextAppearance(Context context, int resId) {
        if (mTextAppearance != resId) {
            mEditText.setTextAppearance(context, resId);
            mTextAppearance = resId;
        }
    }

    public void setTextSize(float size) {
        mEditText.setTextSize(size);
    }

    public void setTextSize(int unit, float size) {
        mEditText.setTextSize(unit, size);
    }

    public void setInputType(int type) {
        mEditText.setInputType(type);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        mEditText.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        mEditText.removeTextChangedListener(watcher);
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                fadeInToTop(true);
            } else {
                fadeOutToBottom(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}
