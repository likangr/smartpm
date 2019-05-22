package com.likangr.smartpm.lib.core;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.likangr.smartpm.lib.R;
import com.likangr.smartpm.lib.util.ApplicationHolder;
import com.likangr.smartpm.lib.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likangren
 */
public class SmartPMService extends AccessibilityService {

    private static final String TAG = "SmartPMService";

    private static final String PACKAGE_INSTALLER_PACKAGE_NAME_SUFFIX = "packageinstaller";

    private static final String[] SMARTPM_NODE_TEXT = ApplicationHolder.getApplication().getResources().getStringArray(R.array.smartpm_node_text);

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

        if (SmartPM.sCurrentRunningPMOperation == null) {
            return;
        }

        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();

        if (!isPackageMangerRootNodeInfo(rootInActiveWindow)) {
            return;
        }

        List<AccessibilityNodeInfo> allNodeInfos = new ArrayList<>();
        for (String content : SMARTPM_NODE_TEXT) {
            allNodeInfos.addAll(rootInActiveWindow.findAccessibilityNodeInfosByText(content));
        }

        for (AccessibilityNodeInfo info : allNodeInfos) {
            while (!info.performAction(AccessibilityNodeInfo.ACTION_CLICK) && (info = info.getParent()) != null)
                ;
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
