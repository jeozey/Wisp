package com.rj.wisp.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.rj.framework.DB;
import com.rj.view.FloatView;
import com.rj.view.R;
import com.rj.wisp.bean.ConnectionStatus;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.core.LocalSocketRequestTool;


public class NetConnectService extends Service {
    private static final String TAG = NetConnectService.class.getName();

    // 定义内容类继承Binder
    public class LocalBinder extends Binder {
        // 返回本地服务
        public NetConnectService getService() {
            return NetConnectService.this;
        }
    }

    public interface CallBackToStartContext {
        void callBack(int img, String msg);
    }

    private FloatView view;

    private LocalSocketRequestTool tool;
    private int timeStamp = 10;
    private int timeOutCount = 0;
    /**
     * 定义线程周期性地获取网速
     */
    private Runnable mRunnable = new Runnable() {
        // 每N秒钟获取一次数据
        @Override
        public void run() {
            // Log.e(TAG, "running check network");
            refresh();
            if (timeOutCount > 5) {
                if (DB.isPhone && !DB.isPortrait) {
                    if (view != null) {
                        view.removeView();
                    }
                }
                setNotificationImg();
                showDialog(getBaseContext());
            }
            mHandler.postDelayed(mRunnable, timeStamp * 1000);
        }
    };

    private AlertDialog dialog;

