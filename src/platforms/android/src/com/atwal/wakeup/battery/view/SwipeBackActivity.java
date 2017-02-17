package com.atwal.wakeup.battery.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public abstract class SwipeBackActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener {
    private SwipeBackLayout swipeBackLayout;
    private RelativeLayout container;
    public abstract int getLayoutId();
    public abstract void initViews();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        setContentView(layoutId);
        getSwipeBackLayout().setEnablePullToBack(false);
        initViews();
    }
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(getContainer());
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
        swipeBackLayout.addView(view);
    }

    private View getContainer() {
        container = new RelativeLayout(this);
        swipeBackLayout = new SwipeBackLayout(this);
        swipeBackLayout.setOnSwipeBackListener(this);
        container.addView(swipeBackLayout);
        return container;
    }
    public RelativeLayout getParentContainer(){
        return container;
    }
    public void setDragEdge(SwipeBackLayout.DragEdge dragEdge) {
        swipeBackLayout.setDragEdge(dragEdge);
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return swipeBackLayout;
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
    }

}
