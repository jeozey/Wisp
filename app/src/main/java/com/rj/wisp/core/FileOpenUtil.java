package com.rj.wisp.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.rj.wisp.bean.Attachment;

import java.io.File;

/**
 * 作者：志文 on 2015/12/26 0026 14:29
 * 邮箱：594485991@qq.com
 */
public class FileOpenUtil {
    private static final String TAG = FileOpenUtil.class.getName();
    private static final String PDF = "application/pdf";
    private static final String EXCEL = "application/vnd.ms-excel";
    private static final String WORD = "application/msword";
    private static final String TXT = "text/plain";
    private static final String HTML = "text/html";

    public static Intent openFile(Attachment attachment, Context context) {
        Intent intent;
        Log.e(TAG, "openFile attachment:" + attachment);
        String contentType = attachment.getContentType();
        String filepath = attachment.getPath();
        try {
//            if(PDF.equals(contentType)){
//                intent = new Intent(context, PDFReadActivity.class);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(filepath));
        intent.setDataAndType(uri, contentType);
        return intent;
    }
}
