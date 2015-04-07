package com.jesussoto.android.sparkle.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.jesussoto.android.sparkle.R;

public class FloatingLabelSpinner extends FloatingLabelControl<Spinner> {

    private static final int PADDING_LEFT = 8;

    private final Spinner mSpinner = (Spinner) getControlView();

    public FloatingLabelSpinner(Context context) {
        this(context, null);
    }

    public FloatingLabelSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.floatingLabelSpinnerStyle);
    }

    public FloatingLabelSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPadding(
                getPaddingLeft()
                        + (int) (PADDING_LEFT * getResources().getDisplayMetrics().density + .5f),
                getPaddingTop(),
                getPaddingRight(),
                getPaddingBottom());

        fadeInToTop(false);
    }

    @Override
    protected void inflateControlView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.floating_label_spinner_merge, this);
    }

    public Object getSelectedItem() {
        return mSpinner.getSelectedItem();
    }

    public int getSelectedItemPosition() {
        return mSpinner.getSelectedItemPosition();
    }

    public void setAdapter(SpinnerAdapter adapter) {
        mSpinner.setAdapter(adapter);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mSpinner.setOnItemSelectedListener(listener);
    }

    public void setSelection(int position) {
        mSpinner.setSelection(position);
    }
}
