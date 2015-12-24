package com.rj.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


public class FloatView extends View {
    private static final String TAG = "FloatView";
    private int statusBarHeight;// 状态栏高度
    private View view;// 透明窗体
    private boolean viewAdded = false;// 透明窗体是否已经显示
    //	private HomeReceiver mHomeReceiver;
    WindowManager mWManger;
    WindowManager.LayoutParams mWManParams;

    // 初始位置
    private float startX;
    private float startY;

    // 坐标
    private float x;
    private float y;

    //
    private float mTouchSatrtX;
    private float mTouchStartY;

    // 组件
    public ImageView img_folat;
    // img_close;
    public TextView tv_showL, tv_showR;
    public ImageView img_showL, img_showR, img_showCenter;

    Context mContext;

    private Intent service;

    private void initHomeReceiver() {
//		mHomeReceiver = new HomeReceiver();
//		service = new Intent(getContext(), RunningStateService.class);
//		getContext().startService(service);
//
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(RunningStateService.ACTION_STATE_HOME);
//		filter.addAction(RunningStateService.ACTION_STATE_INNER);
//		filter.addAction(RunningStateService.ACTION_STATE_OTHER);
//		getContext().getApplicationContext().registerReceiver(mHomeReceiver,
//				filter);
    }

    public FloatView(Context context) {
        super(context);
        this.mContext = context;
        initHomeReceiver();
    }

    public FloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initHomeReceiver();
    }

    public FloatView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    int state = 0;
    float StartX, StartY;

    /**
     * 初始化mWManger,mWManParams
     */
    public void show(int x, int y, int width, int height, int color, int imgCenter) {
        mWManger = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

        // 加载悬浮窗布局文件
        view = LayoutInflater.from(mContext).inflate(R.layout.float_layout,
                null);

        // 设置LayoutParams(全局变量）相关参数
        mWManParams = new WindowManager.LayoutParams();
        mWManParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mWManParams.flags |= 8;
        mWManParams.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
        // 以屏幕左上角为原点，设置x、y初始值
        mWManParams.x = x;
        mWManParams.y = y;
        // 设置悬浮窗口长宽数据
        mWManParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWManParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWManParams.format = 1;

        // mWManger.addView(view, mWManParams);

		/*view.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
				// 获取相对屏幕的坐标，即以屏幕左上角为原点
				x = event.getRawX();
				y = event.getRawY() - 25; // 25是系统状态栏的高度
//				Log.i("currP", "currX" + x + "====currY" + y);// 调试信息
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					state = MotionEvent.ACTION_DOWN;
					StartX = x;
					StartY = y;
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchSatrtX = event.getX();
					mTouchStartY = event.getY();
					Log.i("startP", "startX" + mTouchSatrtX + "====startY"
							+ mTouchStartY);// 调试信息
					break;
				case MotionEvent.ACTION_MOVE:
					state = MotionEvent.ACTION_MOVE;
					updatePosition();
					break;

				case MotionEvent.ACTION_UP:
					state = MotionEvent.ACTION_UP;

					updatePosition();
					// showImg();
					mTouchSatrtX = mTouchStartY = 0;
					break;
				}
				return true;
			}
		});*/

        tv_showL = (TextView) view.findViewById(R.id.tv_showL);
        tv_showR = (TextView) view.findViewById(R.id.tv_showR);
        tv_showL.setTextColor(color);
        tv_showR.setTextColor(color);
        img_showL = (ImageView) view.findViewById(R.id.img_showLeft);
        img_showR = (ImageView) view.findViewById(R.id.img_showRight);
        img_showCenter = (ImageView) view.findViewById(R.id.img_showCenter);
        img_showCenter.setImageResource(imgCenter);
        refresh();
    }

    /**
     * 更新悬浮窗的位置
     */
    public void updatePosition() {
        mWManParams.x = (int) (x - mTouchSatrtX);
        mWManParams.y = (int) (y - mTouchStartY);

        mWManger.updateViewLayout(view, mWManParams);
    }

    /**
     * 刷新悬浮窗
     *
     * @param x 拖动后的X轴坐标
     * @param y 拖动后的Y轴坐标
     */
    public void refreshView(int x, int y) {
        // 状态栏高度不能立即取，不然得到的值是0
        if (statusBarHeight == 0) {
            View rootView = view.getRootView();
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            statusBarHeight = r.top;
        }

        mWManParams.x = x;
        // y轴减去状态栏的高度，因为状态栏不是用户可以绘制的区域，不然拖动的时候会有跳动
        mWManParams.y = y - statusBarHeight;// STATUS_HEIGHT;
        refresh();
    }

    /**
     * 添加悬浮窗或者更新悬浮窗 如果悬浮窗还没添加则添加 如果已经添加则更新其位置
     */
    private void refresh() {
        if (viewAdded) {
            mWManger.updateViewLayout(view, mWManParams);
        } else {
            mWManger.addView(view, mWManParams);
            viewAdded = true;
        }
    }

    /**
     * 关闭悬浮窗
     */
    public void removeView() {
        if (viewAdded) {
            mWManger.removeView(view);
            viewAdded = false;

            getContext().stopService(service);

//			getContext().getApplicationContext().unregisterReceiver(
//					mHomeReceiver);
        }
    }

    private void hide() {
        try {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show() {
        try {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//	class HomeReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			Log.d(TAG, "HomeReceiver onReceive()  action = " + action);
//			if (RunningStateService.ACTION_STATE_HOME.equals(action)) {
//				hide();
//			} else if (RunningStateService.ACTION_STATE_INNER.equals(action)) {
//				show();
//			} else if (RunningStateService.ACTION_STATE_OTHER.equals(action)) {
//				hide();
//			}
//		}
//
//	}
}