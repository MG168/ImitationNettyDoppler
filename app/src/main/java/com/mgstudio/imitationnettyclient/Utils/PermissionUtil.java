package com.mgstudio.imitationnettyclient.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;

public class PermissionUtil {


    public static String getPackageName(Context context) {
        String PackageName = null;
        try {
            PackageName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).packageName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return PackageName;
    }

    public static Boolean check_Permission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permission, getPackageName(context)));
    }

    public static ArrayList<String> checkPermissionList(Context context, ArrayList<String> permissions) {
        PackageManager pm = context.getPackageManager();
        String packagename = getPackageName(context);
        ArrayList<String> nopermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != pm.checkPermission(permission, packagename)) {
                nopermissions.add(permission);
            }
        }
        if(nopermissions.size() == 0)nopermissions.add("success");
        return nopermissions;
    }

    public static ArrayList<String> setPermissionList(){
        ArrayList<String> permissionList = new ArrayList<>();
//        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissionList.add(Manifest.permission.CHANGE_WIFI_STATE);
        permissionList.add(Manifest.permission.INTERNET);
//        permissionList.add(Manifest.permission.CAMERA);
//        permissionList.add(Manifest.permission.BLUETOOTH);
//        permissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        permissionList.add(Manifest.permission.READ_PHONE_STATE);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        permissionList.add(Manifest.permission.READ_LOGS);
//        permissionList.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
        permissionList.add(Manifest.permission.VIBRATE);
        return permissionList;
    }

    public static String getFromPermissionList(int index){
        ArrayList<String> permissionList = setPermissionList();
        return permissionList.get(index);
    }

}
