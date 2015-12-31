package com.rj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rj.view.button.CustomWidgetButton;

import java.util.List;

public class MenuButtonsLayout extends LinearLayout {
    private static final String TAG = "MenuButtonsLayout";
    private Context context;
    private List<CustomWidgetButton> listData;
    private LayoutInflater layoutInflater;
    private ListView listView;
    private ImageView moreTopImg, moreBottomImg;
    private MenuButtonLayoutCallBack menuButtonLayoutCallBack;
    private int preSelectedItem = -1;
    private ListViewAdapter listViewAdapter;

    public interface MenuButtonLayoutCallBack {
        int TYPE_LIST_ITEM = 1;
        int TYPE_SETTING = 2;
        int TYPE_LOGOUT = 3;

        /**
         * 回调方法
         *
         * @param type  点击的类型: TYPE_LIST_ITEM:表示上部中ListView中某一项点击后产生的回调
         *              TYPE_SETTING：表示点击设置项后产生的回调 TYPE_LOGOUT：表示点击注销后产生的回调
         * @param inifo 当点击的类型为TYPE_LIST_ITEM时，该参数不为空，且封装着点击项的信息；当为其它类型的时候为null
         */
        void onClickCallBack(int type, CustomWidgetButton inifo);
    }

    public MenuButtonsLayout(Context context) {
        super(context);
        this.context = context;
    }

    public MenuButtonsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    // 更新按钮条数
    public void updateBtn(CustomWidgetButton button) {
        try {
            for (CustomWidgetButton btn : listData) {
                if (btn.getTitle().equals(button.getTitle())) {
                    btn.setNum(button.getNum());
                }
            }
            listViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void init(List<CustomWidgetButton> listData,
                     final MenuButtonLayoutCallBack menuButtonLayoutCallBack) {
        Log.e(TAG, "init");
        this.listData = listData;
        this.menuButtonLayoutCallBack = menuButtonLayoutCallBack;
        preSelectedItem = -1;
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(context);
            View menuButtonsLyt = layoutInflater.inflate(
                    R.layout.menu_buttons_lyt, null);
            moreTopImg = (ImageView) menuButtonsLyt
                    .findViewById(R.id.more_top_img);
            moreBottomImg = (ImageView) menuButtonsLyt
                    .findViewById(R.id.more_botton_img);
            moreTopImg.setVisibility(View.INVISIBLE);
            moreBottomImg.setVisibility(View.INVISIBLE);

            listView = (ListView) menuButtonsLyt.findViewById(R.id.menu_lv);
            listViewAdapter = new ListViewAdapter();
            listView.setAdapter(listViewAdapter);

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    reSetSelectBackground(position);
                    view.startAnimation(AnimationUtils.loadAnimation(context,
                            R.anim.menu_item_in));

                    menuButtonLayoutCallBack.onClickCallBack(
                            MenuButtonLayoutCallBack.TYPE_LIST_ITEM,
                            MenuButtonsLayout.this.listData.get(position));
                }
            });
            listView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view,
                                                 int scrollState) {
                    // Log.e(TAG, "onScrollStateChanged");
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    // Log.i(TAG, "onScroll firstVisibleItem=" +
                    // firstVisibleItem + ",visibleItemCount="
                    // + visibleItemCount + ",totalItemCount=" +
                    // totalItemCount);
                    final View firstVisibleView = listView.getChildAt(0);
                    if (firstVisibleView != null) {
                        if ((firstVisibleItem == 0 && firstVisibleView.getTop() < 0)
                                || firstVisibleItem > 0) {
                            // Log.i(TAG, "top还有");
                            moreTopImg.setVisibility(View.VISIBLE);
                        } else {
                            // Log.e(TAG, "top没有了");
                            moreTopImg.setVisibility(View.INVISIBLE);
                        }
                    }

                    final View lastVisibleView = listView
                            .getChildAt(visibleItemCount - 1);
                    if (lastVisibleView != null) {
                        if ((firstVisibleItem + visibleItemCount) < totalItemCount
                                || ((firstVisibleItem + visibleItemCount) == totalItemCount && lastVisibleView
                                .getBottom() >= view.getBottom())) {
                            moreBottomImg.setVisibility(View.VISIBLE);
                        } else {
                            moreBottomImg.setVisibility(View.INVISIBLE);
                        }
                        // Log.e(TAG, "lastVisibleView.getBottom()=" +
                        // lastVisibleView.getBottom()
                        // + ",view.getBottom()=" + view.getBottom());
                    }

                }
            });

            addView(menuButtonsLyt, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);

