package com.rj.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.rj.widget.utils.Compress;
import com.rj.widget.utils.RecycleBitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("DrawAllocation")
@SuppressWarnings("unchecked")
public class MyTabletView extends View {
    private static final String TAG = "MyTabletView";
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private float mTemp_X, mTemp_Y;
    private final float TOUCH_TOLERANCE = 4;
    public final int mBgColor = Color.parseColor("#00000000"); // 背景颜色
    public final int mPaintColor = Color.parseColor("#000000"); // 画笔#6699CC
    public final int mSrokeWidth = 3;

    private List<DrawPath> mSavePath; // 当前存放笔画集合
    private List<DrawPath> mLastSavePath = new ArrayList<DrawPath>();// 上次存放笔画集合
    private DrawPath mDp;
    private int mScreenWidth, mScreenHeight;
    private ObserverThread mObserverThread;
    private ArrayList<ArrayList<PointF>> mPointFsList = new ArrayList<ArrayList<PointF>>();// 当前笔迹存放列表
    private SparseArray<ArrayList<ArrayList<PointF>>> mAllPagePoints = new SparseArray<ArrayList<ArrayList<PointF>>>();

    public ArrayList<ArrayList<PointF>> getPointFsList() {
        return mPointFsList;
    }

    public void setPointFsList(ArrayList<ArrayList<PointF>> pointFsList) {
        this.mPointFsList = pointFsList;
    }

    private LinkedList<ArrayList<ArrayList<PointF>>> delStack = new LinkedList<ArrayList<ArrayList<PointF>>>();

    private SparseArray<LinkedList<ArrayList<ArrayList<PointF>>>> mAllPagesDelStack = new SparseArray<LinkedList<ArrayList<ArrayList<PointF>>>>();

    private ArrayList<PointF> mOneLineList;
    private Context mCtx;
    private int size = 1;
    private RecycleBitmap mRecycleBitmap = new RecycleBitmap(null);

    private class DrawPath {
        public Path path;
        public Paint paint;
        public ArrayList<PointF> list;
    }

    private Paint mPaint;

    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    private boolean isEraser = false;

    public boolean isEraser() {
        return isEraser;
    }

    public void setEraser(boolean isEraser) {
        this.isEraser = isEraser;
    }

    private boolean hasTouch = false;

