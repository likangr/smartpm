package com.likangr.smartpm.lib.util;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * @author likangren
 */
public class PackageUtil {

    /**
     * @param packageName
     * @return
     */
    public static boolean packageIsInstalled(String packageName) {
        return getInstalledPackageInfo(packageName) != null;
    }

    /**
     * @param packageName
     * @return
     */
    public static PackageInfo getInstalledPackageInfo(String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = ApplicationHolder.getApplication().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return packageInfo;
    }

    /**
     * @return
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static boolean canRequestPackageInstalls() {
        return ApplicationHolder.getApplication().getPackageManager().canRequestPackageInstalls();
    }
}
