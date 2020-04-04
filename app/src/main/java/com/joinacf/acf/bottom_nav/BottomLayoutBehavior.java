package com.joinacf.acf.bottom_nav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomLayoutBehavior extends CoordinatorLayout.Behavior<BottomNavigationView> {

public BottomLayoutBehavior() {
        super();
        }

public BottomLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        }

@Override
public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigationView child, View dependency) {
        boolean dependsOn = dependency instanceof FrameLayout;
        return dependsOn;
        }

@Override
public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, BottomNavigationView child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type)
        {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }

@Override
public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull BottomNavigationView child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
@ViewCompat.NestedScrollType int type)
        {
        if (dyConsumed > 0) {
        hideBottomNavigationView(child);
        } else if (dyConsumed < 0) {
        showBottomNavigationView(child);
        }
        }


private void hideBottomNavigationView(BottomNavigationView view) {
        view.animate().translationY(view.getHeight());
        }

private void showBottomNavigationView(BottomNavigationView view) {
        view.animate().translationY(0);
        }
        }