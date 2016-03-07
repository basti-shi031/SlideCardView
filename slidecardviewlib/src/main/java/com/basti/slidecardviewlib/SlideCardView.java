package com.basti.slidecardviewlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by SHIBW-PC on 2016/2/24.
 */
public class SlideCardView extends ViewGroup {

    private ViewDragHelper mDragger;
    private Point originPoint;//临界速度
    private SlideCardViewListener mListener;

    public void setLimitSpeed(float limitSpeed) {
        this.limitSpeed = limitSpeed;
    }

    private float limitSpeed = 500;

    enum Orientation {TOP, LEFT, BOTTOM, RIGHT}


    public SlideCardView(Context context) {
        this(context, null);
    }

    public SlideCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        originPoint = new Point();
        initViewDragHelper();
    }

    private void initViewDragHelper() {
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                //xvel 和 yvel 分别是childView在x y方向上的速度

                if (Math.abs(xvel) > limitSpeed || Math.abs(yvel) > limitSpeed) {
                    float childWidth = releasedChild.getMeasuredWidth();
                    float childHeight = releasedChild.getMeasuredWidth();

                    float slideCardViewHeight = getMeasuredHeight();
                    float slideCardViewWidth = getMeasuredWidth();

                    float dividerX = (slideCardViewWidth - childWidth) / 2;
                    float dividerY = (slideCardViewHeight - childHeight) / 2;

                    Orientation orientation = calculateOrientation(xvel, yvel, dividerX, dividerY);

                    removeViewTranslate(releasedChild, orientation, xvel, yvel);

                } else {
                    mDragger.settleCapturedViewAt(originPoint.x, originPoint.y);
                    invalidate();
                }
            }
        });
    }


    //消失动画
    private void removeViewTranslate(final View releasedChild, Orientation orientation, float xvel, float yvel) {

        float duration = 0;
        float start = 0;
        float end = 0;
        String direction = "";
        float offset = 0;

        switch (orientation) {
            case TOP:
                direction = "Y";
                start = releasedChild.getY();
                end = -releasedChild.getMeasuredHeight();
                offset = start - end;
                duration = offset / Math.abs(yvel);
                break;
            case BOTTOM:
                direction = "Y";
                start = releasedChild.getY();
                end = getMeasuredHeight() + releasedChild.getMeasuredHeight();
                offset = end - start;
                duration = offset / Math.abs(yvel);
                break;
            case LEFT:
                direction = "X";
                start = releasedChild.getX();
                end = -releasedChild.getMeasuredWidth();
                offset = start - end;
                duration = offset / Math.abs(xvel);
                break;
            case RIGHT:
                direction = "X";
                start = releasedChild.getX();
                end = getMeasuredWidth() + releasedChild.getMeasuredWidth();
                offset = end - start;
                duration = offset / Math.abs(xvel);
                break;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(releasedChild, direction, start, end);
        objectAnimator.setDuration(500);
        objectAnimator.start();
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (mListener != null){
                    int index = indexOfChild(releasedChild);
                    mListener.deleteFinished(index,releasedChild);
                }
                removeView(releasedChild);
            }
        });


    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    private Orientation calculateOrientation(float xvel, float yvel, float dividerX, float dividerY) {

        if (xvel == 0) {
            if (yvel > 0) return Orientation.BOTTOM;
            else return Orientation.TOP;
        }

        yvel = -yvel;

        float k = dividerY / dividerX;
        float speedK = yvel / xvel;
        if (speedK > -k && speedK < k) {
            if (xvel > 0) return Orientation.RIGHT;
            else return Orientation.LEFT;
        }
        if (speedK < -k || speedK > k) {
            if (yvel > 0) return Orientation.TOP;
            else return Orientation.BOTTOM;
        }

        return Orientation.LEFT;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            int cl = (getMeasuredWidth() - childWidth) / 2;
            int ct = (getMeasuredHeight() - childHeight) / 2;
            int cr = cl + childWidth;
            int cb = ct + childHeight;

            child.layout(cl, ct, cr, cb);
            if (i == childCount - 1) {
                originPoint.set(cl, ct);
                Log.i("onLayout", cl + "   " + ct);
            }
        }
    }

    @Override
    public void addView(View child) {
        //super.addView(child);
        addView(child, 0);
    }

    public void setSlideCardViewListener(SlideCardViewListener mListener) {
        this.mListener = mListener;
    }
}
