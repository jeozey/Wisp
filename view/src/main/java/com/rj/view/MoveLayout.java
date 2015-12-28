package com.rj.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * @author GuLang
 */
public class MoveLayout extends RelativeLayout {
    private final static String TAG = MoveLayout.class.getName();
    private final static int MSG_MOVE_ABOVE_LYT_SCROLL = 111; // 移动aboveLyt的消息
    private final static int MSG_MOVE_ABOVE_LYT_RESET_LOCATION = 112; // 重置aboveLyt的位置
    private final static int MSG_MOVE_ABOVE_LYT_RESET_TEXT = 113; // 重置aboveLyt的文字

    private Context context;
    private List<MoveLayoutItemInfo> listDatas;
    private MoveLayoutCallBack moveLayoutCallBack;
    private LayoutInflater layoutInflater;

    private ListView listView;
    private RelativeLayout aboveLyt; // 前面的条
    private TextView aboveTv; // 前面的条中的TextView

    private MarginLayoutParams marginLayoutParams;

    private void handleMsg(Message msg) {
        switch (msg.what) {
            case MSG_MOVE_ABOVE_LYT_SCROLL: // 移动aboveLyt的消息
                marginLayoutParams = (MarginLayoutParams) aboveLyt
                        .getLayoutParams();
                marginLayoutParams.setMargins(0, msg.arg1, 0, 0);
                aboveLyt.requestLayout();

                break;
            case MSG_MOVE_ABOVE_LYT_RESET_LOCATION: // 重置aboveLyt的位置
                aboveLyt.setVisibility(View.VISIBLE);
                marginLayoutParams = (MarginLayoutParams) aboveLyt
                        .getLayoutParams();
                marginLayoutParams.setMargins(0, 0, 0, 0);
                aboveTv.setText(msg.obj.toString());
                aboveLyt.requestLayout();

                break;
            case MSG_MOVE_ABOVE_LYT_RESET_TEXT: // 重置aboveLyt的文字
                aboveLyt.setVisibility(View.VISIBLE);
                aboveTv.setText(msg.obj.toString());

                break;
            default:
                break;
        }
        msg.recycle();
    }

    public MoveLayout(Context context) {
        super(context);

    }

