package com.rj.wisp.util;

import android.util.Log;

import com.alibaba.fastjson.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

public class SourceFileUtil {

    public static boolean isWriting = false;

    public static HashMap<String, String[]> read(String path) {
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return read(f);
        }
        return null;
    }

    public static HashMap<String, String[]> read(File file) {
        if (file != null && file.exists() && file.isFile()) {
            StringBuffer fileContent = new StringBuffer();
            try {
                FileInputStream fis = new FileInputStream(file);
                byte b[] = new byte[1024];
                int i = fis.read(b);
                while (i != -1) {
                    fileContent.append(new String(b, 0, i));
                    i = fis.read(b);
                }
                fis.close();
//				Log.e("resource", "read Json:" + fileContent.toString());
                return com.alibaba.fastjson.JSON.parseObject(
                        fileContent.toString(),
                        new TypeReference<HashMap<String, String[]>>() {
                        });
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static void writeResourceJson(HashMap<String, String[]> sourceMap,
                                         String resourceJsonPath) {
        if (isWriting) {
            return;
        }
        isWriting = true;
        try {
            if (sourceMap != null) {
                Log.e("resource", "writeResourceJson:" + sourceMap.size());
                String json = com.alibaba.fastjson.JSON.toJSONString(sourceMap);
                Log.e("resource", "json:" + json);
                File sourceList = new File(resourceJsonPath);
                if (!sourceList.getParentFile().exists()) {
                    sourceList.getParentFile().mkdirs();
                }
                sourceList.createNewFile();
                FileOutputStream os = new FileOutputStream(sourceList);
                os.write(json.getBytes());
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isWriting = false;
        }

    }

}
