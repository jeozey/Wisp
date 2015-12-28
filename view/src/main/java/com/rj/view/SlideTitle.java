package com.rj.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class SlideTitle extends HorizontalScrollView {

    private final String TAG = "SlideTitle";
    private Context mContext = null;

    private RelativeLayout contentRL = null;
    private LinearLayout cateItemLayout = null;// 存放子项TextView的线性布局
    private SlideLineTextView lineView = null; // 下划线
    private TitleFlowAdapter tfa;// 适配器
    private MyOnChickListener mOnChickListener;

    private int mCurrentPage = 0;

    private int margin = 0;

    // 接口，点击标题时处理事件
    public interface SlideTitleOnClickListener {
        void slideTitleOnClick(int position);
    }

    private SlideTitleOnClickListener slideTitleOnClickListener;

    public void setSlideTitleOnClickListener(SlideTitleOnClickListener slideTitleOnClickListener) {
        this.slideTitleOnClickListener = slideTitleOnClickListener;
    }

    public SlideTitle(Context context) {
        super(context);
        mContext = context;
        setHorizontalScrollBarEnabled(false);
        setBackgroundColor(Color.WHITE);
        initLayout();
    }

    public SlideTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setBackgroundColor(Color.WHITE);
        setHorizontalScrollBarEnabled(false);
        initLayout();
    }

    private void initLayout() {
        margin = (int) getContext().getResources().getDimension(R.dimen.dp10);
        contentRL = new RelativeLayout(mContext); // 标题页签容器
        LayoutParams contentLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(contentRL, contentLp);
        mOnChickListener = new MyOnChickListener();
        // 线性布局
        cateItemLayout = new LinearLayout(mContext); // 标题页签容器
        // cateItemLayout.setBackgroundColor(Color.YELLOW);
        LayoutParams cateItemLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        contentRL.addView(cateItemLayout, cateItemLp);
        // 下划线
        lineView = new SlideLineTextView(mContext);
        lineView.setColor(getResources().getColor(R.color.theme));
    }

    private float getTotalWidth(List<String> ls) {
        SlideTitleTextView view = new SlideTitleTextView(mContext);
        float totalWidth = 0;
        for (int i = 0; i < ls.size(); i++) {
            view.setText(ls.get(i));
            if (i == ls.size() - 1) {
                totalWidth += view.getViewContentWidth();
            } else {
                totalWidth += view.getViewContentWidth() + margin;
            }
            Log.e(TAG, "totalWidth = " + totalWidth);
        }
        view = null;
        return totalWidth;
    }

    public void setMidChildTitleFlow(List<String> ls) {
        if (ls == null || ls.equals("")) {
            return;
        }
        mCurrentPage = 0;
        tfa = new TitleFlowAdapter(mContext, ls);
        cateItemLayout.removeAllViews();
        contentRL.removeView(lineView);
        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int itemWidth = 0;
        int count = tfa.getCount();

        float totalWidth = getTotalWidth(ls);
        if (totalWidth < screenWidth - margin * 2) {
            totalWidth = 0;
            itemWidth = (screenWidth - margin) / count - margin;
            for (int i = 0; i < count; i++) {
                SlideTitleTextView view = (SlideTitleTextView) tfa.getView(i, null, this);
                // view.setBackgroundColor(Color.RED);
                view.setOnClickListener(mOnChickListener);// 点击事件
                LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(itemWidth, LayoutParams.MATCH_PARENT);
                view.setRealWidth(itemWidth);
                view.setStartX(totalWidth);
                if (i != count - 1) {
                    viewLp.setMargins(0, 0, margin, 0);
                    totalWidth += itemWidth + margin;
                } else {
                    totalWidth += itemWidth;
                }
                cateItemLayout.addView(view, viewLp);
                Log.e(TAG, "-----:------0:" + view);
            }
        } else {
            totalWidth = 0;
            for (int i = 0; i < count; i++) {
                SlideTitleTextView view = (SlideTitleTextView) tfa.getView(i, null, this);
                // view.setBackgroundColor(Color.RED);
                LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                view.setRealWidth(view.getViewContentWidth());
                view.setStartX(totalWidth);
                if (i != count - 1) {
                    viewLp.setMargins(0, 0, margin, 0);
                    totalWidth += view.getViewContentWidth() + margin;
                } else {
                    totalWidth += view.getViewContentWidth();
                }
                view.setLayoutParams(viewLp);
                view.setOnClickListener(mOnChickListener);// 点击事件
                cateItemLayout.addView(view);

            }
        }
        // 下划线
        Log.e(TAG, "-totalWidth- = " + totalWidth);
        // lineView.setBackgroundColor(Color.GREEN);
        RelativeLayout.LayoutParams lineViewLp = new RelativeLayout.LayoutParams((int) totalWidth,
                (int) getContext().getResources().getDimension(R.dimen.dp3));
        lineViewLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        contentRL.addView(lineView, lineViewLp);
        // 默认下划线位置
        setLineViewFrame();
    }

    private void resetWidth(List<String> ls, int parentWidth, int parentOldWidth) {
        if (parentOldWidth == 0) {
            parentOldWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        }

        final float scale = (float) parentWidth / (float) parentOldWidth;
        Log.v(TAG, "scale = " + scale);
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                int count = tfa.getCount();
                float scrollWidth = cateItemLayout.getMeasuredWidth();
                float newScrollWidth = scale * scrollWidth;
                cateItemLayout.getLayoutParams().width = (int) newScrollWidth;
                margin = (int) (margin * scale + 0.5f);
                for (int i = 0; i < count; i++) {
                    SlideTitleTextView view = (SlideTitleTextView) tfa.lv.get(i);
                    view.setRealWidth(view.getRealWidth() * scale);
                    view.setStartX(view.getStartX() * scale);
                    MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
                    if (i != count - 1) {
                        lp.setMargins(0, 0, margin, 0);
                    }
                    lp.width = (int) (view.getRealWidth() + 0.5f);
                }
                contentRL.removeView(lineView);
                RelativeLayout.LayoutParams lineViewLp = new RelativeLayout.LayoutParams((int) newScrollWidth,
                        (int) getContext().getResources().getDimension(R.dimen.dp3));
                lineViewLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                contentRL.addView(lineView, lineViewLp);
                setTitleFlowScroll(mCurrentPage);
                setLineViewFrame();
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void setLineViewFrame() {
        SlideTitleTextView curView = (SlideTitleTextView) getTag();
        lineView.setFrame((int) curView.getStartX(), (int) curView.getRealWidth());
    }

    /**
     * 平滑滑动横线
     */
    private void animateScrollLineView(final int curPosition, final int newPosition) {
        if (isPositionOutOfBound(curPosition) || isPositionOutOfBound(newPosition)) {
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator(); // 先加速后减速的动画插补器
                SlideTitleTextView curView = (SlideTitleTextView) tfa.lv.get(curPosition),
                        newView = (SlideTitleTextView) tfa.lv.get(newPosition);
                float startX = curView.getStartX(); // 起始位置
                float deltaX = newView.getStartX() - startX; // 到终止位置的位移
                float deltaWidth = newView.getRealWidth() - curView.getRealWidth(); // 横线从起始到终止位置的宽度变化
                for (int i = 1; i <= 12; i++) { // 动画时间200ms，按照60fps来算，200ms需要绘制12帧
                    float percent = interpolator.getInterpolation(i / 12.0f);
                    float offset = percent * deltaX;// 横线x方向位移
                    float offsetWidth = percent * deltaWidth; // 横线宽度变化量
                    lineView.setFrame((int) (startX + offset + 0.5f),
                            (int) (curView.getRealWidth() + offsetWidth + 0.5f)); // 绘制横线
                    try {
                        Thread.sleep(16); // 绘制间隔16ms
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }).start();
    }

    /**
     * 判断position是否越界
     */
    private boolean isPositionOutOfBound(int position) {
        boolean result = false;
        if (tfa == null || position < 0 || position >= tfa.getCount()) {
            result = true;
        }
        return result;
    }

    public void setCurLineViewFrame(int arg0, float arg1, int arg2) {
        if (arg1 == 0) {
            return;
        }
        for (int i = 0; i < cateItemLayout.getChildCount(); i++) {
            // 真奇葩，好好的SlideTitleTextView变成了framelayout然后奔溃.
            Log.e(TAG, "-----:------1:" + cateItemLayout.getChildAt(i));
        }
        // 获取当前标题的宽度
        // SlideTitleTextView curView = (SlideTitleTextView)
        // cateItemLayout.getChildAt(mCurrentPage);
        SlideTitleTextView curView = (SlideTitleTextView) tfa.lv.get(mCurrentPage);// 只好改到从适配器取
        int curTitleWidth = (int) curView.getRealWidth();
        if (arg0 == mCurrentPage - 1) {// 左移
            // 获取左边标题的宽度
            // SlideTitleTextView view = (SlideTitleTextView)
            // cateItemLayout.getChildAt(arg0);
            SlideTitleTextView view = (SlideTitleTextView) tfa.lv.get(arg0);
            int newTitleWidth = (int) view.getRealWidth();
            float difference = curTitleWidth - newTitleWidth;
            float lineOffset = (curTitleWidth + margin) * (arg1 - 1);
            int startX = (int) (curView.getStartX() + lineOffset);
            float differenceOffset = difference * (arg1 - 1);
            int width = (int) (curTitleWidth + differenceOffset);
            startX -= differenceOffset;
            lineView.setFrame(startX, width);
            // 开始移动
        } else if (arg0 == mCurrentPage) {// 右移
            if (arg0 + 1 >= tfa.getCount()) {
                return;
            }
            // 获取右边标题的宽度
            // SlideTitleTextView view = (SlideTitleTextView)
            // cateItemLayout.getChildAt(arg0+1);
            SlideTitleTextView view = (SlideTitleTextView) tfa.lv.get(arg0 + 1);
            int newTitleWidth = (int) view.getRealWidth();
            float difference = newTitleWidth - curTitleWidth;
            float lineOffset = (curTitleWidth + margin) * arg1;
            int startX = (int) (curView.getStartX() + lineOffset);
            float differenceOffset = difference * arg1;
            int width = (int) (curTitleWidth + differenceOffset);
            lineView.setFrame(startX, width);
        }
    }

    // public void addTitle(String title) {
    // tfa.mls.add(title);
    // View view = tfa.getView(tfa.getCount() - 1, null, this);
    // view.setOnClickListener(mOnChickListener);// 点击事件
    // cateItemLayout.addView(view, new LayoutParams(
    // LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    // }
    //
    // public void deleteTitle(String title) {
    // for (int i = 0; i < tfa.mls.size(); i++) {
    // if (tfa.mls.get(i).equals(title)) {
    // View view = tfa.getView(i, tfa.lv.get(i), this);
    // view.setOnClickListener(null);// 点击事件
    // cateItemLayout.removeView(view);
    // tfa.lv.remove(i);
    // tfa.mls.remove(title);
    // mCurrentPage = mCurrentPage > tfa.mls.size() - 1 ? tfa.mls
    // .size() - 1 : mCurrentPage;
    // setChangeTitle(mCurrentPage);
    // break;
    // }
    // }
    // }

    public View getTitleView(int position) {
        View v = null;
        if (tfa != null && position < tfa.getCount()) {
            v = tfa.lv.get(position);
        }
        return v;
    }

    class TitleFlowAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public List<String> mls;
        public List<View> lv = new ArrayList<View>();

        public TitleFlowAdapter(Context context, List<String> ls) {
            mls = ls;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.slide_title_textview, parent, false);
                ((SlideTitleTextView) convertView).setText(mls.get(position));
                ((SlideTitleTextView) convertView).setSelectColor(getResources().getColor(R.color.theme));
                lv.add(convertView);
            } else {
                convertView = lv.get(position);
            }
            if (position == 0) { // 只有一个标签，直接选中
                setTag(convertView);
                ((SlideTitleTextView) convertView).setSelect(true);
            }
            return convertView;
        }
    }

    class MyOnChickListener implements OnClickListener {// 标题点击事件

        @Override
        public void onClick(View v) {
            if (v == getTag()) {
                return;
            }
            int position = 0;
            for (int i = 0; i < tfa.lv.size(); i++) {
                if (tfa.lv.get(i).equals(v)) {
                    position = i;
                    break;
                }
            }
            setChangeTitle(position);
            // 抛出接口
            if (slideTitleOnClickListener != null) {
                slideTitleOnClickListener.slideTitleOnClick(position);
            }
        }
    }

    /**
     * 改变选中的标题
     */
    public void setChangeTitle(int position) {
        animateScrollLineView(mCurrentPage, position);
        selectItemText(position);
        // 设置下划线
        // setLineViewFrame();
    }

    /**
     * 选中指定标题，只高亮指定标题文字，不移动游标
     */
    public void selectItemText(int position) {
        // 改变颜色
        if (getTag() != null) {
            ((SlideTitleTextView) getTag()).setSelect(false);
        }
        ((SlideTitleTextView) tfa.lv.get(position)).setSelect(true);
        setTag(tfa.lv.get(position));
        // 滑动标题
        setTitleFlowScroll(position);
        // 设置页码
        mCurrentPage = position;
    }

    private void setTitleFlowScroll(int position) {
        View v = tfa.lv.get(position);
        if (v != null) {
            int[] arr = new int[2];
            // 获取在整个屏幕内的绝对坐标，注意这个值是要从屏幕顶端算起
            v.getLocationOnScreen(arr);
            final int x = arr[0];
            if ((x + v.getWidth() / 2 < getMeasuredWidth() / 2 - 2)
                    || (x + v.getWidth() / 2 > getMeasuredWidth() / 2 + 2)) {
                int offset = 0;
                for (int i = position - 1; i >= 0; i--) {
                    offset += tfa.lv.get(i).getWidth();
                }
                offset += (v.getWidth() / 2 - getMeasuredWidth() / 2);
                smoothScrollTo(offset, 0);
            } else {
                return;
            }
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        Log.v(TAG, "onSizeChanged : w = " + w + "  oldw = " + oldw);
        if (w == oldw) {
            return;
        }
        resetWidth(tfa.mls, w, oldw);
    }

}
