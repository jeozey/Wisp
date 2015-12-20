package com.rj.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class FileUtil {
    private static final String TAG = FileUtil.class.getName();

    public static int randomDownLoad(String filePath, byte[] arrByte, int pos,
                                     int length) {
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
            raf.seek(pos);
            raf.write(arrByte, 0, length);
            raf.close();
            raf = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return pos;
        }
        return pos + length;
    }

    public static void delFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] childs = file.listFiles();
                for (File item : childs) {
                    if (item.isFile()) {
                        item.delete();
                    } else if (item.isDirectory()) {
                        delFile(item.getAbsolutePath());
                    }
                }
                file.delete();
            } else if (file.isFile()) {
                file.delete();
            }
        }
    }

    public static final File createOrReplaceFile(File file) {
        try {
            if (!file.getParentFile().exists()) {
                if (file.getParentFile().mkdirs()) {
                    file.createNewFile();
                }
            } else {
                file.createNewFile();
            }
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    public static final File createOrReplaceDir(File dir) {
        if (!dir.getParentFile().exists()) {
            if (dir.getParentFile().mkdirs()) {
                dir.mkdir();
            }
        } else {
            dir.mkdir();
        }
        return dir;
    }

    public static final void writeFile(String filepath, byte[] content)
            throws IOException {
        File file = new File(filepath);
        File folder = file.getParentFile();
        if (!folder.isDirectory() && !folder.exists()) {
            Log.e(TAG, "mkdirs:" + folder.getAbsolutePath());
            folder.mkdirs();
        }
        if (!file.exists()) {
            Log.e(TAG, "createNewFile file:" + file.getAbsolutePath());
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.flush();
        fos.close();
    }

    public static final void writeFile(File file, byte[] content)
            throws IOException {
        File folder = file.getParentFile();
        if (!folder.isDirectory() && !folder.exists()) {
            Log.e(TAG, "mkdirs:" + folder.getAbsolutePath());
            folder.mkdirs();
        }
        if (!file.exists()) {
            Log.e(TAG, "createNewFile file:" + file.getAbsolutePath());
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.flush();
        fos.close();
    }

    public static final void write(File source, File target) {
        if (!source.exists()) {
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(
                    createOrReplaceFile(target));
            FileInputStream in = new FileInputStream(source);
            byte[] b = new byte[8192];
            int len = 0;
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static final void write(InputStream in, File target) {
        try {
            FileOutputStream out = new FileOutputStream(
                    createOrReplaceFile(target));
            byte[] b = new byte[8192];
            int len = 0;
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getTypeName(String contentType) {
        String typeName = "";
        if ("".equals(contentType)) {
            return typeName;
        }
        if (contentType.indexOf("application/msword") != -1) {
            typeName = "doc";
        } else if (contentType
                .indexOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document") != -1) {
            typeName = "docx";
        } else if (contentType.indexOf("application/vnd.ms-excel") != -1) {
            typeName = "xls";
        } else if (contentType
                .indexOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") != -1) {
            typeName = "xlsx";
        } else if (contentType.indexOf("application/vnd.ms-powerpoint") != -1) {
            typeName = "ppt";
        } else if (contentType
                .indexOf("application/vnd.openxmlformats-officedocument.presentationml.presentation") != -1) {
            typeName = "pptx";
        } else if (contentType.indexOf("image/png") != -1) {
            typeName = "png";
        } else if (contentType.indexOf("image/jpeg") != -1) {
            typeName = "jpg";
        } else if (contentType.indexOf("image/gif") != -1) {
            typeName = "gif";
        } else if (contentType.indexOf("text/plain") != -1) {
            typeName = "txt";
        } else if (contentType.indexOf("application/x-rar-compressed") != -1) {
            typeName = "rar";
        } else if (contentType.indexOf("application/java-archive") != -1) {
            typeName = "jar";
        } else if (contentType.indexOf("application/zip") != -1) {
            typeName = "zip";
        } else if (contentType.indexOf("application/pdf") != -1) {
            typeName = "pdf";
        } else if (contentType.indexOf("text/html") != -1) {
            typeName = "pdf";
        }
        return typeName;
    }

    /**
     * 删除文件夹里面的所有文件
     *
     * @param path String 文件夹路径或者文件的绝对路径 如：/mnt/sdcard/test/1.png
     */
    private static void deleteAllFile(String path) {
//		Log.e("FileUtil", "deleteAllFile:"+path);
        // 在内存开辟一个文件空间，但是没有创建
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
//		Log.e("FileUtil", "deleteAllFile1:"+path);
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] tempList = file.listFiles();
            for (File file2 : tempList) {
                deleteDirectory(file2.getAbsolutePath());
            }
        }
    }

    /**
     * 删除文件夹或者文件
     *
     * @param folderPath String 文件夹路径或者文件的绝对路径 如：/mnt/sdcard/test/1.png
     */
    public static void deleteDirectory(String folderPath) {
        try {
            // 删除文件夹里所有的文件及文件夹
            deleteAllFile(folderPath);
            File lastFile = new File(folderPath);
            if (lastFile.exists()) {
                Log.e("file", "delete:" + lastFile.getAbsolutePath());
                // 最后删除空文件夹
                lastFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 以下为从Raw文件中读取：
    public static String getFromRaw(Context context, int rawId) {
        try {
            InputStreamReader inputReader = new InputStreamReader(context
                    .getResources().openRawResource(rawId));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            StringBuffer result = new StringBuffer();
            while ((line = bufReader.readLine()) != null)
                result.append(line);
            bufReader.close();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 以下为直接从assets读取
    public static String getFromAssets(Context context, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            InputStreamReader inputReader = new InputStreamReader(context
                    .getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            StringBuffer result = new StringBuffer();
            while ((line = bufReader.readLine()) != null)
                result.append(line);
            inputReader.close();
            bufReader.close();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 以下为直接从sdCard读取
    public static String readFile(File file) {
        return readFile(file.getAbsoluteFile());
    }

    public static String readFile(String fileName)
            throws FileNotFoundException {
        try {
            System.out.println("fileName:" + fileName);
            File file = new File(fileName);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            BufferedReader bufReader = new BufferedReader(new FileReader(
                    new File(fileName)));
            String line = "";
            StringBuffer result = new StringBuffer();
            while ((line = bufReader.readLine()) != null)
                result.append(line);
            bufReader.close();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
