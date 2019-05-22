package com.likangr.smartpm.lib.core;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.likangr.smartpm.lib.exception.StringFieldEmptyException;
import com.likangr.smartpm.lib.util.ApplicationHolder;
import com.likangr.smartpm.lib.util.IntentManager;

import java.util.ArrayList;

/**
 * @author likangren
 */
public class SmartPM {

    private static final String TAG = "SmartPM";

    private static final ArrayList<PMOperation> PM_OPERATION_QUEUE = new ArrayList<>();

    public static PMOperation sCurrentRunningPMOperation;
    private static final Object INITIALISE_LOCK = new Object();
    private static Handler sHandler;
    private static boolean sIsInitialised = false;

    public static final int FAIL_REASON_SET_LOCATION_ENABLED_USER_REJECT = 1;
    public static final int FAIL_REASON_LOCATION_MODULE_NOT_EXIST = 2;
    public static final int FAIL_REASON_NOT_HAS_LOCATION_PERMISSION = 3;

    public static final int FAIL_REASON_WIFI_MODULE_NOT_EXIST = 4;
    public static final int FAIL_REASON_NOT_HAS_WIFI_PERMISSION = 5;
    public static final int FAIL_REASON_SET_WIFI_ENABLED_TIMEOUT = 7;

    /**
     * @param application
     */
    public static void initCore(Application application) {
        synchronized (INITIALISE_LOCK) {
            if (sIsInitialised) {
                return;
            }
            ApplicationHolder.init(application);
            sHandler = new Handler(Looper.getMainLooper());
            sIsInitialised = true;
        }
    }


    /**
     * @return
     */
    public static Handler getHandler() {
        checkIsInitialised();
        return sHandler;
    }

    /**
     * @param pmOperation
     */
    public static void postPMOperation(final PMOperation pmOperation) {
        checkIsInitialised();
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                checkPMOperationIsValid(pmOperation);
                PM_OPERATION_QUEUE.add(pmOperation);
                if (sCurrentRunningPMOperation == null) {
                    executeNextPMOperationIfHas();
                }
            }
        });

    }

    /**
     * @param packageName
     * @param isPlugin
     * @return
     */
    public static boolean runPackage(String packageName, boolean isPlugin) {
        checkIsInitialised();
        return isPlugin ? IntentManager.runPluginPackage(packageName) : IntentManager.runIndependentPackage(packageName);
    }


    /**
     * @param pmOperation
     */
    private static void checkPMOperationIsValid(PMOperation pmOperation) {
        if (pmOperation == null) {
            throw new NullPointerException("PMOperation can not be null!");
        }
        switch (pmOperation.mOperationType) {
            case PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE:
                if (TextUtils.isEmpty(pmOperation.mPackageArchiveLocalPath)) {
                    throw new StringFieldEmptyException("PMOperation mPackageArchiveLocalPath can not be empty!");
                }
                break;
            case PMOperation.TYPE_PM_OPERATION_REMOVE_PACKAGE:
                if (TextUtils.isEmpty(pmOperation.mPackageName)) {
                    throw new StringFieldEmptyException("PMOperation mPackageName can not be empty!");
                }
                break;
            default:
                throw new UnsupportedOperationException("PMOperation mOperationType only support TYPE_PM_OPERATION_INSTALL_PACKAGE or TYPE_PM_OPERATION_REMOVE_PACKAGE!");
        }
    }

    /**
     *
     */
    public static void executeNextPMOperationIfHas() {
        if (sCurrentRunningPMOperation != null) {
            PM_OPERATION_QUEUE.remove(sCurrentRunningPMOperation);
            sCurrentRunningPMOperation = null;
        }
        if (!PM_OPERATION_QUEUE.isEmpty()) {
            sCurrentRunningPMOperation = PM_OPERATION_QUEUE.get(0);
            sCurrentRunningPMOperation.run();
        }
    }


    /**
     *
     */
    private static void checkIsInitialised() {
        synchronized (INITIALISE_LOCK) {
            if (!sIsInitialised) {
                throw new IllegalStateException("You must invoke initCore method first of all.");
            }
        }
    }
}