//			View settingView = menuButtonsLyt
//					.findViewById(R.id.menu_item_setting);
//			View logoutView = menuButtonsLyt
//					.findViewById(R.id.menu_item_logout);
//			settingView.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					MenuButtonsLayout.this.menuButtonLayoutCallBack
//							.onClickCallBack(
//									MenuButtonLayoutCallBack.TYPE_SETTING, null);
//				}
//			});
//			logoutView.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					MenuButtonsLayout.this.menuButtonLayoutCallBack
//							.onClickCallBack(
//									MenuButtonLayoutCallBack.TYPE_LOGOUT, null);
//				}
//			});
        }

        listViewAdapter.notifyDataSetChanged();

        // 默认点击项目
        int index = 0;
        for (CustomWidgetButton customWidgetButton : listData) {
            if ("true".equals(customWidgetButton.getIsclick())) {
                Log.e(TAG, "默认点击:" + customWidgetButton);
                // View view = listView.getChildAt(index);
                // view.startAnimation(AnimationUtils.loadAnimation(context,
                // R.anim.menu_item_in));
                reSetSelectBackground(index);

                menuButtonLayoutCallBack.onClickCallBack(
                        MenuButtonLayoutCallBack.TYPE_LIST_ITEM,
                        MenuButtonsLayout.this.listData.get(index));
            }
            index++;
        }
    }

    /**
     * 重新设置背景图片
     *
     * @param location
     * @author GuLang
     */
    private synchronized void reSetSelectBackground(int location) {
        Log.e(TAG, "reSetSelectBackground:" + location + " preSelectedItem:"
                + preSelectedItem);
        if (preSelectedItem >= 0 && preSelectedItem < listData.size()) {
            listData.get(preSelectedItem).setIsclick("false");
        }
        if (location >= 0 && location < listData.size()) {
            listData.get(location).setIsclick("true");
        }

        listViewAdapter.notifyDataSetChanged();
    }

    static int i = 0;

    class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return MenuButtonsLayout.this.listData.size();
        }

        @Override
        public Object getItem(int position) {
            return MenuButtonsLayout.this.listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder = null;
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.a_menu_button_lyt, null);
                holder = new MyHolder();
                holder.icon = (ImageView) convertView
                        .findViewById(R.id.menu_item_icon);
                holder.title = (TextView) convertView
                        .findViewById(R.id.menu_item_title);
                holder.num = (TextView) convertView
                        .findViewById(R.id.menu_item_num_tv);
                holder.totalLyt = (FrameLayout) convertView
                        .findViewById(R.id.menu_item_lyt);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }

            final CustomWidgetButton info = MenuButtonsLayout.this.listData
                    .get(position);
            holder.icon.setImageDrawable(info.getBeforeImg());
            holder.title.setText(info.getTitle());
            holder.num.setText("" + info.getNum());
            if (info.getNum() > 0) {
                holder.num.setVisibility(View.VISIBLE);
            } else {
                holder.num.setVisibility(View.INVISIBLE);
            }

            if ("true".equals(info.getIsclick())) {
                Log.e(TAG, "info.getTitle():" + info.getTitle());
                preSelectedItem = position;
                holder.totalLyt
                        .setBackgroundResource(R.drawable.menu_selected_bg);
            } else {
                holder.totalLyt
                        .setBackgroundResource(android.R.color.transparent);
            }

            return convertView;
        }

        private class MyHolder {
            public ImageView icon;
            public TextView title;
            public TextView num;
            public FrameLayout totalLyt;
        }

    }

}
