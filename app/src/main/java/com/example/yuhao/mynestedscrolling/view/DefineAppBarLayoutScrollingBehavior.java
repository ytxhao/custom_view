package com.example.yuhao.mynestedscrolling.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class DefineAppBarLayoutScrollingBehavior extends  AppBarLayout.ScrollingViewBehavior {


    private static final String TAG = "DefineAppBarLayoutScrollingBehavior";
    public DefineAppBarLayoutScrollingBehavior() {
    }

    public DefineAppBarLayoutScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
//        child.setY(dependency.getY()+200);
       // return true;
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
      //  return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes) {
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes);

        //return true;
    }
}