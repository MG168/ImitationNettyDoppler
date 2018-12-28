package com.mgstudio.imitationnettyclient.Utils;


import android.os.Environment;

import java.io.File;

/**
 * 本地缓存
 */
public class AppCacheUtil {


    public static String rootFolder = "nettyclient";

    public enum FolderType {
        logs("logs");
        private String folder;
        FolderType(String folder) {
            this.folder = folder;
        }
        public String getFolder() {
            return folder;
        }
    }

    private static File getCacheRootFile() {
        File cacheRootDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);  // 判断sd卡是否存在
        if (sdCardExist) {
            cacheRootDir = new File(Environment.getExternalStorageDirectory(), rootFolder);  // 获取根目录
        } else {
            cacheRootDir = new File(rootFolder);
        }
        if (!cacheRootDir.exists()) {
            cacheRootDir.mkdir();  // 如果路径不存在就先创建路径
        }
        return cacheRootDir;
    }

    public static String getPathByFolderType(FolderType ft) {
        File cacheDir = new File(getCacheRootFile(), ft.getFolder());
        if (!cacheDir.exists()) {
            cacheDir.mkdir();  // 如果路径不存在就先创建路径
        }
        return cacheDir.getPath();
    }
}
