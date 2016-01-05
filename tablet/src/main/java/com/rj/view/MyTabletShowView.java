package com.rj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.rj.widget.utils.Compress;

public class MyTabletShowView extends View {
    private int mScreenWidth, mScreenHeight;
    private Paint mPaint;
    private Paint mYPaint;
    private Context mCtx;

    public MyTabletShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
    }

    public MyTabletShowView(Context context) {
        super(context);
        mCtx = context;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(getWidth(), getHeight());
    }

    public void init(int width, int Height) {
        mScreenWidth = width;
        mScreenHeight = Height;
        mPaint = new Paint();
        mYPaint = getPathEffectPaint();
    }

    public static int whiteBg = Color.parseColor("#FFFFFF"); // 背景颜色

    private int linesColor = 0xffc0d0e0;// 横线的颜色

//	private int lineHeight;
//
//	private int lineWidth;

    private int lineCount = 8;

    private int linesWidth = 2;// 横线的宽度

    private int paddingLine = 5;

    private int mCheck = 80;

    /**
     * 用于draw背景和lines
     */
    private void addBgLine(Canvas canvas) {
        int baseXLine = 0;
        int baseYline = 0;
        int check = Compress.dp2Px(mCheck, mCtx);
//		lineHeight = mScreenHeight / lineCount;
//		lineWidth = mScreenWidth / lineCount;
        mPaint.setColor(whiteBg);// 背景颜色
        canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mPaint);

        mPaint.setColor(linesColor);// 横线
        mPaint.setStrokeWidth(linesWidth);
        while (baseXLine < mScreenHeight) {
            canvas.drawLine(paddingLine, baseXLine, mScreenWidth - paddingLine, baseXLine, mPaint);
            baseXLine += check;
        }
        baseXLine = check;

        while (baseYline < mScreenWidth) {
            canvas.drawLine(baseYline, 0, baseYline, mScreenHeight - paddingLine, mYPaint);
            baseYline += check;
        }

        baseYline = check;

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        addBgLine(canvas);
    }

    private Paint getPathEffectPaint() {
        Paint p = new Paint();
        p.setStyle(Style.STROKE);
        p.setColor(linesColor);
        p.setAntiAlias(true);
        p.setStrokeWidth(linesWidth);
        PathEffect effects = new DashPathEffect(new float[]{20, 20, 20, 20}, 1);
        p.setPathEffect(effects);
        return p;
    }

}
