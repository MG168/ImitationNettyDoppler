package com.mgstudio.imitationnettyclient.Utils;


import android.Manifest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import timber.log.Timber;

/**
 * 日志持久化
 */
public class WriteLogUtil {

    public static void writeLogByThread(final String log) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeLog(log);
            }
        }).start();
    }

    public static void writeLog(String log) {
        String date = DateFormatUtil.format(System.currentTimeMillis(), DateFormatUtil.DateFormatEnum.ymd);
        String dir = AppCacheUtil.getPathByFolderType(AppCacheUtil.FolderType.logs);
        File dirFile = new File(dir, date);
        if (!dirFile.exists()
                && PermissionUtil.check_Permission(MyApplication.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && PermissionUtil.check_Permission(MyApplication.getContext(),Manifest.permission.READ_EXTERNAL_STORAGE)) {
            dirFile.mkdir();
        }else return;
        File file = new File(dirFile, "log.txt");
        try {
            String dateTime = DateFormatUtil.format(System.currentTimeMillis(), DateFormatUtil.DateFormatEnum.ymdhms);
            FileOutputStream fileOut = new FileOutputStream(file, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileOut);
            outputWriter.write(String.format("%s:%s\n", dateTime, log));
            outputWriter.close();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }
}