    public MyTabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
    }

    public MyTabletView(Context context) {
        super(context);
        mCtx = context;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        init(getWidth(), getHeight());
        Log.i(TAG, "onlayout");
    }

    public void init(int width, int Height) {
        mScreenWidth = width;
        mScreenHeight = Height;
        mBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
        Bitmap old = mRecycleBitmap.getBitmap();
        mRecycleBitmap.setBitmap(mBitmap);

        if (old != null && old != mBitmap) {
            if (!old.isRecycled()) {
                Log.i("wanan2", "init recycle");
                old.recycle();
                old = null;
            }
        }
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBgColor);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mSrokeWidth);
        mPaint.setColor(mPaintColor);
        mSavePath = new ArrayList<DrawPath>();
        if (mPointFsList != null)
            Log.i(TAG, "mPointFsList.size=" + mPointFsList.size());
        returnCanvasByPoints(mPointFsList);
    }

    public void lastPath(String lastPath) {
        if (TextUtils.isEmpty(lastPath)) {
            return;
        }
        setSize();
        getPointFsByBytes(lastPath);
        clearCanvas();
        for (int i = 0; i < mAllPagesDelStack.size(); i++) {
            if (mAllPagesDelStack.get(i) != null)
                mAllPagesDelStack.get(i).clear();
        }

        if (mShowPageListener != null) {
            mShowPageListener.showPageCount(String.valueOf(mCurrentPage + 1) + "/" + mAllPagePoints.size());
        }

        // delStack.clear();
    }

    public void setSize() {
        size = 1;
    }

    public void startOrStopThread() {
        if (mObserverThread == null) {
            mObserverThread = new ObserverThread();
            mObserverThread.start();
        } else {
            if (mObserverThread.isAlive())
                mObserverThread.interrupt();
            mObserverThread = null;
        }
    }

    private Path mAllPath;

    @Override
    public void onDraw(Canvas canvas) {
        if (!isCanasEraser) {
            if (mBitmap != null)
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            if (mPath != null) {
                canvas.drawPath(mPath, mPaint);// 实时的显示
            }
        } else {
            if (mAllPath == null)
                mAllPath = new Path();
            else
                mAllPath.reset();
            PointF p;
            mPointFsList = mAllPagePoints.get(mCurrentPage);
            if (mPointFsList == null) {
                return;
            }
            Iterator<ArrayList<PointF>> it = mPointFsList.iterator();
            while (it.hasNext()) {
                ArrayList<PointF> arc = it.next();
                if (arc.size() >= 2) {
                    Iterator<PointF> iit = arc.iterator();
                    p = iit.next();
                    float mX = p.x;
                    float mY = p.y;
                    mAllPath.moveTo(mX, mY);
                    while (iit.hasNext()) {
                        p = iit.next();
                        float x = p.x;
                        float y = p.y;
                        mAllPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                        mX = x;
                        mY = y;
                    }
                    mAllPath.lineTo(mX, mY);
                }
            }

            canvas.drawPath(mAllPath, mPaint);
            mCanvas.drawPath(mAllPath, mPaint);
            isCanasEraser = false;
        }

    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mTemp_X = x;
        mTemp_Y = y;
        PointF point = new PointF(x, y);
        mOneLineList = new ArrayList<PointF>();
        mOneLineList.add(point);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mTemp_X);
        float dy = Math.abs(mTemp_Y - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mTemp_X, mTemp_Y, (x + mTemp_X) / 2, (y + mTemp_Y) / 2); // 画一条贝塞尔曲线
            mTemp_X = x;
            mTemp_Y = y;
        }
        PointF point = new PointF(x, y);
        mOneLineList.add(point);
    }

    private void touch_up() {
        hasTouch = true;
        mPath.lineTo(mTemp_X, mTemp_Y);
        mCanvas.drawPath(mPath, mPaint);
        mDp.list = mOneLineList;
        mSavePath.add(mDp);
        mPath = null;
        mPointFsList.add(mOneLineList);
    }

    /**
     * 擦除痕迹
     */
    private void erase(float x, float y) {
        final int radius = 20;
        final RectF rect = new RectF(x - radius, y - radius, x + radius, y + radius);
        boolean isBreak = false;
        DrawPath mDp = null;
        for (int i = 0; i < mPointFsList.size(); i++) {
            for (PointF pointf : mPointFsList.get(i)) {
                if (rect.contains(pointf.x * 1.0f, pointf.y * 1.0f)) {
                    ArrayList<ArrayList<PointF>> delPoints = new ArrayList<ArrayList<PointF>>();
                    delPoints.add(mPointFsList.remove(i));
                    delStack.push(delPoints);

                    isBreak = true;
                    break;
                }
            }
            if (isBreak) {
                clearCanvas();
                break;
            }
        }

    }

    private boolean isCanasEraser = false;

    private void eraseSelect(DrawPath dp) {
        clearCanvas();
        mSavePath.remove(dp);
        if (mSavePath != null && mSavePath.size() > 0) {
            Iterator<DrawPath> iter = mSavePath.iterator();
            while (iter.hasNext()) {
                DrawPath drawPath = iter.next();
                mCanvas.drawPath(drawPath.path, drawPath.paint);
            }
        }
        invalidate();
    }

    private void goBack() {
        clearCanvas();
        if (mSavePath != null && mSavePath.size() > 0) {
            mSavePath.remove(mSavePath.size() - 1);
            Log.i(TAG, "mSavePath.size=" + mSavePath.size());
            Log.i(TAG, "mLastSavePath.size=" + mLastSavePath.size());
            Iterator<DrawPath> iter = mSavePath.iterator();
            while (iter.hasNext()) {
                DrawPath drawPath = iter.next();
                mCanvas.drawPath(drawPath.path, drawPath.paint);
            }
            invalidate();
        }
    }

    private void goBack2() {
        clearCanvas();
        if (mSavePath != null && mSavePath.size() > 0) {
            // addtoLast(mSavePath);
            mSavePath.remove(mSavePath.size() - 1);
            Log.i(TAG, "mSavePath.size=" + mSavePath.size());
            Log.i(TAG, "mLastSavePath.size=" + mLastSavePath.size());
            PointF p;
            Iterator<DrawPath> it = mSavePath.iterator();
            while (it.hasNext()) {
                DrawPath drawPath = it.next();
                ArrayList<PointF> arc = drawPath.list;
                Paint paint = drawPath.paint;
                Path path = drawPath.path;
//				DrawPath arc = it.next();
                if (arc.size() >= 2) {
                    Iterator<PointF> iit = arc.iterator();
                    p = iit.next();
                    float mX = p.x;
                    float mY = p.y;
                    path.moveTo(mX, mY);
                    while (iit.hasNext()) {
                        p = iit.next();
                        float x = p.x;
                        float y = p.y;
                        path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                        mX = x;
                        mY = y;
                    }
                    path.lineTo(mX, mY);
                }
            }
            mCanvas.drawPath(mPath, mPaint);
            invalidate();
        }
    }