    public MoveLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MoveLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void clear() {
        try {
            listDatas.clear();
            moveLayoutListAdapter.notifyDataSetChanged();
            Log.e(TAG, "clear");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MoveLayoutListAdapter moveLayoutListAdapter;

    public void init(List<MoveLayoutItemInfo> listDatas,
                     MoveLayoutCallBack moveLayoutCallBack) {
        this.listDatas = listDatas;
        // if(listDatas!=null&&listDatas.size()>0){
        // //blank item
        // MoveLayoutItemInfo last = new MoveLayoutItemInfo();
        // last.setType(1);
        // listDatas.add(last);
        // }
        this.moveLayoutCallBack = moveLayoutCallBack;
        this.layoutInflater = LayoutInflater.from(context);
        setBackgroundColor(context.getResources()
                .getColor(R.color.more_apps_bg));

        listView = new ListView(context);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setFadingEdgeLength(0);
        listView.setDivider(new ColorDrawable(0xFFE5E5E7));
        listView.setDividerHeight((int) context.getResources().getDimension(
                R.dimen.more_apps_item_divider));
        addView(listView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        // 前面的文字条
        aboveLyt = (RelativeLayout) layoutInflater.inflate(
                R.layout.move_item_lyt, null);
        aboveLyt.findViewById(R.id.move_lyt_left_imgview)
                .setVisibility(View.GONE);
        aboveLyt.findViewById(R.id.move_lyt_num_tv)
                .setVisibility(View.GONE);
        aboveLyt.findViewById(R.id.go_imgview)
                .setVisibility(View.GONE);
        // aboveLyt.setBackgroundResource(R.drawable.gradient_color_move_list_item_lyt);
        // aboveTv = (TextView) aboveLyt.findViewById(R.id.move_lyt_title_tv);
        // if (listDatas.size() > 0) {
        // aboveLyt.setVisibility(View.VISIBLE);
        // aboveTv.setText(listDatas.get(0).getTitle());
        // aboveTv.setTextColor(Color.WHITE);
        // } else {
        // aboveLyt.setVisibility(View.GONE);
        // }
        // addView(aboveLyt, ViewGroup.LayoutParams.MATCH_PARENT,
        // ViewGroup.LayoutParams.WRAP_CONTENT);

		/*
         * 设置点击事件
		 */
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int location, long arg3) {
                MoveLayout.this.moveLayoutCallBack
                        .moveLayoutCallBack(MoveLayout.this.listDatas
                                .get(location));
            }
        });

		/*
		 * 设置适配
		 */
        moveLayoutListAdapter = new MoveLayoutListAdapter();
        listView.setAdapter(moveLayoutListAdapter);

		/*
		 * 设置滚动事件
		 * 
		 * listView.setOnScrollListener(new OnScrollListener() { private int
		 * preFirstVisibleItem = 0;
		 * 
		 * @Override public void onScrollStateChanged(AbsListView view, int
		 * scrollState) { }
		 * 
		 * private MoveLayoutItemInfo getPreTitleItemInfo(int firstVisibleItem)
		 * { int temp = firstVisibleItem; MoveLayoutItemInfo tempInfo = null;
		 * boolean isReach = false; while (temp >= 0) { tempInfo =
		 * MoveLayout.this.listDatas.get(temp); Log.i("GuLang",
		 * "getPreTitleItemInfo-->temp=" + temp); Log.i("GuLang",
		 * "getPreTitleItemInfo-->tempInfo=" + tempInfo);
		 * 
		 * if (tempInfo != null && tempInfo.getType() == 1) { isReach = true;
		 * break; } temp--; } if (isReach) { return tempInfo; } else { return
		 * null; } }
		 * 
		 * @Override public void onScroll(AbsListView view, int
		 * firstVisibleItem, int visibleItemCount, int totalItemCount) { int
		 * aboveLytHeight = aboveLyt.getHeight(); final View firstItemAtLook =
		 * view.getChildAt(0); final View secondItemAtLook = view.getChildAt(1);
		 * final MoveLayoutItemInfo firstItemInfoAtLook =
		 * MoveLayout.this.listDatas .get(firstVisibleItem); final
		 * MoveLayoutItemInfo secondItemInfoAtLook = MoveLayout.this.listDatas
		 * .get(firstVisibleItem + 1); if (firstItemAtLook != null &&
		 * secondItemAtLook != null && visibleItemCount < totalItemCount) { int
		 * secondTop = secondItemAtLook.getTop();
		 * 
		 * if (preFirstVisibleItem != firstVisibleItem) { MoveLayoutItemInfo
		 * tempMoveLayoutItemInfo = getPreTitleItemInfo(firstVisibleItem); if
		 * (tempMoveLayoutItemInfo != null) { Message message =
		 * Message.obtain(); message.what = MSG_MOVE_ABOVE_LYT_RESET_TEXT;
		 * message.obj = tempMoveLayoutItemInfo.getTitle(); handleMsg(message);
		 * }
		 * 
		 * }
		 * 
		 * if (secondItemInfoAtLook.getType() == 1 && secondTop < aboveLytHeight
		 * && secondTop > 0) { Message msg = Message.obtain(); msg.what =
		 * MSG_MOVE_ABOVE_LYT_SCROLL; msg.arg1 = secondItemAtLook.getTop() -
		 * aboveLytHeight; handleMsg(msg);
		 * 
		 * } else if (preFirstVisibleItem < firstVisibleItem &&
		 * firstItemInfoAtLook.getType() == 1 && firstItemAtLook.getTop() <= 0
		 * || aboveLyt.getBottom() <= 5) { Log.i("GuLang_ListView", "下标增加");
		 * 
		 * Message msg = Message.obtain(); msg.what =
		 * MSG_MOVE_ABOVE_LYT_RESET_LOCATION; msg.obj =
		 * firstItemInfoAtLook.getTitle(); handleMsg(msg);
		 * 
		 * } else if (firstItemInfoAtLook.getType() != 1 && preFirstVisibleItem
		 * > firstVisibleItem) { Log.i("GuLang_ListView", "下标减小");
		 * MoveLayoutItemInfo tempInfo; if ((tempInfo =
		 * getPreTitleItemInfo(firstVisibleItem)) != null) { Message msg =
		 * Message.obtain(); msg.what = MSG_MOVE_ABOVE_LYT_RESET_LOCATION;
		 * msg.obj = tempInfo.getTitle(); handleMsg(msg); } } }
		 * preFirstVisibleItem = firstVisibleItem; } });
		 */

    }

    public interface MoveLayoutCallBack {
        void moveLayoutCallBack(MoveLayoutItemInfo info);
    }

    private class MoveLayoutListAdapter extends BaseAdapter {
        private final int TYPE_ONE = 0, TYPE_TWO = 1, TYPE_COUNT = 2;
        private LayoutInflater layoutInflater;

        public MoveLayoutListAdapter() {
            layoutInflater = LayoutInflater.from(context);
        }

        /**
         * 该方法返回多少个不同的布局
         */
        @Override
        public int getViewTypeCount() {
            // TODO Auto-generated method stub
            return TYPE_COUNT;
        }

        /**
         * 根据position返回相应的Item
         */
        @Override
        public int getItemViewType(int position) {
            MoveLayoutItemInfo info = listDatas.get(position);
            if (info.getType() == 1)
                return TYPE_ONE;
            else
                return TYPE_TWO;
        }

