package com.rj.wisp.ui.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rj.framework.ButtonFactory;
import com.rj.view.MoveLayout;
import com.rj.view.MoveLayoutItemInfo;
import com.rj.view.ToastTool;
import com.rj.view.button.CustomButton;
import com.rj.wisp.R;

import java.util.ArrayList;
import java.util.List;


public class MoreActivity extends Activity {

    private MoveLayout moveLayout;
    private static final String TAG = "MoreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_more);

        moveLayout = (MoveLayout) findViewById(R.id.move_lyt);
        moveLayout.removeAllViews();
        List<MoveLayoutItemInfo> list = getListDatas();
        if (list == null || list.size() == 0) {
            ToastTool.show(getBaseContext(), "无更多项目", Toast.LENGTH_SHORT);
            finish();
        }
        moveLayout.init(list, new MoveLayout.MoveLayoutCallBack() {
            @Override
            public void moveLayoutCallBack(MoveLayoutItemInfo info) {
                Intent data = new Intent();
                data.putExtra("title", info.getTitle());
                data.putExtra("url", info.getCallBack());
                setResult(1, data);
                finish();
            }
        });

    }

    private List<MoveLayoutItemInfo> getListDatas() {
        List<MoveLayoutItemInfo> listDatas = new ArrayList<MoveLayoutItemInfo>();
        try {
            MoveLayoutItemInfo info = new MoveLayoutItemInfo();
            List<CustomButton> collectionlist = (List<CustomButton>) getIntent()
                    .getExtras().getSerializable("buttons");
            for (CustomButton customButton : collectionlist) {
                for (CustomButton pBtn : customButton.getCollction()) {
                    Log.e(TAG, "@" + pBtn.getButtonText());
                    info = new MoveLayoutItemInfo();
                    info.setType(1);
                    info.setDrawable(ButtonFactory.getDrawable(
                            getBaseContext(), pBtn.getBeforeImg()));
                    try {
                        info.setNum(Integer.parseInt(pBtn.getNumber()));
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    info.setTitle(pBtn.getButtonText());
                    info.setCallBack(pBtn.getClickEvent());
                    listDatas.add(info);
                    for (CustomButton sBtn : pBtn.getCollction()) {
                        Log.e(TAG, "*" + sBtn.getButtonText());
                        info = new MoveLayoutItemInfo();
                        info.setType(2);
                        info.setDrawable(ButtonFactory.getDrawable(
                                getBaseContext(), sBtn.getBeforeImg()));
                        try {
                            info.setNum(Integer.parseInt(sBtn.getNumber()));
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        info.setTitle(sBtn.getButtonText());
                        info.setCallBack(sBtn.getClickEvent());
                        listDatas.add(info);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listDatas;
    }

    /**
     * 响应界面中的返回按钮
     *
     * @param view
     */
    public void onClickBackBtn(View view) {
        if (view.getId() == R.id.more_app_module_back_btn) {
            this.finish();
        }
    }

}
