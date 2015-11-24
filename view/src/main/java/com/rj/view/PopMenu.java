package com.rj.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.rj.view.listview.CornerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PopMenu implements AdapterView.OnItemClickListener {
    public interface PopMenuOnItemClickListener {
        void onItemClick(int index);
    }

    private Activity activity;
    private PopupWindow popupWindow;
    // private GridView gridView;
    private CornerListView cornerListView;
    private List<Map<String, Object>> listData = null;
    private SimpleAdapter adapter = null;
    private PopMenuOnItemClickListener listener;
    private LayoutInflater inflater;

    public PopMenu(Activity activity, Handler handler, PopMenuOnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.listener = onItemClickListener;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.menu_setting_popmenu, null);
        // gridView = (GridView) view.findViewById(R.id.pop_setting_list);
        // gridView.setAdapter(new GridAdapter(handler, activity));
        // gridView.setOnItemClickListener(this);

        setListData();
        cornerListView = (CornerListView) view.findViewById(R.id.setting_list);
        adapter = new SimpleAdapter(activity, listData,
                R.layout.menu_setting_list_item, new String[]{"title", "drawable"},
                new int[]{R.id.menu_title, R.id.menu_ico});
//		cornerListView.setOnItemSelectedListener(PopMenu.this);
        cornerListView.setOnItemClickListener(PopMenu.this);
        cornerListView.setAdapter(adapter);

//		popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT);
        popupWindow = new PopupWindow(view, PixelTool.dip2px(activity, 120),
                LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(activity.getResources().getIdentifier(
                "PopupWindowAnim", "style", activity.getPackageName()));
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    /**
     * 设置列表数据
     */
    private void setListData() {
        listData = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", "注销");
        map.put("drawable", R.mipmap.icon_logout);
        listData.add(map);

        map = new HashMap<String, Object>();
        map.put("title", "设置");
        map.put("drawable", R.mipmap.icon_setting);
        listData.add(map);

    }  
  

	/*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		dismiss();
		if (listener != null) {
			listener.onItemClick(position);
		}

	}*/


    public void showAsDropDown(View parent) {
        popupWindow.showAsDropDown(parent, 0, -10);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        dismiss();
        if (listener != null) {
            listener.onItemClick(position);
        }
    }

}
