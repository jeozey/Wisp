package com.rj.view.listview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rj.view.R;

/***
 * @version V1.0 Copyright (C) 2012, Rongji Enterprise.
 * @Title: CornerListView.java
 * @Package com.rj.framework
 * @Description: TODO
 */
public class CornerListView extends ListView {
    private Context context;

    public CornerListView(Context context) {
        super(context);
        this.context = context;
    }

    public CornerListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public CornerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                setSelector(context.getResources().getDrawable(R.drawable.menu_list_corner_round));
                break;

        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                this.setSelection(-1);
                break;
            case MotionEvent.ACTION_DOWN:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                int itemnum = pointToPosition(x, y);
                if (itemnum == AdapterView.INVALID_POSITION)
                    break;
                else {
                    if (itemnum == 0) {
                        if (itemnum == (getAdapter().getCount() - 1)) {
                            setSelector(R.drawable.menu_list_corner_round);
                        } else {
                            setSelector(R.drawable.menu_list_corner_round_top);
                        }
                    } else if (itemnum == (getAdapter().getCount() - 1)) {
                        setSelector(R.drawable.menu_list_corner_round_bottom);
                    } else {
                        setSelector(R.drawable.menu_list_corner_round_center);
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
