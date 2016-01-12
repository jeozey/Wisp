package com.rj.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rj.view.MyTabletView.ShowPageListener;
import com.rj.widget.utils.Compress;
import com.rj.widgetlib.R;

public class MainTabletLayout extends LinearLayout {

	private MyTabletView mMyTabletView;
	private Button mBtnUndo, mBtnRedo;
	private Button mBtnSave;
	private Button mBtnEraser;
	private ImageView mBtnExit;
	private String mPngPath, mHwPath, mLastHWPath, mHwPathShow;
	private Button mLastPathBT;
	private Button mClearAllBT;
	private Button mPrePageBT;
	private Button mNextPageBT;
	private Context mContext;
	private TextView mPageTV;

	public MainTabletLayout(Context context) {
		super(context);
		mContext = context;
	}

	public MainTabletLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.mytablet, this, true);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mBtnUndo = (Button) findViewById(R.id.btn_undo);
		mBtnRedo = (Button) findViewById(R.id.btn_redo);
		mBtnSave = (Button) findViewById(R.id.btn_save);
		mBtnEraser = (Button) findViewById(R.id.btn_eraser);
		mBtnExit = (ImageView) findViewById(R.id.btn_exit);
		mMyTabletView = (MyTabletView) findViewById(R.id.mytabletview);
		mLastPathBT = (Button) findViewById(R.id.btn_rem);
		mClearAllBT = (Button) findViewById(R.id.btn_clear_all);
		mPrePageBT = (Button) findViewById(R.id.pre_page);
		mPrePageBT.setTextColor(Color.GRAY);
		mNextPageBT = (Button) findViewById(R.id.next_page);
		mPageTV = (TextView) findViewById(R.id.page_count);
		mPrePageBT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyTabletView.prePage();
			}
		});

		mNextPageBT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyTabletView.nextPage();
			}
		});

		mMyTabletView.setShowPageListener(new ShowPageListener() {
			@Override
			public void hidePreBT() {
				mPrePageBT.setEnabled(false);
				mPrePageBT.setTextColor(Color.GRAY);
			}

			@Override
			public void showPreBT() {
				mPrePageBT.setEnabled(true);
				mPrePageBT.setTextColor(Color.parseColor("#fffffb"));
			}

			@Override
			public void showPageCount(String count) {
				mPageTV.setText(count);
			}
		});

		mClearAllBT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyTabletView.clearCanvas();
				if (mMyTabletView.getPointFsList().size() != 0)
					mMyTabletView.clear();
			}
		});

		mLastPathBT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Compress.buildDialog(mContext, "上次痕迹", "即将删除当前痕迹", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								mMyTabletView.lastPath(mLastHWPath);
							}
						},

						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
			}
		});

		mBtnEraser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isEraser = mMyTabletView.isEraser();
				isEraser = !isEraser;
				mMyTabletView.setEraser(isEraser);
				if (isEraser) {
					mBtnEraser.setBackgroundResource(R.drawable.tablet_btn_true);
				} else {
					mBtnEraser.setBackgroundResource(R.drawable.tablet_save_btn_false);
				}

			}
		});
		mBtnUndo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyTabletView.undo();

			}
		});
		mBtnRedo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyTabletView.redo();

			}
		});
		mBtnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				save();
			}
		});
		mBtnExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOnTabletResultListener != null) {
					mOnTabletResultListener.onCloseTablet();
				}
			}
		});
	}

	public void save() {
		Bitmap tempBitmap = mMyTabletView.processBitmap();
		if (mOnTabletResultListener != null) {
			boolean isSaveSuccess = mMyTabletView.saveBitmapAndPoint(tempBitmap, mMyTabletView.printPointsToByte(), mPngPath, mHwPath);
			// mOnTabletResultListener.onShowBitmap(tempBitmap);
			if (isSaveSuccess) {
				mOnTabletResultListener.onSaveSuccessCallBack(mPngPath, mHwPath);
			} else {
				mOnTabletResultListener.onSaveErrorCallBack();
			}
		}
	}

	public void setSavePath(String pngPath, String hwPath, String pngPathShow, String hwPathShow) {
		mPngPath = pngPath;
		mHwPath = hwPath;
		mLastHWPath = pngPathShow;
		mHwPathShow = hwPathShow;
		mMyTabletView.getPointFsByBytes(mHwPathShow);
		if (TextUtils.isEmpty(mLastHWPath)) {
			mLastPathBT.setVisibility(View.GONE);
		}
	}

	public interface onTabletResultListener {
		void onShowBitmap(Bitmap bitmap);

		void onSavePointFs(String pointfsInfo);

		/**
		 * 手写图片保存成功后回调接口
		 */
		void onSaveSuccessCallBack(String pngPath, String hwPath);

		/**
		 * 手写图片保存失败后回调接口
		 */
		void onSaveErrorCallBack();

		void onCloseTablet();
	}

	private onTabletResultListener mOnTabletResultListener;

	public onTabletResultListener getTabletResultListener() {
		return mOnTabletResultListener;
	}

	public void setOnTabletResultListener(onTabletResultListener mOnTabletResultListener) {
		this.mOnTabletResultListener = mOnTabletResultListener;
	}

	public void recycle() {
		mMyTabletView.setEraser(false);
		mBtnEraser.setBackgroundResource(R.drawable.tablet_save_btn_false);
		mMyTabletView.recycleAll();
	}

	public boolean isModify() {
		if (mMyTabletView != null)
			return mMyTabletView.isModify();
		else {
			return false;
		}
	}

}
