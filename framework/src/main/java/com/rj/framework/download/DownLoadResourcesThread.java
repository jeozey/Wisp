package com.rj.framework.download;

import com.rj.framework.DB;

import java.io.OutputStream;
import java.net.Socket;

/***
 * 从手机端对平板电脑端进行代码优化（AjaxGetResource）
 * <p/>
 * 发起下载资源请求
 */
public class DownLoadResourcesThread extends Thread {
    Socket socket = null;
    OutputStream os = null;
    String filepath = "";
    String modified = "";
    String filetype = "";

    public DownLoadResourcesThread(String filepath, String filetype, String modified) {
        // TODO Auto-generated constructor stub
        this.filepath = filepath;
        this.filetype = filetype;
        this.modified = modified;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(80000);
            os = socket.getOutputStream();
            os.write(("GET /wisp_aas/" + filepath + " HTTP/1.1" + "\r\n").getBytes());
            os.write(("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n").getBytes());
            os.write(("Method-Type: download" + "\r\n").getBytes());
            os.write(("File-Time: " + modified + "\r\n").getBytes());
            os.write(("File-Type: " + filetype + "\r\n").getBytes());
            os.write(("File-Name: " + filepath + "\r\n").getBytes());
            os.write(("Accept: */*" + "\r\n").getBytes());
            os.write(("Accept-Encoding: gzip, deflate" + "\r\n").getBytes());
            os.write(("Accept-Language: zh-CN, en-US" + "\r\n").getBytes());
            os.write(("Connection: Keep-Alive" + "\r\n").getBytes());
            os.write("\r\n".getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (Exception e2) {
                    // TODO: handle exception
                }
        }
    }
}
