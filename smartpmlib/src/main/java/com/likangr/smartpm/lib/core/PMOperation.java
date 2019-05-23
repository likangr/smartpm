package com.likangr.smartpm.lib.core;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.likangr.smartpm.lib.SmartPM;
import com.likangr.smartpm.lib.core.exception.PMOperationInvalidException;
import com.likangr.smartpm.lib.core.guid.UserActionBridgeActivity;
import com.likangr.smartpm.lib.util.AccessibilityUtil;
import com.likangr.smartpm.lib.util.ApplicationHolder;
import com.likangr.smartpm.lib.util.IntentManager;
import com.likangr.smartpm.lib.util.PackageUtil;

/**
 * @author likangren
 */
public class PMOperation implements Runnable {

    public static final int TYPE_PM_OPERATION_REMOVE_PACKAGE = 1;
    public static final int TYPE_PM_OPERATION_INSTALL_PACKAGE = 2;
    private static final String TAG = "PMOperation";
    public String packageName;
    public String packageArchiveLocalPath;
    public Object extraData;
    public int operationType;
    public long lastUpdateTime;
    public boolean isUpdate;

    public int failReason;
    public boolean isNeedUserAssist;

    private PMOperationCallback pmOperationCallback;

    private PMOperation() {
    }

    /**
     * @param packageArchiveLocalPath
     * @param extraData
     * @param pmOperationCallback
     * @return
     */
    public static PMOperation createInstallPackageOperation(String packageArchiveLocalPath,
                                                            Object extraData,
                                                            PMOperationCallback pmOperationCallback) {
        PMOperation pmOperation = new PMOperation();
        pmOperation.operationType = PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE;
        pmOperation.packageArchiveLocalPath = packageArchiveLocalPath;
        pmOperation.extraData = extraData;
        pmOperation.pmOperationCallback = pmOperationCallback;
        return pmOperation;
    }

    /**
     * @param packageName
     * @param extraData
     * @param pmOperationCallback
     * @return
     */
    public static PMOperation createRemovePackageOperation(String packageName,
                                                           Object extraData,
                                                           PMOperationCallback pmOperationCallback) {
        PMOperation pmOperation = new PMOperation();
        pmOperation.operationType = PMOperation.TYPE_PM_OPERATION_REMOVE_PACKAGE;
        pmOperation.packageName = packageName;
        pmOperation.extraData = extraData;
        pmOperation.pmOperationCallback = pmOperationCallback;
        return pmOperation;
    }

    /**
     * @param packageArchiveLocalPath
     * @param pmOperationCallback
     * @return
     */
    public static PMOperation createInstallPackageOperation(String packageArchiveLocalPath,
                                                            PMOperationCallback pmOperationCallback) {
        return createInstallPackageOperation(packageArchiveLocalPath, null, pmOperationCallback);
    }

    /**
     * @param packageName
     * @param pmOperationCallback
     * @return
     */
    public static PMOperation createRemovePackageOperation(String packageName,
                                                           PMOperationCallback pmOperationCallback) {
        return createRemovePackageOperation(packageName, null, pmOperationCallback);
    }

    /**
     *
     */
    private void callOnOperationStart() {
        if (pmOperationCallback != null) {
            pmOperationCallback.onPackageOperationStart(this);
        }
    }

    /**
     *
     */
    private void callOnOperationIsNeedUserAssist(boolean isNeedUserAssist) {
        this.isNeedUserAssist = isNeedUserAssist;
        if (pmOperationCallback != null) {
            pmOperationCallback.onPackageOperationIsNeedUserAssist(this);
        }
    }

    /**
     *
     */
    private void callOnOperationSuccess() {
        if (pmOperationCallback != null) {
            pmOperationCallback.onPackageOperationSuccess(this);
        }
        SmartPM.executeNextPMOperationIfHas();
    }


    /**
     *
     */
    private void callOnOperationFail(int failReason) {
        this.failReason = failReason;
        if (pmOperationCallback != null) {
            pmOperationCallback.onPackageOperationFail(this);
        }
        SmartPM.executeNextPMOperationIfHas();
    }

    public void checkIsValid() throws PMOperationInvalidException {
        switch (operationType) {
            case PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE:
                if (TextUtils.isEmpty(packageArchiveLocalPath)) {
                    throw new PMOperationInvalidException("packageArchiveLocalPath can not be empty!");
                }
                Application application = ApplicationHolder.getApplication();

                PackageManager pm = application.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(packageArchiveLocalPath, PackageManager.GET_ACTIVITIES);
                if (info == null || info.applicationInfo == null) {
                    throw new PMOperationInvalidException("The file " + packageArchiveLocalPath + " is not a valid apk!");
                }

                packageName = info.applicationInfo.packageName;

                PackageInfo installedPackageInfo = PackageUtil.getInstalledPackageInfo(packageName);
                if (installedPackageInfo != null) {
                    isUpdate = true;
                    lastUpdateTime = installedPackageInfo.lastUpdateTime;
                }
                break;
            case PMOperation.TYPE_PM_OPERATION_REMOVE_PACKAGE:
                if (TextUtils.isEmpty(packageName)) {
                    throw new PMOperationInvalidException("packageName can not be empty!");
                }
                if (!PackageUtil.packageIsInstalled(packageName)) {
                    throw new PMOperationInvalidException(packageName + " is not installed!");
                }
                break;
            default:
                throw new PMOperationInvalidException("operationType only support TYPE_PM_OPERATION_INSTALL_PACKAGE or TYPE_PM_OPERATION_REMOVE_PACKAGE!");
        }
    }

