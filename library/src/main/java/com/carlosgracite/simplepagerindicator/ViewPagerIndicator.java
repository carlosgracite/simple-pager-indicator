package com.carlosgracite.simplepagerindicator;

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ViewPagerIndicator extends LinearLayout implements ViewPager.OnPageChangeListener {

    private @ColorInt int selectedColor;
    private @ColorInt int unselectedColor;

    private ViewPager viewPager;

    private ArgbEvaluator evaluator;

    // Transient variables to control animation
    private int currentState;
    private int currentAnimatingPosition = -1;
    private int targetPosition = -1;

    public ViewPagerIndicator(Context context) {
        super(context);
        init(context, null, R.attr.vpi_viewPagerIndicatorStyle);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.vpi_viewPagerIndicatorStyle);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, defStyleAttr, 0);
        selectedColor = a.getColor(R.styleable.ViewPagerIndicator_vpi_selectedColor, Color.BLUE);
        unselectedColor = a.getColor(R.styleable.ViewPagerIndicator_vpi_unselectedColor, Color.YELLOW);
        float dividerSize = a.getDimension(R.styleable.ViewPagerIndicator_vpi_dividerSize, 20.0f);
        a.recycle();

        setShowDividers(SHOW_DIVIDER_MIDDLE);
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.setIntrinsicWidth((int)dividerSize);
        setDividerDrawable(drawable);
        setDividerPadding((int)dividerSize);

        setGravity(Gravity.CENTER);

        evaluator = new ArgbEvaluator();

        if (isInEditMode()) {
            for (int i = 0; i < 3; i++) {
                IndicatorView indicator = new IndicatorView(getContext());
                indicator.setColor(i == 0 ? selectedColor : unselectedColor);
                ViewGroup.LayoutParams params = new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                addView(indicator, params);
            }
        }
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;

        viewPager.addOnPageChangeListener(this);

        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            addIndicator();
        }
    }

    private void addIndicator() {
        IndicatorView indicator = new IndicatorView(getContext());
        indicator.setColor(unselectedColor);

        indicator.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < getChildCount(); i++) {
                    if (v == getChildAt(i)) {
                        viewPager.setCurrentItem(i, true);
                    }
                }
            }
        });

        ViewGroup.LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(indicator, params);
    }

    // =============================================================================================
    //  ViewPager Callbacks
    // =============================================================================================

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        @ColorInt int colorOut = (int)evaluator.evaluate(positionOffset, selectedColor, unselectedColor);
        @ColorInt int colorIn = (int)evaluator.evaluate(Math.abs(1 - positionOffset), selectedColor, unselectedColor);

        ((IndicatorView)getChildAt(position)).setColor(colorOut);

        if (getChildCount() > position+1) {
            ((IndicatorView) getChildAt(position + 1)).setColor(colorIn);

            if (currentState == ViewPager.SCROLL_STATE_SETTLING) {
                if (currentAnimatingPosition < 0) {
                    currentAnimatingPosition = position;

                } else if (currentAnimatingPosition != position) {
                    if (targetPosition < currentAnimatingPosition) {
                        ((IndicatorView) getChildAt(currentAnimatingPosition + 1)).setColor(unselectedColor);
                    } else {
                        ((IndicatorView) getChildAt(currentAnimatingPosition)).setColor(unselectedColor);
                    }
                    currentAnimatingPosition = position;
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (currentState == ViewPager.SCROLL_STATE_SETTLING) {
            targetPosition = position;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (currentState == ViewPager.SCROLL_STATE_SETTLING &&
                state == ViewPager.SCROLL_STATE_IDLE) {
            currentAnimatingPosition = -1;
        }
        currentState = state;
    }
}
