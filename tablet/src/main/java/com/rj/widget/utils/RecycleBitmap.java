package com.rj.widget.utils;

import android.graphics.Bitmap;
import android.util.Log;

public class RecycleBitmap {
	private Bitmap mBitmap;

	public RecycleBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public void recycle() {
		if (mBitmap != null) {
			mBitmap.recycle();
			Log.i("wanan2", "RecycleBitmap recycle");
			mBitmap = null;
		}
	}
}