    private void showDialog(Context mContext) {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("系统提示");
            builder.setMessage("无法连接到服务器\n请确认当前网络是否可用以及是否切换到正确的APN!");
            builder.setPositiveButton("确定", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timeOutCount = 0;
                }
            });
            dialog = builder.create();
            dialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();
        } else if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        if (intent != null && oldTime == System.currentTimeMillis()
                && intent.getBooleanExtra("FLAG_IS_EXIT", false)) {
            Log.e(TAG, "onStartCommand-->退出");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind()");
        mHandler.removeCallbacks(mRunnable);
        if (DB.isPhone && !DB.isPortrait) {
            // deleteIconToStatusbar();
            stopSelf();
        }
        return super.onUnbind(intent);
    }

    /**
     * 在服务结束时删除消息队列
     */
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()");
        mHandler.removeCallbacks(mRunnable);
        if (DB.isPhone && !DB.isPortrait) {
            deleteIconToStatusbar();
        } else {
            if (view != null) {
                view.removeView();
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind()");
        return null;
    }

    long oldTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        oldTime = System.currentTimeMillis();

        try {

            if (DB.isPhone && !DB.isPortrait) {
                setNotificationImg();
            } else {
                // 悬浮窗
                view = new FloatView(this);
                DisplayMetrics dm = getResources().getDisplayMetrics();
                if (DB.isPortrait) {
                    view.show(0, 0, dm.widthPixels,
                            dm.heightPixels,
                            getResources().getColor(R.color.float_textView2), R.mipmap.center_p);
                } else {
                    view.show(0, 0, dm.widthPixels, dm.heightPixels, getResources()
                            .getColor(R.color.float_textView1), R.mipmap.center);
                }
            }

            tool = new LocalSocketRequestTool();
            mHandler.postDelayed(mRunnable, 0);

        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
            //异常退出系统的时候,service还会重启,此时根据异常来关闭此服务
            stopSelf();
            deleteIconToStatusbar();
        }
    }

    private enum CONNECT_STATE {
        TIME_OUT, BAD, GOOD, PERFECT
    }

    protected class ConnectStatus {
        private int leftImg;
        private int rightImg;
        private String title;

        protected ConnectStatus() {
        }

        protected ConnectStatus(int leftImg, int rightImg, String title) {
            this.leftImg = leftImg;
            this.rightImg = rightImg;
            this.title = title;
        }


    }

    private CONNECT_STATE oaState = CONNECT_STATE.TIME_OUT;
    private CONNECT_STATE serverState = CONNECT_STATE.TIME_OUT;
    private ConnectStatus currServerStatus;
    private ConnectStatus currOaStatus;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "msg.what:" + msg.what);
            ConnectionStatus status = (ConnectionStatus) msg.obj;
            if (status == null)
                return;

            Log.e(TAG, "result:" + status);
            long time = status.getToOaTime();

            if (msg.what == HandlerWhat.GET_CONNECTION_SUCC) {
                try {
                    if (time > 10000 || time == -1) {
                        oaState = CONNECT_STATE.TIME_OUT;
                    } else if (time > 5000) {
                        oaState = CONNECT_STATE.BAD;
                    } else if (time > 1000) {
                        oaState = CONNECT_STATE.GOOD;
                    } else {
                        oaState = CONNECT_STATE.PERFECT;
                    }

                    long toServerTime = status.getToServerTime();
                    if (toServerTime > 10000) {
                        timeStamp = 5;
                        timeOutCount++;
                        serverState = CONNECT_STATE.TIME_OUT;
                    } else if (toServerTime > 5000) {
                        timeStamp = 6;
                        timeOutCount = 0;
                        serverState = CONNECT_STATE.BAD;
                    } else if (toServerTime > 1000) {
                        timeStamp = 9;
                        timeOutCount = 0;
                        serverState = CONNECT_STATE.GOOD;
                    } else {
                        timeStamp = 10;
                        timeOutCount = 0;
                        serverState = CONNECT_STATE.PERFECT;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                timeStamp = 5;
                timeOutCount++;
                serverState = CONNECT_STATE.TIME_OUT;
            }
            setNotificationImg();
        }
    };

    private final ConnectStatus phoneTimeOutStatus = new ConnectStatus(R.mipmap.pnetwork0, 0, "超时");
    private final ConnectStatus phoneBadStatus = new ConnectStatus(R.mipmap.pnetwork1, 0, "较差");
    private final ConnectStatus phoneGoodStatus = new ConnectStatus(R.mipmap.pnetwork4, 0, "顺畅");
    private final ConnectStatus phonePerfectStatus = new ConnectStatus(R.mipmap.pnetwork5, 0, "理想");

    private final ConnectStatus padTimeOutStatus = new ConnectStatus(R.mipmap.left1, R.mipmap.right1, "超时");
    private final ConnectStatus padBadStatus = new ConnectStatus(R.mipmap.left2, R.mipmap.right2, "较差");
    private final ConnectStatus padGoodStatus = new ConnectStatus(R.mipmap.left3, R.mipmap.right3, "顺畅");
    private final ConnectStatus padPerFectStatus = new ConnectStatus(R.mipmap.left4, R.mipmap.right4, "理想");

    private void setNotificationImg() {
        Log.e(TAG, "serverState:" + serverState + " oaState:" + oaState);
        ConnectStatus serverStatus = new ConnectStatus();
        ConnectStatus oaStatus = new ConnectStatus();
        if (DB.isPhone) {
            if (serverState == CONNECT_STATE.TIME_OUT) {
                serverStatus = phoneTimeOutStatus;
            } else if (serverState == CONNECT_STATE.BAD) {
                serverStatus = phoneBadStatus;
            } else if (serverState == CONNECT_STATE.GOOD) {
                serverStatus = phoneGoodStatus;
            } else {
                serverStatus = phonePerfectStatus;
            }
        } else {
            if (oaState == CONNECT_STATE.TIME_OUT) {
                oaStatus = padTimeOutStatus;
            } else if (oaState == CONNECT_STATE.BAD) {
                oaStatus = padBadStatus;
            } else if (oaState == CONNECT_STATE.GOOD) {
                oaStatus = padGoodStatus;
            } else {
                oaStatus = padPerFectStatus;
            }

            if (serverState == CONNECT_STATE.TIME_OUT) {
                serverStatus = padTimeOutStatus;
            } else if (serverState == CONNECT_STATE.BAD) {
                serverStatus = padBadStatus;
            } else if (serverState == CONNECT_STATE.GOOD) {
                serverStatus = padGoodStatus;
            } else {
                serverStatus = padPerFectStatus;
            }
        }
        if (DB.isPhone && !DB.isPortrait) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setContentTitle("正在运行!")
                    .setContentText("网络连接")
//					.setContentIntent(pi)
                    .setSmallIcon(serverStatus.leftImg)
                    .setWhen(System.currentTimeMillis());
            Notification notification = builder.getNotification();
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);

        } else {
            view.tv_showL.setText(serverStatus.leftImg);
            view.tv_showR.setText(serverStatus.rightImg);
            view.img_showL.setImageResource(oaStatus.leftImg);
            view.img_showR.setImageResource(oaStatus.rightImg);
        }
    }

    private final static int NOTIFICATION_ID = 0x12345;


    private void deleteIconToStatusbar() {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void refresh() {
        AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Integer... params) {
                tool.checkConnection(mHandler);
                return 0;

            }
        };
        task.execute(0);
    }


}
