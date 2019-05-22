package com.likangr.smartpm.lib.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.likangr.smartpm.lib.ConstantValue;
import com.likangr.smartpm.lib.core.guid.UserActionBridgeActivity;

import java.io.File;
import java.util.List;

/**
 * @author likangren
 */
public class IntentManager {

    private static final String TAG = "IntentManager";
    /**
     *
     */
    private static final String RUN_PACKAGE_INTENT_ACTION = "smartpm.%s.intent.action.MAIN";

    /**
     * @param context
     * @param path
     * @param intent
     * @return
     */
    private static Uri getFileUri(Context context, String path, Intent intent) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            File file = new File(path);
            uri = FileProvider.getUriForFile(context, ConstantValue.FILE_PROVIDER_AUTHORITY, file);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

        } else {
            uri = Uri.parse("file://" + path);
        }
        return uri;
    }

    /**
     * @param actionCode
     * @param onUserDoneCallback
     */
    public static void gotoUserActionBridgeActivity(int actionCode, UserActionBridgeActivity.OnUserActionDoneCallback onUserDoneCallback) {
        Application application = ApplicationHolder.getApplication();
        UserActionBridgeActivity.setOnUserActionDoneCallback(onUserDoneCallback);
        Intent intent = new Intent(application, UserActionBridgeActivity.class);
        intent.putExtra(UserActionBridgeActivity.INTENT_EXTRA_KEY_USER_ACTION_DONE_CALLBACK_ID, onUserDoneCallback.hashCode());
        intent.putExtra(UserActionBridgeActivity.INTENT_EXTRA_KEY_USER_ACTION_CODE, actionCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }


    /**
     * @param packageArchiveLocalPath
     */
    public static void gotoInstallPackageActivity(String packageArchiveLocalPath) {
        Application application = ApplicationHolder.getApplication();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = IntentManager.getFileUri(application, packageArchiveLocalPath, intent);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        application.startActivity(intent);
    }

    /**
     * @param packageName
     */
    public static void gotoRemovePackageActivity(String packageName) {
        Application application = ApplicationHolder.getApplication();
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse(String.format("package:%s", packageName)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    /**
     * @param packageName
     * @return
     */
    public static boolean runIndependentPackage(String packageName) {
        Application application = ApplicationHolder.getApplication();
        Intent intent = application.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            application.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param packageName
     * @return
     */
    public static boolean runPluginPackage(String packageName) {
        Application application = ApplicationHolder.getApplication();
        Intent intent = new Intent(String.format(RUN_PACKAGE_INTENT_ACTION, packageName), null);

        PackageManager pm = application.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        String className = "";
        for (ResolveInfo info : resolveInfos) {
            if (packageName.equals(info.activityInfo.packageName)) {
                className = info.activityInfo.name;
                break;
            }
        }
        if (!TextUtils.isEmpty(className)) {
            intent.setComponent(new ComponentName(packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static void gotoInstallPermissionSettingActivity(Context context) {
        Uri uri = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * @param context
     * @return
     */
    public static boolean gotoAccessibilitySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
