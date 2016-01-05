package com.rj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class SlideLineTextView extends TextView {

	private final String TAG = "SlideLineTextView";
	private Paint paint;
	private float scale;
	private final String TEXT_COLOR = "#6C6B6B"; //默认颜色
	private int startX = 0;
	private int width = 0;

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.e(TAG, "onDraw:width = " + this.getWidth());
		canvas.drawLine(startX, this.getHeight(), startX + width,
				this.getHeight(), paint);
	}

	public SlideLineTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scale = context.getResources().getDisplayMetrics().density;
		init();
	}

	public SlideLineTextView(Context context) {
		super(context);
		scale = context.getResources().getDisplayMetrics().density;
		init();
	}

	private void init() {
		paint = new Paint();// 新建画笔
		paint.setAntiAlias(true);// 平滑处理
		paint.setColor(Color.parseColor(TEXT_COLOR));// 默认文字颜色
		paint.setStrokeWidth(4 * scale);
	}

	public void setFrame(int startX, int width) {
		this.startX = startX;
		this.width = width;
		Log.e(TAG, "startX = " + startX + ",width = " + width);
		postInvalidate();
	}

	public void setColor(int color) {
		paint.setColor(color);
	}

	public void setColor(String rgb) {
		paint.setColor(Color.parseColor(rgb));
	}

}
