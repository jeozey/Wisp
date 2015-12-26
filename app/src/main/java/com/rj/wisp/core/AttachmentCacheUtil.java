package com.rj.wisp.core;

import com.rj.wisp.bean.Attachment;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：志文 on 2015/12/26 0026 12:23
 * 邮箱：594485991@qq.com
 */
public class AttachmentCacheUtil {
    private static final String TAG = AttachmentCacheUtil.class.getName();

    private static Map<String, Attachment> attachments = new HashMap<>();

    public static void clearAttachments() {
        attachments.clear();
    }

    public static void removeAttachment(String url) {
        if (attachments.containsKey(url)) {
            attachments.remove(url);
        }
    }

    public static void putAttachment(Attachment attachment) {
        attachments.put(attachment.getUrl(), attachment);
    }

    public static Attachment getAttachment(String url) {
        if (attachments.containsKey(url)) {
            return attachments.get(url);
        }
        return null;

    }
}