//	private Path mAllPath=new Path();

    private void goNext() {
        clearCanvas();
        if (mLastSavePath != null && mLastSavePath.size() > 0) {
            Iterator<DrawPath> iter = mLastSavePath.iterator();
            while (iter.hasNext()) {
                DrawPath drawPath = iter.next();
                mCanvas.drawPath(drawPath.path, drawPath.paint);
            }
            invalidate();
        }
    }

    public Bitmap processBitmap() {
        Bitmap bmp = makeTempBitmap();
        return bmp;
    }

    @SuppressWarnings("deprecation")
    private Bitmap makeTempBitmap() {
        if (isPointEmpty()) {
            return null;
        }

        ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();

        Display dp = ((Activity) mCtx).getWindowManager().getDefaultDisplay();
        int mainW = dp.getWidth();
        int mainH = dp.getHeight();
        boolean isInit = true;
        Matrix max = new Matrix();
        max.postScale(scale, scale);
        float minX = 0;
        float minY = 0;
        float maxX = 0;
        float maxY = 0;
        for (int j = 0; j < mAllPagePoints.size(); j++) {
            if (mAllPagePoints.get(j).size() == 0) {
                continue;
            }
            Bitmap bmp = null;

            ArrayList<ArrayList<PointF>> list = mAllPagePoints.get(j);
            if (isInit) {
                // isInit = false;
                minX = list.get(0).get(0).x;
                minY = list.get(0).get(0).y;
                maxX = list.get(0).get(0).x;
                maxY = list.get(0).get(0).y;
            }
            for (ArrayList<PointF> paths : list) {
                for (int i = 0; i < paths.size(); i++) {
                    float x = paths.get(i).x;
                    float y = paths.get(i).y;
                    if (x < minX) {
                        minX = x;
                    } else if (x > maxX) {
                        maxX = x;
                    }

                    if (y < minY) {
                        minY = y;
                    } else if (y > maxY) {
                        maxY = y;
                    }
                }
            }

            int x1 = Math.round(minX) - 5;
            if (x1 < 0) {
                x1 = 0;
            }
            int x2 = Math.round(maxX) + 5;
            if (x2 > mainW) {
                x2 = mainW;
            }
            int y1 = Math.round(minY) - 5;
            if (y1 < 0) {
                y1 = 0;
            }

            int y2 = Math.round(maxY + 5);
            if (y2 > mainH) {
                y2 = mainH;
            }
            int bmpW = x2 - x1;
            int bmpH = y2 - y1;

            if (x1 + bmpW > mBitmap.getWidth()) {
                bmpW = mBitmap.getWidth() - x1;
            }
            if (y1 + bmpH > mBitmap.getHeight()) {
                bmpH = mBitmap.getHeight() - y1;
            }
            getPageBitmap(j);
            bmp = Bitmap.createBitmap(mBitmap, x1, y1, bmpW, bmpH, max, true);
            bitmapList.add(bmp);
        }
        return addAllBitmap(bitmapList);
    }

    //jeo 缩放大小，原本是1f,但是传到后台时候显示太多，故设置0.3
    private final float scale = 0.3f;

    private Bitmap addAllBitmap(ArrayList<Bitmap> bitmaps) {
        if (bitmaps.size() == 0) {
            return null;
        }
        int maxWidth = bitmaps.get(0).getWidth();
        int height = 0;
        for (Bitmap bmp : bitmaps) {
            if (bmp.getWidth() > maxWidth) {
                maxWidth = bmp.getWidth();
            }
            height = height + bmp.getHeight();

        }
        Bitmap result = Bitmap.createBitmap(maxWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int h = 0;
        for (Bitmap bmp : bitmaps) {
            canvas.drawBitmap(bmp, 0, h, null);
            h = bmp.getHeight() + h;
        }
        for (Bitmap bmp : bitmaps) {
            if (bmp != null && !bmp.isRecycled()) {
                Log.i("wanan2", "bitmaplist recycle");
                bmp.recycle();
                bmp = null;
            }
        }
        return result;
    }

    private Bitmap getPageBitmap(int j) {
        Canvas canvas = new Canvas();
        Iterator<ArrayList<PointF>> it = mAllPagePoints.get(j).iterator();
        mBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
        Bitmap old = mRecycleBitmap.getBitmap();
        mRecycleBitmap.setBitmap(mBitmap);

        if (old != null && old != mBitmap) {
            if (!old.isRecycled()) {
                Log.i("wanan2", "getPageBitmap recycle");
                old.recycle();
                old = null;
            }
        }
        canvas.setBitmap(mBitmap);
        canvas.drawColor(mBgColor);

        PointF p;
        if (mAllPath == null)
            mAllPath = new Path();
        else
            mAllPath.reset();
        while (it.hasNext()) {
            ArrayList<PointF> arc = it.next();
            if (arc.size() >= 2) {
                Iterator<PointF> iit = arc.iterator();
                p = iit.next();
                float mX = p.x;
                float mY = p.y;
                mAllPath.moveTo(mX, mY);
                while (iit.hasNext()) {
                    p = iit.next();
                    float x = p.x;
                    float y = p.y;
                    mAllPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                mAllPath.lineTo(mX, mY);
            }
        }

        canvas.drawPath(mAllPath, mPaint);

        // mCanvas.setBitmap(mBitmap);
        return mBitmap;

    }

    /**
     * 手写图片进行缩放
     */
    public Bitmap getBitmapBy(ArrayList<ArrayList<PointF>> points) {
        if (points.size() == 0) {
            return null;
        }
        // returnCanvasByPoints(points);
        Bitmap tempBitmap = Bitmap.createScaledBitmap(mBitmap, mScreenWidth / 3, mScreenHeight / 3, true);
        return tempBitmap;
    }

    /**
     * 根据当前保存的手写痕迹生成图片
     */
    public void returnCanvasByPoints(ArrayList<ArrayList<PointF>> points) {
        // 测试方便
        if (mAllPagePoints.get(mCurrentPage) == null) {
            ArrayList<ArrayList<PointF>> list = new ArrayList<ArrayList<PointF>>();
            mAllPagePoints.put(mCurrentPage, list);
        }

        if (mAllPagesDelStack.get(mCurrentPage) == null) {
            LinkedList<ArrayList<ArrayList<PointF>>> list = new LinkedList<ArrayList<ArrayList<PointF>>>();
            mAllPagesDelStack.put(mCurrentPage, list);
        }

        mPointFsList = mAllPagePoints.get(mCurrentPage);
        delStack = mAllPagesDelStack.get(mCurrentPage);

        if (mShowPageListener != null) {
            mShowPageListener.showPageCount(String.valueOf(mCurrentPage + 1) + "/" + mAllPagePoints.size());
        }

        if (points == null || points.size() == 0) {
            return;
        }

        clearCanvas();
    }

    private boolean isPointEmpty() {
        boolean isEmpty = true;
        for (int i = 0; i < mAllPagePoints.size(); i++) {
            if (mAllPagePoints.get(i).size() > 0) {
                isEmpty = false;
            } else {
                continue;
            }
        }
        return isEmpty;
    }

    private int checkSize() {
        if (isPointEmpty()) {
            return size;
        }
        double tempsize = 1.0;
        float minX = mPointFsList.get(0).get(0).x;
        float minY = mPointFsList.get(0).get(0).y;
        float maxX = mPointFsList.get(0).get(0).x;
        float maxY = mPointFsList.get(0).get(0).y;
        Display dp = ((Activity) mCtx).getWindowManager().getDefaultDisplay();
        int phoneW = dp.getWidth();
        int phoneH = dp.getHeight();
        for (int j = 0; j < mAllPagePoints.size(); j++) {
            ArrayList<ArrayList<PointF>> list = mAllPagePoints.get(j);
            for (ArrayList<PointF> paths : list) {
                for (int i = 0; i < paths.size(); i++) {
                    float x = paths.get(i).x;
                    float y = paths.get(i).y;
                    if (x < minX) {
                        minX = x;
                    } else if (x > maxX) {
                        maxX = x;
                    }

                    if (y < minY) {
                        minY = y;
                    } else if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        float rateX = 1;
        float rateY = 1;
        if (maxX > phoneW) {
            rateX = maxX / phoneW;
        }
        if (maxY > phoneH) {
            rateY = maxY / phoneH;
        }
        float rate = Math.max(rateX, rateY);
        tempsize = Math.ceil(rate);
        size = (int) tempsize;
        return size;
    }

    private int cleanCount = 0;
    private boolean isDraw = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isEraser) {
                    cleanCount = 0;
                    isDraw = true;
                    mPath = new Path();
                    mDp = new DrawPath();
                    mDp.path = mPath;
                    mDp.paint = mPaint;
                    touch_start(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isEraser) {
                    touch_move(x, y);
                    invalidate();
                } else {
                    erase(x, y);
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                isDraw = false;
                if (!isEraser) {
                    touch_up();
                    invalidate();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void recycleAll() {
        clearCanvas();//手写过程中导致canas.draw 导致mbitmap引用异常，重置canas.setbitmap,得以安全回收。
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            Log.i("wanan2", "onDetachedFromWindow mBitmap recycle");
            mBitmap = null;
        }

        mRecycleBitmap.recycle();

        System.gc();

        Log.i(TAG, "mytablet onDetachedFromWindow");
        clear();
    }

    /**
     * 当用户200*5毫秒内未操作手写区域，则认定用户完成一次手写
     */
    private class ObserverThread extends Thread {
        public void run() {
            while (!this.isInterrupted()) {
                if (!isDraw && cleanCount >= 5) {
                    cleanCount = 0;
                    //
                } else {
                    if (!isDraw) {
                        cleanCount++;
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public void clear() {
        delStack.push((ArrayList<ArrayList<PointF>>) mPointFsList.clone());
        mPointFsList.clear();
        mSavePath.clear();
    }

    public void undo() {
        if (mPointFsList.size() == 0) {
            return;
        }
        ArrayList<PointF> delPoint = mPointFsList.remove(mPointFsList.size() - 1);
        ArrayList<ArrayList<PointF>> delPoints = new ArrayList<ArrayList<PointF>>();
        delPoints.add((ArrayList<PointF>) delPoint.clone());
        delStack.push(delPoints);
        clearCanvas();

        // goBack();

    }

    public void redo() {
        if (delStack.size() == 0) {
            return;
        }
        ArrayList<ArrayList<PointF>> delPoints = delStack.pop();
        mPointFsList.addAll((Collection<? extends ArrayList<PointF>>) delPoints.clone());
        clearCanvas();
    }

    public void clearCanvas() {
        isCanasEraser = true;
        mBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
        Bitmap old2 = mRecycleBitmap.getBitmap();
        mRecycleBitmap.setBitmap(mBitmap);
        if (old2 != null && old2 != mBitmap) {
            if (!old2.isRecycled()) {
                Log.i("wanan2", "clearCanvas recycle");
                old2.recycle();
                old2 = null;
            }
        }
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(mBgColor);
        invalidate();
    }

    /**
     * 生成手写记录文件
     */
    public String toString() {
        if (isPointEmpty()) {
            return null;
        }

        final StringBuffer sb = new StringBuffer();

        for (int i = 0; i < mAllPagePoints.size(); i++) {
            ArrayList<ArrayList<PointF>> list = mAllPagePoints.get(i);
            if (list.size() == 0) {
                continue;
            } else {
                for (ArrayList<PointF> onePaths : list) {
                    for (int j = 0; j < onePaths.size(); j++) {
                        sb.append(onePaths.get(j).x + "," + onePaths.get(j).y + ";");
                    }
                    sb.append("#");
                }
                sb.append("@");
            }
        }

        Log.i(TAG, sb.toString());
        return sb.toString();
    }

    /**
     * 已字节的形式输出手写记录
     */
    public byte[] printPointsToByte() {
        try {
            return Compress.byteCompress(toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从输出流里获取字节流
     *
     * @param is
     * @return
     */
    public byte[] getContentFromInputStream(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] tempbytes = new byte[1024];
        try {
            int numread;
            while ((numread = is.read(tempbytes)) != -1) {
                bos.write(tempbytes, 0, numread);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return bos.toByteArray();
    }

    public ArrayList<ArrayList<PointF>> getPointFsByBytes(String hwPath) {
        try {
            if (TextUtils.isEmpty(hwPath)) {
                return null;
            }
            File file = new File(hwPath);
            if (!file.exists()) {
                return null;
            }

            mAllPagePoints.clear();

            String pointsStr = "";
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                // ByteArrayOutputStream bos = new ByteArrayOutputStream();
                // Compress.decompress(fis, bos);
                byte[] data = getContentFromInputStream(fis);
                // bos.reset();
                pointsStr = new String(Compress.byteDecompress(data));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!TextUtils.isEmpty(pointsStr)) {
                String[] pages = pointsStr.split("@");
                int notNullPage = 0;
                for (String page : pages) {
                    ArrayList<ArrayList<PointF>> allPoints = new ArrayList<ArrayList<PointF>>();

                    if (TextUtils.isEmpty(page)) {
                        continue;
                    }
                    String[] lines = page.split("#");
                    for (String s : lines) {
                        if (TextUtils.isEmpty(s)) {
                            continue;
                        }
                        String[] points = s.split(";");
                        ArrayList<PointF> pointfs = new ArrayList<PointF>();

                        for (String point : points) {
                            if (!TextUtils.isEmpty(point)) {
                                String[] a = point.split(",");
                                PointF pf = new PointF(Float.valueOf(a[0]) / size, Float.valueOf(a[1]) / size);
                                pointfs.add(pf);
                            }
                        }
                        allPoints.add(pointfs);
                    }
                    mAllPagePoints.put(notNullPage, allPoints);
                    notNullPage++;
                }
            } else {
                ((Activity) mCtx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mCtx, "手写数据错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            mCurrentPage = 0;
            mPointFsList = mAllPagePoints.get(mCurrentPage);
            checkSize();
            checkPoints();

            return mPointFsList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void checkPoints() {
        for (int j = 0; j < mAllPagePoints.size(); j++) {
            ArrayList<ArrayList<PointF>> list = mAllPagePoints.get(j);
            for (ArrayList<PointF> paths : list) {
                for (int i = 0; i < paths.size(); i++) {
                    paths.get(i).x = paths.get(i).x / size;
                    paths.get(i).y = paths.get(i).y / size;
                }
            }
        }
    }

    public boolean saveBitmapAndPoint(Bitmap bitmap, byte[] data, String bitmapPath, String pointPath) {
        if (bitmap == null || data == null || TextUtils.isEmpty(bitmapPath)) {
            return false;
        }
        File bitmapFile = new File(bitmapPath);
        File pointFile = new File(pointPath);
        if (!bitmapFile.exists()) {
            bitmapFile.getParentFile().mkdirs();
            try {
                bitmapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (!pointFile.exists()) {
            pointFile.getParentFile().mkdirs();
            try {
                pointFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
                Log.i("wanan2", "saveBitmapAndPoint recycle");
                bitmap = null;
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fout = null;
            }
        }
        try {
            fout = new FileOutputStream(pointFile);
            fout.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (bitmapFile.exists()) {
                bitmapFile.delete();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            if (bitmapFile.exists()) {
                bitmapFile.delete();
            }
            return false;
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fout = null;
            }
        }
        return true;
    }

    public interface ShowPageListener {
        void hidePreBT();

        void showPreBT();

        void showPageCount(String count);
    }

    private ShowPageListener mShowPageListener;

    private int mCurrentPage = 0;

    private final int MAX_PAGE = 3;

    public void nextPage() {
        // delStack.clear();
        // if (mPointFsList.size() == 0) {
        // Toast.makeText(mCtx, "当页无痕迹，不能继续下一页", Toast.LENGTH_SHORT).show();
        // return;
        // }
        mCurrentPage = mCurrentPage + 1;
        if (mCurrentPage >= MAX_PAGE) {
            mCurrentPage = MAX_PAGE - 1;
            Toast.makeText(mCtx, "最大页数为" + MAX_PAGE + "页，请合理布局", Toast.LENGTH_SHORT).show();

            return;
        }

        if (mAllPagePoints.get(mCurrentPage) == null) {
            ArrayList<ArrayList<PointF>> list = new ArrayList<ArrayList<PointF>>();
            mAllPagePoints.put(mCurrentPage, list);
        }

        if (mAllPagesDelStack.get(mCurrentPage) == null) {
            LinkedList<ArrayList<ArrayList<PointF>>> list = new LinkedList<ArrayList<ArrayList<PointF>>>();
            mAllPagesDelStack.put(mCurrentPage, list);
        }

        if (mShowPageListener != null) {
            mShowPageListener.showPageCount(String.valueOf(mCurrentPage + 1) + "/" + mAllPagePoints.size());
            mShowPageListener.showPreBT();
        }

        delStack = mAllPagesDelStack.get(mCurrentPage);

        clearCanvas();
    }

    public void prePage() {
        mCurrentPage = mCurrentPage - 1;
        if (mCurrentPage <= 0) {
            mCurrentPage = 0;
            if (mShowPageListener != null) {
                mShowPageListener.hidePreBT();
            }
        }
        mShowPageListener.showPageCount(String.valueOf(mCurrentPage + 1) + "/" + mAllPagePoints.size());

        delStack = mAllPagesDelStack.get(mCurrentPage);

        clearCanvas();
    }

    public void setShowPageListener(ShowPageListener showPageListener) {
        this.mShowPageListener = showPageListener;
    }

    public ShowPageListener getShowPageListener() {
        return mShowPageListener;
    }

    public boolean isModify() {
        boolean isModify = false;
        for (int i = 0; i < mAllPagesDelStack.size(); i++) {
            if (mAllPagesDelStack.get(i) != null) {
                if (mAllPagesDelStack.get(i).size() != 0) {
                    isModify = true;
                    break;
                } else {
                    continue;
                }
            }

        }
        return isModify || hasTouch;
    }

}