    @Override
    public void run() {
        if (operationType == TYPE_PM_OPERATION_INSTALL_PACKAGE &&
                !PackageUtil.canRequestPackageInstalls()) {
            IntentManager.gotoUserActionBridgeActivity(
                    UserActionBridgeActivity.USER_ACTION_CODE_ENABLE_INSTALL_UNKNOWN_SOURCES,
                    new UserActionBridgeActivity.OnUserActionDoneCallback() {
                        @Override
                        public void onUserActionDoneIsWeExcepted() {
                            performOperation();
                        }

                        @Override
                        public void onUserActionDoneIsNotWeExcepted() {
                            callOnOperationFail(SmartPM.FAIL_REASON_ENABLE_INSTALL_UNKNOWN_SOURCES_USER_REJECT);
                        }
                    });
        } else {
            performOperation();
        }
    }

    /**
     *
     */
    private void performOperation() {

        if (AccessibilityUtil.checkIsHasAccessibility()) {
            callOnOperationIsNeedUserAssist(false);
            performRealOperation();
        } else {
            IntentManager.gotoUserActionBridgeActivity(UserActionBridgeActivity.USER_ACTION_CODE_ENABLE_ACCESSIBILITY_SERVICE,
                    new UserActionBridgeActivity.OnUserActionDoneCallback() {
                        @Override
                        public void onUserActionDoneIsWeExcepted() {
                            callOnOperationIsNeedUserAssist(false);
                            performRealOperation();
                        }

                        @Override
                        public void onUserActionDoneIsNotWeExcepted() {
                            //user manually.
                            callOnOperationIsNeedUserAssist(true);
                            performRealOperation();
                        }
                    });
        }
    }

    /**
     *
     */
    private void performRealOperation() {
        callOnOperationStart();
        Application application = ApplicationHolder.getApplication();
        switch (operationType) {
            case TYPE_PM_OPERATION_INSTALL_PACKAGE:
                IntentManager.gotoInstallPackageActivity(application, packageArchiveLocalPath);
                break;
            case TYPE_PM_OPERATION_REMOVE_PACKAGE:
                IntentManager.gotoRemovePackageActivity(application, packageName);
                break;
            default:
                break;
        }
        listenPackageOperationAction();
    }

    /**
     *
     */
    private void listenPackageOperationAction() {
        final Application application = ApplicationHolder.getApplication();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                application.unregisterActivityLifecycleCallbacks(this);

                switch (operationType) {
                    case PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE:
                        PackageInfo installedPackageInfo = PackageUtil.getInstalledPackageInfo(packageName);
                        if (installedPackageInfo != null && lastUpdateTime != installedPackageInfo.lastUpdateTime) {
                            callOnOperationSuccess();
                        } else {
                            callOnOperationFail(SmartPM.FAIL_REASON_UNKNOWN);
                        }
                        break;
                    case PMOperation.TYPE_PM_OPERATION_REMOVE_PACKAGE:
                        if (!PackageUtil.packageIsInstalled(packageName)) {
                            callOnOperationSuccess();
                        } else {
                            callOnOperationFail(SmartPM.FAIL_REASON_UNKNOWN);
                        }
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


    @Override
    public String toString() {
        return "PMOperation{" +
                "packageName='" + packageName + '\'' +
                ", packageArchiveLocalPath='" + packageArchiveLocalPath + '\'' +
                ", extraData=" + extraData +
                ", operationType=" + operationType +
                ", lastUpdateTime=" + lastUpdateTime +
                ", isUpdate=" + isUpdate +
                ", pmOperationCallback=" + pmOperationCallback +
                '}';
    }

    /**
     *
     */
    public interface PMOperationCallback {
        /**
         * @param pmOperation
         */
        void onPackageOperationStart(PMOperation pmOperation);

        /**
         * @param pmOperation
         */
        void onPackageOperationIsNeedUserAssist(PMOperation pmOperation);

        /**
         * @param pmOperation
         */
        void onPackageOperationSuccess(PMOperation pmOperation);

        /**
         * @param pmOperation
         */
        void onPackageOperationFail(PMOperation pmOperation);
    }
}