        @Override
        public int getCount() {
            return listDatas.size();
        }

        @Override
        public Object getItem(int location) {
            return listDatas.get(location);
        }

        @Override
        public long getItemId(int location) {
            return location;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.e(TAG, "getView:");
            final MoveLayoutItemInfo info = listDatas.get(position);
            ListViewParentHolder listViewParentHolder = null;
            ListViewItemHolder listViewItemHolder = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                if (type == TYPE_ONE) {
                    listViewParentHolder = new ListViewParentHolder();
                    convertView = layoutInflater.inflate(
                            R.layout.move_item_lyt, null);
                    listViewParentHolder.moveItemLyt = (RelativeLayout) convertView
                            .findViewById(R.id.move_item_lyt);
                    listViewParentHolder.titleTv = (TextView) convertView
                            .findViewById(R.id.move_lyt_title_tv);
                    listViewParentHolder.imageView = (ImageView) convertView
                            .findViewById(R.id.move_lyt_left_imgview);
                    listViewParentHolder.numTv = (TextView) convertView
                            .findViewById(R.id.move_lyt_num_tv);
                    listViewParentHolder.goImgView = (ImageView) convertView
                            .findViewById(R.id.go_imgview);
                    convertView.setTag(listViewParentHolder);
                } else {
                    listViewItemHolder = new ListViewItemHolder();
                    convertView = layoutInflater.inflate(
                            R.layout.move_item_lyt, null);
                    listViewItemHolder.moveItemLyt = (RelativeLayout) convertView
                            .findViewById(R.id.move_item_lyt);
                    listViewItemHolder.titleTv = (TextView) convertView
                            .findViewById(R.id.move_lyt_title_tv);
                    listViewItemHolder.imageView = (ImageView) convertView
                            .findViewById(R.id.move_lyt_left_imgview);
                    listViewItemHolder.numTv = (TextView) convertView
                            .findViewById(R.id.move_lyt_num_tv);
                    listViewItemHolder.goImgView = (ImageView) convertView
                            .findViewById(R.id.go_imgview);
                    convertView.setTag(listViewItemHolder);
                }
            }

            if (type == TYPE_ONE) {
				/*
				 * 设置数据
				 */
                listViewParentHolder = (ListViewParentHolder) convertView
                        .getTag();
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.WRAP_CONTENT, (int) context
                        .getResources().getDimension(
                                R.dimen.more_apps_item_divider_hight));
                listViewParentHolder.moveItemLyt.setLayoutParams(params);
                listViewParentHolder.moveItemLyt.setBackgroundColor(context
                        .getResources().getColor(R.color.more_apps_bg));

                listViewParentHolder.imageView.setVisibility(View.GONE);
                listViewParentHolder.numTv.setVisibility(View.GONE);
                listViewParentHolder.titleTv.setVisibility(View.GONE);
                listViewParentHolder.goImgView.setVisibility(View.GONE);
            } else {
				/*
				 * 设置数据
				 */
                listViewItemHolder = (ListViewItemHolder) convertView.getTag();
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.WRAP_CONTENT);
                listViewItemHolder.moveItemLyt.setLayoutParams(params);
                listViewItemHolder.moveItemLyt
                        .setBackgroundResource(android.R.color.white);

                Log.e(TAG, "info.getType():" + info.getType()
                        + "  info.getTitle():" + info.getTitle());
                listViewItemHolder.titleTv.setText(info.getTitle());
                listViewItemHolder.titleTv.setVisibility(View.VISIBLE);
                listViewItemHolder.titleTv.setTextColor(Color.BLACK);
                listViewItemHolder.imageView.setVisibility(View.VISIBLE);
                listViewItemHolder.numTv.setVisibility(View.VISIBLE);
                listViewItemHolder.goImgView.setVisibility(View.VISIBLE);
                listViewItemHolder.imageView.setImageDrawable(null);
                if (info.getNum() < 1) {
                    listViewItemHolder.numTv.setVisibility(View.GONE);
                } else {
                    listViewItemHolder.numTv.setVisibility(View.VISIBLE);
                    listViewItemHolder.numTv.setText("" + info.getNum());
                }
                listViewItemHolder.imageView.setImageDrawable(info
                        .getDrawable());
            }
            return convertView;
        }

        private class ListViewParentHolder {
            public RelativeLayout moveItemLyt;
            public ImageView imageView;
            public TextView titleTv;
            public TextView numTv;
            public ImageView goImgView;
        }

        private class ListViewItemHolder {
            public RelativeLayout moveItemLyt;
            public ImageView imageView;
            public TextView titleTv;
            public TextView numTv;
            public ImageView goImgView;
        }

    }

}
