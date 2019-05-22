package com.likangr.smartpm.lib.core;

import android.accessibilityservice.AccessibilityService;
import android.content.res.Resources;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.likangr.smartpm.lib.R;
import com.likangr.smartpm.lib.util.ApplicationHolder;
import com.likangr.smartpm.lib.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author likangren
 */
public class SmartPMService extends AccessibilityService {

    private static final String TAG = "SmartPMService";

    private static final Resources RESOURCES = ApplicationHolder.getApplication().getResources();
    private static final String PACKAGE_INSTALLER_PACKAGE_NAME_SUFFIX = "packageinstaller";


    /**
     *
     */
    private static final ArrayList<String> NODE_CONTENT = new ArrayList<>(Arrays.asList(
            RESOURCES.getString(R.string.auto_service_install),
            RESOURCES.getString(R.string.auto_service_ok),
            RESOURCES.getString(R.string.auto_service_ensure),
            RESOURCES.getString(R.string.auto_service_next),
            RESOURCES.getString(R.string.auto_service_replace),
            RESOURCES.getString(R.string.auto_service_update),
            RESOURCES.getString(R.string.auto_service_done),
            RESOURCES.getString(R.string.auto_service_allow),
            RESOURCES.getString(R.string.auto_service_remove),
            RESOURCES.getString(R.string.auto_service_know),
            RESOURCES.getString(R.string.auto_service_continue),
            RESOURCES.getString(R.string.auto_service_failed),

            RESOURCES.getString(R.string.upper_case_auto_service_install),
            RESOURCES.getString(R.string.upper_case_auto_service_ok),
            RESOURCES.getString(R.string.upper_case_auto_service_next),
            RESOURCES.getString(R.string.upper_case_auto_service_replace),
            RESOURCES.getString(R.string.upper_case_auto_service_update),
            RESOURCES.getString(R.string.upper_case_auto_service_done),
            RESOURCES.getString(R.string.upper_case_auto_service_allow),
            RESOURCES.getString(R.string.upper_case_auto_service_remove),
            RESOURCES.getString(R.string.upper_case_auto_service_remove2),
            RESOURCES.getString(R.string.upper_case_auto_service_know),
            RESOURCES.getString(R.string.upper_case_auto_service_continue),
            RESOURCES.getString(R.string.upper_case_auto_service_failed),
            RESOURCES.getString(R.string.upper_case_auto_service_failure),

            RESOURCES.getString(R.string.lower_case_auto_service_install),
            RESOURCES.getString(R.string.lower_case_auto_service_ok),
            RESOURCES.getString(R.string.lower_case_auto_service_next),
            RESOURCES.getString(R.string.lower_case_auto_service_replace),
            RESOURCES.getString(R.string.lower_case_auto_service_update),
            RESOURCES.getString(R.string.lower_case_auto_service_done),
            RESOURCES.getString(R.string.lower_case_auto_service_allow),
            RESOURCES.getString(R.string.lower_case_auto_service_remove),
            RESOURCES.getString(R.string.lower_case_auto_service_remove2),
            RESOURCES.getString(R.string.lower_case_auto_service_know),
            RESOURCES.getString(R.string.lower_case_auto_service_continue),
            RESOURCES.getString(R.string.lower_case_auto_service_fail),
            RESOURCES.getString(R.string.lower_case_auto_service_failed),
            RESOURCES.getString(R.string.lower_case_auto_service_failure)
    ));


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "onCreate");
    }


    /**
     * @param rootInActiveWindow
     * @return
     */
    private boolean isPackageMangerRootNodeInfo(AccessibilityNodeInfo rootInActiveWindow) {
        return rootInActiveWindow != null && rootInActiveWindow.getPackageName().toString().contains(PACKAGE_INSTALLER_PACKAGE_NAME_SUFFIX);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Logger.d(TAG, "AccessibilityEvent=" + event);

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Logger.d(TAG, "current page=" + event);
        }

        if (SmartPM.CURRENT_RUNNING_PM_OPERATION == null) {
            return;
        }
        Logger.d(TAG, "current task: " + SmartPM.CURRENT_RUNNING_PM_OPERATION.toString());

        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();

        if (!isPackageMangerRootNodeInfo(rootInActiveWindow)) {
            return;
        }

        List<AccessibilityNodeInfo> allNodeInfos = new ArrayList<>();
        for (String content : NODE_CONTENT) {
            allNodeInfos.addAll(rootInActiveWindow.findAccessibilityNodeInfosByText(content));
        }

        if (allNodeInfos.size() > 0) {
            for (AccessibilityNodeInfo info : allNodeInfos) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Logger.d(TAG, "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy");
    }

}
