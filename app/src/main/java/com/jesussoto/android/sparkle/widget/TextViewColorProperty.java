package com.jesussoto.android.sparkle.widget;

import android.util.Property;
import android.widget.TextView;

public class TextViewColorProperty extends Property<TextView, Integer> {

	public TextViewColorProperty(Class<Integer> type, String name) {
		super(type, name);
	}

	@Override
	public Integer get(TextView textView) {
		return Integer.valueOf(textView.getCurrentTextColor());
	}

	@Override
	public void set(TextView textView, Integer color) {
		textView.setTextColor(color);
	}
}
