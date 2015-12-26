package com.rj.wisp.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.rj.wisp.bean.Attachment;

/**
 * 作者：志文 on 2015/12/26 0026 14:29
 * 邮箱：594485991@qq.com
 */
public class FileOpenUtil {
    private static final String TAG = FileOpenUtil.class.getName();
    private static final String PDF = "application/pdf";

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
        intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + filepath),
                contentType);
        return intent;
    }
}
