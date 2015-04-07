package com.jesussoto.android.sparkle.widget;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jesussoto.android.sparkle.R;

public abstract class FloatingLabelControl<T extends View> extends LinearLayout {

    /**
     * The default interpolator for animations.
     */
    private static final TimeInterpolator ANIMATION_INTERPOLATOR = new DecelerateInterpolator(2.0f);

    /**
     * ARGB evaluator for color transition animations.
     */
    private static final ArgbEvaluator COLOR_EVALUATOR = new ArgbEvaluator();

    /**
     * Custom property for text color transitions on TextView.
     */
    private static final Property<TextView, Integer> TEXT_COLOR_PROPERTY =
            new TextViewColorProperty(Integer.TYPE, "textColor");

    /**
     * The default animation duration value.
     */
    private static final long ANIMATION_DURATION = 333L;

    private final TextView mFloatingLabel;
    private final T mControlView;
    private final ViewPropertyAnimator mFloatingLabelAnimator;
    private final ObjectAnimator mFloatingLabelColorAnimator;
    private final Rect mBoundsRect = new Rect();

    private ColorStateList mFloatingLabelColor;
    private int mFloatingLabelTextAppearance;
    private boolean mLaidOut = false;
    private boolean mFloating = false;

    public FloatingLabelControl(Context context) {
        this(context, null);
    }

    public FloatingLabelControl(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.floatingLabelControlStyle);
    }

    @SuppressWarnings("unchecked")
    public FloatingLabelControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources resources = context.getResources();

        LayoutInflater.from(context).inflate(R.layout.floating_label_control_merge, this);
        inflateControlView(context);

        mFloatingLabel = (TextView) getChildAt(0);
        mControlView = (T) getChildAt(1);
        mFloatingLabelAnimator = mFloatingLabel.animate();
        mFloatingLabelAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
        mFloatingLabelColorAnimator = ObjectAnimator.ofInt(mFloatingLabel, TEXT_COLOR_PROPERTY, 0);
        mFloatingLabelColorAnimator.setEvaluator(COLOR_EVALUATOR);
        mFloatingLabelColorAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
        mFloatingLabelColor = resources.getColorStateList(R.color.floating_label);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.FloatingLabelControl, defStyle, 0);

            mControlView.setId(a.getResourceId(R.styleable.FloatingLabelControl_controlId, -1));

            mFloatingLabelTextAppearance = a.getResourceId(
                    R.styleable.FloatingLabelControl_floatingLabelTextAppearance,
                            R.style.TextAppearance_FloatingLabel);

            ColorStateList list = a.getColorStateList(
                    R.styleable.FloatingLabelControl_floatingLabelColor);

            if (list != null) {
                mFloatingLabelColor = list;
            }

            mFloatingLabel.setText(a.getString(R.styleable.FloatingLabelControl_floatingLabelText));

            int gravity = a.getInteger(R.styleable.FloatingLabelControl_android_gravity, -1);
            if (gravity != -1) {
                setGravity(gravity);
            }

            a.recycle();
        }

        mFloatingLabel.setTextAppearance(context, mFloatingLabelTextAppearance);
        mFloatingLabel.setTextColor(mFloatingLabelColor.getDefaultColor());

        setAddStatesFromChildren(true);
    }

    protected abstract void inflateControlView(Context context);

    protected void toggleFloatingLabel() {
        if (mFloating) {
            fadeInToTop(false);
        } else {
            fadeOutToBottom(false);
        }
    }

    protected void changeFloatingLabelState(int color, boolean animated) {
        mFloatingLabelColorAnimator.setIntValues(color);
        mFloatingLabelColorAnimator.setDuration(animated ? ANIMATION_DURATION : 0);
        mFloatingLabelColorAnimator.start();
    }

    protected void updateFloatingLabelColor() {
        if (mControlView.isEnabled() && (mControlView.isFocused() || mControlView.isPressed())) {
            changeFloatingLabelState(mFloatingLabelColor.getColorForState(FOCUSED_STATE_SET,
                    mFloatingLabelColor.getDefaultColor()), true);
        } else {
            changeFloatingLabelState(mFloatingLabelColor.getDefaultColor(), true);
        }
    }

    protected void fadeInToTop(boolean animated) {
        mFloatingLabelAnimator.alpha(1f)
                .translationY(0)
                .setDuration(animated ? ANIMATION_DURATION : 0)
                .start();
        mFloating = true;
    }

    protected void fadeOutToBottom(boolean animated) {
        mFloatingLabelAnimator.alpha(0f)
                .translationY(mFloatingLabel.getHeight() / 2)
                .setDuration(animated ? ANIMATION_DURATION : 0)
                .start();
        mFloating = false;
    }

    protected T getControlView() {
        return mControlView;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (mControlView == null) {
            return super.onCreateDrawableState(extraSpace);
        }
        updateFloatingLabelColor();
        return mControlView.getDrawableState();
    }

    @Override
    protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      mLaidOut = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mLaidOut) {
            toggleFloatingLabel();
            mLaidOut = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getDrawingRect(mBoundsRect);
        setTouchDelegate(new TouchDelegate(mBoundsRect, mControlView));
    }

    public CharSequence getFloatingLabelText() {
        return mFloatingLabel.getText();
    }

    public void setFloatingLabelText(CharSequence text) {
        mFloatingLabel.setText(text);
    }

    public ColorStateList getFloatingLabelColor() {
        return mFloatingLabelColor;
    }

    public void setFloatingLabelColor(ColorStateList colorStateList) {
        mFloatingLabelColor = colorStateList;
        updateFloatingLabelColor();
    }

    public void setFloatingLabelTextAppearance(Context context, int resId) {
        if (resId != mFloatingLabelTextAppearance) {
            mFloatingLabel.setTextAppearance(context, resId);
            mFloatingLabelTextAppearance = resId;
        }
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        if (isSaveEnabled()) {
            SavedState ss = new SavedState(superState);
            ss.mFloatingLabelColor = mFloatingLabelColor;
            return ss;
        }
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mFloatingLabelColor = ss.mFloatingLabelColor;
    }

    public static class SavedState extends BaseSavedState {
        private ColorStateList mFloatingLabelColor;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel in) {
            super(in);
            mFloatingLabelColor = in.readParcelable(ColorStateList.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(mFloatingLabelColor, flags);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
