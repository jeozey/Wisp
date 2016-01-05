package com.rj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;


public class SlideTitleTextView extends TextView {

	private final String TAG = "SlideTitleTextView";
	private boolean mSelect = false;
	private Paint paint;
	private float scale;

	private float startX = 0;

	private float realWidth = 0;

	private final String TEXT_COLOR_NORMAL = "#666666";

	private int selectColor = Color.parseColor("#38B8F2"); //默认选中颜色

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.e(TAG, "onDraw:width = " + this.getWidth());
		if (mSelect) {
		}
	}

	public SlideTitleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scale = context.getResources().getDisplayMetrics().density;
		init();
	}

	public SlideTitleTextView(Context context) {
		super(context);
		scale = context.getResources().getDisplayMetrics().density;
		init();
	}

	private void init() {
		selectColor = getResources().getColor(R.color.theme);
		//ContextCompat.getColor(context, R.color.my_color)

		paint = new Paint();// 新建画笔
		paint.setAntiAlias(true);// 平滑处理
		paint.setColor(Color.parseColor(TEXT_COLOR_NORMAL));// 默认文字颜色
		paint.setStrokeWidth(4 * scale);

		setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
		setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		setTextColor(Color.parseColor(TEXT_COLOR_NORMAL));
	}

	public float getViewContentWidth() {
		return this.getPaint().measureText(this.getText().toString());
	}

	public void setSelect(boolean select) {
		this.mSelect = select;
		if (mSelect) {
			setTextColor(selectColor);
		} else {
			setTextColor(Color.parseColor(TEXT_COLOR_NORMAL));
		}
		postInvalidate();
	}

	public float getRealWidth() {
		return realWidth;
	}

	public void setRealWidth(float realWidth) {
		this.realWidth = realWidth;
	}

	public float getStartX() {
		return startX;
	}

	public void setStartX(float startX) {
		this.startX = startX;
	}

	public void setSelectColor(int color) {
		this.selectColor = color;
	}

}
