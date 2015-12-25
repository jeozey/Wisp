package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rj.connection.SSLServer;
import com.rj.framework.DB;

import java.io.InputStream;

/**
 * 作者：志文 on 2015/11/18 0018 15:15
 * 邮箱：594485991@qq.com
 */
public class WispCore {
    private static WISPSO wispso = null;
    private static final String TAG = WispCore.class.getName();
    private static HttpServer httpServer;

    private WispCore() {
    }

    public static WISPSO getWISPSO() {
        if (wispso == null) {
            try {
                // jni 加载成功
//                Log.e(TAG, WISPLibs.testLibLoad());

                wispso = new WispCore().new WISPSO();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return wispso;
    }

    public class WISPSO {
        private WISPSO() {

        }

        public void StartService(Handler handler, Context context) {
            try {
                if (!HttpServer.getHasStart()) {
                    Log.e(TAG, "StartService:handler" + handler);
                    // 启动HTTP服务
                    httpServer = new HttpServer(handler, context);
                } else {
                    changeHttpServer(handler, context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean IsServerRun() {
            return HttpServer.getHasStart();
        }

        public void changeHttpServer(Handler handler, Context context) {
            try {
                Log.e(TAG, "changeHttpServer:handler" + handler);
                httpServer.changeHttpServer(handler, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void restartHttpServer() {
            try {
                httpServer.stopHttpServer();
                httpServer.startHttpServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void CloseService() {
            try {
//				HttpServer.stopHttpServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void SSLServerInit(InputStream kclient_is, InputStream tclient_is)
                throws Exception {
            SSLServer.initSSL(kclient_is, tclient_is,
                    DB.CLIENT_KEY_STORE_PASSWORD,
                    DB.CLIENT_TRUST_KEY_STORE_PASSWORD);
        }

    }
}
