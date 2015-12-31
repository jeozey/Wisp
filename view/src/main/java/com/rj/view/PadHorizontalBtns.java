package com.rj.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rj.view.button.CustomWidgetButton;
import com.rj.view.listview.CornerListView;
import com.rj.view.listview.HorizontalListView;

import java.util.ArrayList;
import java.util.List;

public class PadHorizontalBtns extends RelativeLayout implements
        OnItemClickListener {
    private static final String TAG = PadHorizontalBtns.class.getName();
    private List<CustomWidgetButton> listDatas = new ArrayList<CustomWidgetButton>();
    private Context context;
    private HorizontalBtnsCallBack horizontalBtnsCallBack;
    private ImageView moreLeftImageView, moreRightImageView;
    private HorizontalListView listView;
    private MyAdapter adapter;
    private View contentView, screenView, fontSizeView;
    private View viewMargin;
    // private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private CustomWidgetButton fontSizeInfo;

    public interface HorizontalBtnsCallBack {
        int CALL_BACK_MSG_TYPE_SCREEN = 0;
        int CALL_BACK_MSG_TYPE_COMMON_ITEM = 1;
        int CALL_BACK_MSG_TYPE_POP_ITEM = 2;
        int CALL_BACK_MSG_TYPE_FONTSIZE = 3;
        int CALL_BACK_MSG_TYPE_CLOSE = 4;

        /**
         * 回调函数
         *
         * @param type        引起回调类型： CALL_BACK_MSG_TYPE_SCREEN表示屏幕按钮后引起的回调；
         *                    CALL_BACK_MSG_TYPE_COMMON_ITEM表示横向ListView中某一项
         *                    (普通的，没有弹出PopWindow的)引起的回调
         *                    CALL_BACK_MSG_TYPE_POP_ITEM表示横向ListView中某一项
         *                    (有弹出PopWindow的)引起的回调 CALL_BACK_MSG_TYPE_FONTSIZE表示点击的是字体设置
         *                    CALL_BACK_MSG_TYPE_CLOSE表示点击的是关闭按钮
         * @param info        info选中项 当回调类型为CALL_BACK_MSG_TYPE_SCREEN时，info为null
         *                    当回调类型为CALL_BACK_MSG_TYPE_COMMON_ITEM时
         *                    ，info为MyBottomInof类型的封装数据
         *                    当回调类型为CALL_BACK_MSG_TYPE_POP_ITEM时
         *                    ，info为MyBottomInof类型的封装数据
         *                    当回调类型为CALL_BACK_MSG_TYPE_FONTSIZE时，info为null
         *                    当回调类型为CALL_BACK_MSG_TYPE_CLOSE时，info为null
         * @param popPosition 当回调类型为CALL_BACK_MSG_TYPE_FONTSIZE或者CALL_BACK_MSG_TYPE_POP_ITEM时
         *                    ，这一项有用,表示选中PopWindow的第几项
         */
        void callBack(int type, CustomWidgetButton info, int popPosition);
    }

    public PadHorizontalBtns(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public PadHorizontalBtns(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public PadHorizontalBtns(Context context) {
        super(context);
        this.context = context;
    }

    public PadHorizontalBtns(Context context, List<CustomWidgetButton> listDatas) {
        super(context);
        this.context = context;
        this.listDatas = listDatas;
    }

    private void setFontSizeBtn(CustomWidgetButton fontSizeInfo) {
        if (fontSizeInfo == null) {
            fontSizeView.setVisibility(View.GONE);
        } else {
            fontSizeView.setVisibility(View.VISIBLE);
            MyHolder fontSizeHolder = new MyHolder();
            fontSizeHolder.iconImgv = (ImageView) fontSizeView
                    .findViewById(R.id.icon_imgv);
            fontSizeHolder.titleTv = (TextView) fontSizeView
                    .findViewById(R.id.titleTv);
            fontSizeHolder.dropDownImgv = (ImageView) fontSizeView
                    .findViewById(R.id.drop_down_imgv);
            fontSizeHolder.iconImgv.setImageDrawable(fontSizeInfo.getBeforeImg());
            fontSizeHolder.titleTv.setText(fontSizeInfo.getTitle());
            if (fontSizeInfo.getPopData() != null
                    && fontSizeInfo.getPopData().size() > 0) {
                fontSizeHolder.dropDownImgv.setVisibility(View.VISIBLE);
            } else {
                fontSizeHolder.dropDownImgv.setVisibility(View.GONE);
            }
            fontSizeView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopWindow(v, PadHorizontalBtns.this.fontSizeInfo,
                            HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_FONTSIZE);
                }
            });
        }
    }

    private int screenWidth;

    public void init(int screenWidth, CustomWidgetButton screenBtnInfo,
                     List<CustomWidgetButton> listDatas,
                     CustomWidgetButton fontSizeInfo,
                     HorizontalBtnsCallBack horizontalBtnsCallBack) {
        Log.e(TAG, "init");
        this.screenWidth = screenWidth;
        this.listDatas = listDatas;
        this.fontSizeInfo = fontSizeInfo;
        this.horizontalBtnsCallBack = horizontalBtnsCallBack;
        // 初始化view
        layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = layoutInflater.inflate(
                R.layout.pad_horizontal_buttons_layout, null);
        addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        viewMargin = contentView.findViewById(R.id.view_margin);
        // 设置全屏或者退出全屏
        screenView = contentView.findViewById(R.id.menu_screen_lyt);
        if (screenBtnInfo != null) {
            screenView.setVisibility(screenBtnInfo.getVisiable());
            MyHolder myHolder = new MyHolder();
            myHolder.iconImgv = (ImageView) screenView.findViewById(R.id.icon_imgv);
            myHolder.titleTv = (TextView) screenView.findViewById(R.id.titleTv);
            myHolder.dropDownImgv = (ImageView) screenView
                    .findViewById(R.id.drop_down_imgv);
            setValues(myHolder, screenBtnInfo);
            screenView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PadHorizontalBtns.this.horizontalBtnsCallBack.callBack(
                            HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_SCREEN, null,
                            0);
                }
            });
            screenView.setTag(myHolder);
        }

        listView = (HorizontalListView) contentView
                .findViewById(R.id.btns_listview);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        moreLeftImageView = (ImageView) contentView
                .findViewById(R.id.more_left_img);
        moreRightImageView = (ImageView) contentView
                .findViewById(R.id.more_right_img);

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

        // 字体
        fontSizeView = contentView.findViewById(R.id.menu_font_lyt);
        setFontSizeBtn(fontSizeInfo);

        // 关闭
        View closeView = contentView.findViewById(R.id.menu_close_lyt);
        MyHolder closeHolder = new MyHolder();
        closeHolder.iconImgv = (ImageView) closeView
                .findViewById(R.id.icon_imgv);
        closeHolder.titleTv = (TextView) closeView.findViewById(R.id.titleTv);
        closeHolder.dropDownImgv = (ImageView) closeView
                .findViewById(R.id.drop_down_imgv);

        closeHolder.iconImgv.setImageDrawable(context.getApplicationContext()
                .getResources().getDrawable(R.mipmap.ic_close));
        closeHolder.titleTv.setText("关闭");
        closeHolder.dropDownImgv.setVisibility(View.GONE);
        closeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PadHorizontalBtns.this.horizontalBtnsCallBack.callBack(
                        HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_CLOSE, null,
                        0);
            }
        });
    }

    public void update(CustomWidgetButton screenBtnInfo,
                       CustomWidgetButton fontSizeBtnInfo,
                       List<CustomWidgetButton> listDatas) {
        // 改版全屏按钮状态
        MyHolder holder = (MyHolder) screenView.getTag();
//		if (screenBtnInfo!=null&&screenBtnInfo.getNum() == 1) {
//			viewMargin.setVisibility(View.VISIBLE); // 为了占位置，给网络控件
//		} else {
//			viewMargin.setVisibility(View.GONE); // 不用占位置，给网络控件
//		}
        setValues(holder, screenBtnInfo);

        if (fontSizeBtnInfo != null) {
            setFontSizeBtn(fontSizeBtnInfo);

            // MyHolder holder1 = (MyHolder) fontSizeView.getTag();
            // setValues(holder1, fontSizeBtnInfo);

            fontSizeInfo = fontSizeBtnInfo;
        }
        this.listDatas = listDatas;

        this.adapter.notifyDataSetChanged();

    }

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
            MyHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.horizontal_list_item_lyt, null);
                holder = new MyHolder();
                holder.iconImgv = (ImageView) convertView
                        .findViewById(R.id.icon_imgv);
                holder.titleTv = (TextView) convertView
                        .findViewById(R.id.titleTv);
                holder.dropDownImgv = (ImageView) convertView
                        .findViewById(R.id.drop_down_imgv);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }

            final CustomWidgetButton info = listDatas.get(position);
            setValues(holder, info);
            return convertView;
        }

    }

    public class MyHolder {
        ImageView iconImgv;
        TextView titleTv;
        ImageView dropDownImgv;
    }

    /**
     * 给每个选项赋值
     *
     * @param holder
     * @param info
     */
    public void setValues(final MyHolder holder, final CustomWidgetButton info) {
        if (holder != null && info != null) {
            holder.iconImgv.setBackgroundDrawable(info.getBeforeImg());
            if (info.getBeforeImg() == null) {
                holder.iconImgv.setVisibility(View.GONE);
            } else {
                holder.iconImgv.setVisibility(View.VISIBLE);
            }
            holder.titleTv.setText("" + info.getTitle());
            if (info.getPopData() != null && info.getPopData().size() > 0) {
                holder.dropDownImgv.setVisibility(View.VISIBLE);
            } else {
                holder.dropDownImgv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Log.i("GuLang", "onItemClick position=" + position + ",id=" + id);
        final CustomWidgetButton info = listDatas.get(position);
        Log.i("GuLang", "onItemClick-->" + info);
        if (info.getPopData() == null) {
            horizontalBtnsCallBack.callBack(
                    HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_COMMON_ITEM,
                    listDatas.get(position), 0);
        } else {
            showPopWindow(view, listDatas.get(position),
                    HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_POP_ITEM);
        }

    }

    private PopupWindow formTopBtnPopupWindow;

    public void showPopWindow(View parentView, final CustomWidgetButton info,
                              final int callBackType) {
        if (info != null && info.getPopData() != null
                && info.getPopData().size() > 0) {
            View popupLayout = LayoutInflater.from(context).inflate(
                    R.layout.form_top_btn_popupwindow, null);
            CornerListView listView = (CornerListView) popupLayout
                    .findViewById(R.id.form_top_btn_popup_listv);
            FormTopBtnPopupAdapter formTopBtnPopupAdapter = new FormTopBtnPopupAdapter(
                    context, info.getPopData(), R.layout.form_top_btn_list_item);
            listView.setAdapter(formTopBtnPopupAdapter);
            formTopBtnPopupWindow = new PopupWindow(popupLayout,
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            formTopBtnPopupWindow.setFocusable(true);
            formTopBtnPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            formTopBtnPopupWindow.setWidth(screenWidth / 7);

            formTopBtnPopupWindow.showAsDropDown(parentView);
            Log.i("GuLang", "all-->" + info.getPopData());
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Log.i("GuLang", "position=" + position);
                    Log.i("GuLang", "one-->" + info.getPopData().get(position));
                    horizontalBtnsCallBack
                            .callBack(
                                    HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_COMMON_ITEM,
                                    info.getPopData().get(position), 0);
                    if (formTopBtnPopupWindow != null)
                        formTopBtnPopupWindow.dismiss();
                }
            });
        }
        // 志文学长编写
        // if (formTopBtnPopupWindow == null && info != null &&
        // info.getPopData() != null
        // && info.getPopData().size() > 0) {
        // View popupLayout = LayoutInflater.from(context).inflate(
        // R.layout.form_top_btn_popupwindow, null);
        // ListView listView = (ListView)
        // popupLayout.findViewById(R.id.form_top_btn_popup_listv);
        // FormTopBtnPopupAdapter formTopBtnPopupAdapter = new
        // FormTopBtnPopupAdapter(context,
        // info.getPopData(), R.layout.form_top_btn_list_item);
        // listView.setAdapter(formTopBtnPopupAdapter);
        // formTopBtnPopupWindow = new PopupWindow(popupLayout,
        // LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT);
        // formTopBtnPopupWindow.setFocusable(true);
        // formTopBtnPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        // // formTopBtnPopupWindow.setWidth(PixelTool.dip2px(context, 100));
        // formTopBtnPopupWindow.setWidth(100);
        // //
        // AutoSizeTool.setFormViewDocumentPopupWindow(formTopBtnPopupWindow);
        // // int position = -formTopBtnPopupWindow.getWidth() / 2
        // // + parentView.getWidth() / 2;
        // // Log.e("test3", "position1 " + position);
        // formTopBtnPopupWindow.showAsDropDown(parentView);
        // listView.setOnItemClickListener(new OnItemClickListener() {
        // @Override
        // public void onItemClick(AdapterView<?> parent, View view, int
        // position, long id) {
        // horizontalBtnsCallBack.callBack(
        // HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_COMMON_ITEM, info
        // .getPopData().get(position), 0);
        // Toast.makeText(context, "" + info.getPopData().get(position),
        // Toast.LENGTH_SHORT).show();
        // if (formTopBtnPopupWindow != null)
        // formTopBtnPopupWindow.dismiss();
        // }
        // });
        // } else {
        // // int position = -formTopBtnPopupWindow.getWidth() / 2
        // // + parentView.getWidth() / 2;
        // // Log.e("test3", "position1 " + position);
        // // formTopBtnPopupWindow.showAsDropDown(parentView, position, 4);
        // formTopBtnPopupWindow.showAsDropDown(parentView);
        // }
    }

    private List<CustomWidgetButton> popList;

    class PopWindowAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return popList.size();// 设置内容的个数
        }

        @Override
        public Object getItem(int position) {
            return popList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            MyHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.horizontal_list_item_lyt, null);
                holder = new MyHolder();
                holder.iconImgv = (ImageView) convertView
                        .findViewById(R.id.icon_imgv);
                holder.titleTv = (TextView) convertView
                        .findViewById(R.id.titleTv);
                holder.dropDownImgv = (ImageView) convertView
                        .findViewById(R.id.drop_down_imgv);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }

            final CustomWidgetButton info = popList.get(position);
            setValues(holder, info);
            return convertView;
        }

    }

}
