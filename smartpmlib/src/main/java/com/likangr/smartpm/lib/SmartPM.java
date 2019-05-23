package com.likangr.smartpm.lib;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.likangr.smartpm.lib.core.PMOperation;
import com.likangr.smartpm.lib.core.exception.PMOperationInvalidException;
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
    private static final Object PM_OPERATIONS_QUEUE_LOCK = new Object();
    private static Handler sHandler;
    private static boolean sIsInitialised = false;

    public static final int FAIL_REASON_ENABLE_INSTALL_UNKNOWN_SOURCES_USER_REJECT = 1;
    public static final int FAIL_REASON_UNKNOWN = 2;


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
    public static void postPMOperation(PMOperation pmOperation) throws PMOperationInvalidException {
        checkIsInitialised();
        synchronized (PM_OPERATIONS_QUEUE_LOCK) {
            pmOperation.checkIsValid();
            PM_OPERATION_QUEUE.add(pmOperation);
            if (sCurrentRunningPMOperation == null) {
                executeNextPMOperationIfHas();
            }
        }
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
