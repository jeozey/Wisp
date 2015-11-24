package com.rj.view.button;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rj.view.R;
import com.rj.view.listview.HorizontalListView;

import java.util.ArrayList;
import java.util.List;

public class PhoneHorizontalBtns extends RelativeLayout implements
        OnItemClickListener {
    private static final String TAG = "PhoneHorizontalBtns";
    private List<CustomWidgetButton> listDatas = new ArrayList<CustomWidgetButton>();
    private Context context;
    private HorizontalBtnsCallBack horizontalBtnsCallBack;
    private ImageView moreLeftImageView, moreRightImageView;
    private HorizontalListView listView;
    private MyAdapter adapter;
    private int preSelectedItem = -2;

    public interface HorizontalBtnsCallBack {
        void callBack(CustomWidgetButton info);
    }

    public PhoneHorizontalBtns(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public PhoneHorizontalBtns(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public PhoneHorizontalBtns(Context context) {
        super(context);
        this.context = context;
    }

    public PhoneHorizontalBtns(Context context,
                               List<CustomWidgetButton> listDatas) {
        super(context);
        this.context = context;
        if (listDatas != null) {
            this.listDatas = listDatas;
        }
    }

    public void updateBtn(CustomWidgetButton button) {
        try {
            for (CustomWidgetButton btn : listDatas) {
                if (btn.getTitle().equals(button.getTitle())) {
                    btn.setNum(button.getNum());
                }
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // private void checkPadding(int parentWidth){
    // int itemWidth = PixelTool.dip2px(context, 60);
    // padding = (parentWidth - 4*itemWidth)/12;
    // Log.e(TAG,"checkPadding:"+padding+" parentWidth:"+parentWidth+" itemWidth:"+itemWidth);
    // }
    //
    // int padding = 10;
    public void init(List<CustomWidgetButton> listDatas,
                     final HorizontalBtnsCallBack horizontalBtnsCallBack) {
//		Log.e(TAG, "init:" + listDatas);
        if (listDatas != null) {
            this.listDatas = listDatas;
        }
        // DisplayMetrics dm = new DisplayMetrics();
        // activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // checkPadding(dm.widthPixels);

        this.horizontalBtnsCallBack = horizontalBtnsCallBack;
        // 初始化值
        preSelectedItem = -2;

        // 初始化view
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.phone_horizontal_buttons_layout,
                null);
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        listView = (HorizontalListView) view.findViewById(R.id.btns_listview);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        moreLeftImageView = (ImageView) view.findViewById(R.id.more_left_img);
        moreRightImageView = (ImageView) view.findViewById(R.id.more_right_img);

        listView.setOnHasLeftOrRight(new HorizontalListView.OnHasLeftOrRight() {

            @Override
            public void noLeftDisVisibleView() {
                moreLeftImageView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void hasLeftDisVisibleView() {
                moreLeftImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void noRightDisVisibleView() {
                moreRightImageView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void hasRightDisVisibleView() {
                moreRightImageView.setVisibility(View.VISIBLE);
            }
        });

        try {
            // 默认点击项目
            boolean flg = false;
            int index = 0;
            for (CustomWidgetButton customWidgetButton : listDatas) {
                if (lastChooeseItem != null && index == 0 && lastChooeseItem.getTitle().equals(customWidgetButton.getTitle())) {
                    customWidgetButton.setChooese(true);
                    preSelectedItem = index;
                }
                if ("true".equals(customWidgetButton.getIsclick())) {
                    Log.e(TAG, "默认点击:" + customWidgetButton);
                    flg = true;
                    horizontalBtnsCallBack.callBack(listDatas.get(index));
                    reSetSelectBackground(index);
                }
                index++;
            }
            //默认选择第一项（非 更多项）
            if (!flg && !"更多".equals(listDatas.get(0).getTitle())) {
                reSetSelectBackground(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static CustomWidgetButton lastChooeseItem = null;

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listDatas.size();// 设置内容的个数
        }

        @Override
        public Object getItem(int position) {
            return listDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final CustomWidgetButton info = listDatas.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.bottom_button_layout, null);
                // FrameLayout parentFrameLayout =
                // (FrameLayout)convertView.findViewById(R.id.buttomFrameLayout);
                // parentFrameLayout.setPadding(padding, 0, padding, 0);

                MyHolder holder = new MyHolder();
                holder.imgShow = (ImageView) convertView
                        .findViewById(R.id.imgShow);
                holder.imgNum = (TextView) convertView
                        .findViewById(R.id.imgNum);
                holder.imgTitle = (TextView) convertView
                        .findViewById(R.id.imgTitle);
                convertView.setTag(holder);
            }

            final MyHolder holder = (MyHolder) convertView.getTag();
            Log.e(TAG, "info.isChooese():" + info.isChooese());
            if (info.isChooese()) {
                Log.e(TAG, "getAfterImg:" + info.getAfterImg());
                holder.imgShow.setImageDrawable(info.getAfterImg());
                holder.imgTitle.setTextColor(context.getResources().getColor(
                        R.color.theme));
                lastChooeseItem = info;
            } else {
                holder.imgShow.setImageDrawable(info.getBeforeImg());
                holder.imgTitle.setTextColor(context.getResources().getColor(
                        R.color.theme_menu_txt_color));
            }
            holder.imgNum.setText("" + info.getNum());
            holder.imgTitle.setText(info.getTitle());
            if (info.getNum() <= 0) {
                holder.imgNum.setVisibility(View.GONE);
            } else {
                holder.imgNum.setVisibility(View.VISIBLE);
            }

            // Log.i("GuLang", "getView info=" + info);

            /**
             * 要实现学长所说的功能，必须配套这个才能实现，因为布局会重用
             *
             * @author GuLang

            if (preSelectedItem == position) {
            convertView
            .setBackgroundResource(R.drawable.shape_horizontal_buttons);
            } else {
            convertView.setBackgroundResource(android.R.color.transparent);
            }*/

            return convertView;
        }

        class MyHolder {
            public ImageView imgShow;
            public TextView imgNum;
            public TextView imgTitle;
        }

    }

    /**
     * 重新设置背景图片
     *
     * @param location
     * @author GuLang
     */
    private synchronized void reSetSelectBackground(int location) {
        Log.i("GuLang", "preSelectedItem=" + preSelectedItem + ",location="
                + location + ",left=" + listView.getLeftViewAdapterIndex()
                + ",right=" + listView.getRightViewAdapterIndex());
        if (preSelectedItem != location) {
            /*
             * 如果之前选中的在可视界面里，则将选中背景去掉，否则不处理，借助适配器类中的getView()方法来完成这个效果
			 * 
			 * if (preSelectedItem <= listView.getRightViewAdapterIndex() &&
			 * preSelectedItem >= listView.getLeftViewAdapterIndex()) {
			 * 
			 * View preSelectedItemView = listView.getChildAt(preSelectedItem -
			 * listView.getLeftViewAdapterIndex()); if (preSelectedItemView !=
			 * null) { Log.i("GuLang", "preSelectedItemView != null");
			 * preSelectedItemView
			 * .setBackgroundResource(android.R.color.transparent); } }
			 * 
			 * View selectedItemView = listView.getChildAt(location -
			 * listView.getLeftViewAdapterIndex());
			 * 
			 * if (selectedItemView != null) { Log.i("GuLang",
			 * "selectedItemView != null"); selectedItemView
			 * .setBackgroundResource(R.drawable.shape_horizontal_buttons); }
			 * preSelectedItem = location;
			 */
            try {
                if (preSelectedItem >= 0) {
                    CustomWidgetButton oldMenu = listDatas.get(preSelectedItem);
                    if (oldMenu != null) {
                        oldMenu.setChooese(false);
                    }
                }
                CustomWidgetButton newMenu = listDatas.get(location);
                newMenu.setChooese(true);
                preSelectedItem = location;
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // Log.i("GuLang", "onItemClick position=" + position);
        horizontalBtnsCallBack.callBack(listDatas.get(position));
        reSetSelectBackground(position);
    }

}
