package com.example.yuhao.mynestedscrolling.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class SmLinearLayout extends LinearLayout implements NestedScrollingChild2 ,NestedScrollingParent {

    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;

    public SmLinearLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        getScrollingChildHelper();
    }


    public SmLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    private int mLastY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = mLastY - y;
                int oldY = getScrollY();
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                }
                mLastY = y - mScrollOffset[1];
                if (dy < 0) {
                    int newScrollY = Math.max(0, oldY + dy);
                    dy -= newScrollY - oldY;
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        mLastY -= mScrollOffset[1];
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopNestedScroll();
                break;
        }
        return true;
    }


    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
//        int maxY = getMeasuredHeight() - showHeight;
//        if (y > maxY) {
//            y = maxY;
//        }
//        if (y < 0) {
//            y = 0;
//        }
        super.scrollTo(x, y);
    }

    //初始化helper对象
    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mNestedScrollingChildHelper == null) {
            mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
            mNestedScrollingChildHelper.setNestedScrollingEnabled(true);
        }
        return mNestedScrollingChildHelper;
    }

    //初始化helper对象
    private NestedScrollingParentHelper getScrollingParentHelper() {
        if (mNestedScrollingParentHelper == null) {
            mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        }
        return mNestedScrollingParentHelper;
    }


    //实现一下接口
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }


    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return getScrollingChildHelper().startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        getScrollingChildHelper().stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return getScrollingChildHelper().hasNestedScrollingParent(type);
    }


    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow, int type) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type);
    }


    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
                                           int type) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //通知协作
        return true;
    }
    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        getScrollingParentHelper().onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);//fu buju xie做
    }
    @Override
    public void onStopNestedScroll(View target) {
        getScrollingParentHelper().onStopNestedScroll(target);
        stopNestedScroll();
    }
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //把子的消耗传上去
        dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, null);
    }
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX,velocityY,consumed);
    }
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityX);
//        return false;
    }
    @Override
    public int getNestedScrollAxes() {
        return 0;
    }
}
