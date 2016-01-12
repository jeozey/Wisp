package com.rj.wisp.tablet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.rj.view.MainTabletLayout;
import com.rj.wisp.R;

public class TabletActivity extends Activity {
    private MainTabletLayout mlt;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mlt != null)
            mlt.recycle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_tablet_layout);

        final String savePngPath = getIntent().getStringExtra("savePngPath");
        final String saveHwPath = getIntent().getStringExtra("saveHwPath");
        String showHwPath = getIntent().getStringExtra("showHwPath");
        String lastHWPath = getIntent().getStringExtra("lastHWpath");

        mlt = (MainTabletLayout) findViewById(R.id.phone_tablet);
        mlt.setSavePath(savePngPath, saveHwPath, lastHWPath, showHwPath);
        mlt.setOnTabletResultListener(new MainTabletLayout.onTabletResultListener() {
            @Override
            public void onShowBitmap(Bitmap bitmap) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onSaveSuccessCallBack(String pngPath, String hwPath) {
                Toast.makeText(TabletActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("savePngPath", pngPath);
                intent.putExtra("saveHwPath", hwPath);
                setResult(2, intent);
                finish();
            }

            @Override
            public void onSavePointFs(String pointfsInfo) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSaveErrorCallBack() {
                Toast.makeText(TabletActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                setResult(4);
            }

            @Override
            public void onCloseTablet() {
                if (mlt.isModify())
                    showAlert();
                else {
                    setResult(3);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mlt.isModify())
            showAlert();
        else {
            setResult(3);
            finish();
        }
    }

    private void showAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(TabletActivity.this);
        dialog.setTitle("系统提示");
        dialog.setMessage("是否需要保存更改?");
        dialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mlt.save();
            }
        });
        dialog.setNeutralButton("不保存", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setResult(3);
                finish();
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

}
