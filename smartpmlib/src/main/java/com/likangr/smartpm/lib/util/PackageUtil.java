package com.likangr.smartpm.lib.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

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
    public static boolean canRequestPackageInstalls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ApplicationHolder.getApplication().getPackageManager().canRequestPackageInstalls();
        } else {
            return Settings.Secure.getInt(ApplicationHolder.getApplication().getContentResolver(),
                    Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;
        }

    }
}
