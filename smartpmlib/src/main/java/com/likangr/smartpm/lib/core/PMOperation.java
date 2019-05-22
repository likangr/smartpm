package com.likangr.smartpm.lib.core;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.likangr.smartpm.lib.core.guid.UserActionBridgeActivity;
import com.likangr.smartpm.lib.util.AccessibilityUtil;
import com.likangr.smartpm.lib.util.ApplicationHolder;
import com.likangr.smartpm.lib.util.IntentManager;
import com.likangr.smartpm.lib.util.PackageUtil;

/**
 * @author likangren
 */
public class PMOperation implements Runnable {

    private static final String TAG = "PMOperation";

    public static final int TYPE_PM_OPERATION_REMOVE_PACKAGE = 1;
    public static final int TYPE_PM_OPERATION_INSTALL_PACKAGE = 2;

    public String mPackageName;
    public String mPackageArchiveLocalPath;
    public Object mExtraData;
    public int mOperationType;
    public long mLastUpdateTime;
    public boolean mIsUpdate;
    private PMOperationCallback mPMOperationCallback;

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
        pmOperation.mOperationType = PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE;
        pmOperation.mPackageArchiveLocalPath = packageArchiveLocalPath;
        pmOperation.mExtraData = extraData;
        pmOperation.mPMOperationCallback = pmOperationCallback;
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
        pmOperation.mOperationType = PMOperation.TYPE_PM_OPERATION_REMOVE_PACKAGE;
        pmOperation.mPackageName = packageName;
        pmOperation.mExtraData = extraData;
        pmOperation.mPMOperationCallback = pmOperationCallback;
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
        if (mPMOperationCallback != null) {
            mPMOperationCallback.onPackageOperationStart(this);
        }
    }

    /**
     *
     */
    private void callOnOperationSuccess() {
        if (mPMOperationCallback != null) {
            mPMOperationCallback.onPackageOperationSuccess(this);
        }
        SmartPM.executeNextPMOperationIfHas();
    }

    /**
     *
     */
    private void callOnOperationFail() {
        if (mPMOperationCallback != null) {
            mPMOperationCallback.onPackageOperationFail(this);
        }
        SmartPM.executeNextPMOperationIfHas();
    }


    @Override
    public void run() {
        if (mOperationType == TYPE_PM_OPERATION_INSTALL_PACKAGE &&
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
                            callOnOperationFail();
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

        if (!checkOperationIsValid()) {
            return;
        }

        if (AccessibilityUtil.checkIsHasAccessibility()) {
            performRealOperation();
        } else {
            IntentManager.gotoUserActionBridgeActivity(UserActionBridgeActivity.USER_ACTION_CODE_ENABLE_ACCESSIBILITY_SERVICE,
                    new UserActionBridgeActivity.OnUserActionDoneCallback() {
                        @Override
                        public void onUserActionDoneIsWeExcepted() {
                            performRealOperation();
                        }

                        @Override
                        public void onUserActionDoneIsNotWeExcepted() {
                            //user manually.
                            performRealOperation();
                        }
                    });
        }
    }

    /**
     * @return
     */
    private boolean checkOperationIsValid() {
        boolean isValid;
        switch (mOperationType) {
            case TYPE_PM_OPERATION_INSTALL_PACKAGE:
                isValid = checkInstallPackageOperationIsValid();
                break;
            case TYPE_PM_OPERATION_REMOVE_PACKAGE:
                isValid = checkRemovePackageOperationIsValid();
                break;
            default:
                isValid = false;
                break;
        }
        return isValid;
    }

    /**
     *
     */
    private void performRealOperation() {
        callOnOperationStart();
        Application application = ApplicationHolder.getApplication();
        switch (mOperationType) {
            case TYPE_PM_OPERATION_INSTALL_PACKAGE:
                IntentManager.gotoInstallPackageActivity(application, mPackageArchiveLocalPath);
                break;
            case TYPE_PM_OPERATION_REMOVE_PACKAGE:
                IntentManager.gotoRemovePackageActivity(application, mPackageName);
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

                switch (mOperationType) {
                    case PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE:
                        PackageInfo installedPackageInfo = PackageUtil.getInstalledPackageInfo(mPackageName);
                        if (installedPackageInfo != null && mLastUpdateTime != installedPackageInfo.lastUpdateTime) {
                            callOnOperationSuccess();
                        } else {
                            callOnOperationFail();
                        }
                        break;
                    case PMOperation.TYPE_PM_OPERATION_REMOVE_PACKAGE:
                        if (!PackageUtil.packageIsInstalled(mPackageName)) {
                            callOnOperationSuccess();
                        } else {
                            callOnOperationFail();
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

    /**
     *
     */
    private boolean checkInstallPackageOperationIsValid() {
        Application application = ApplicationHolder.getApplication();

        PackageManager pm = application.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(mPackageArchiveLocalPath, PackageManager.GET_ACTIVITIES);
        if (info == null || info.applicationInfo == null) {
            callOnOperationFail();
            return false;
        }

        mPackageName = info.applicationInfo.packageName;

        PackageInfo installedPackageInfo = PackageUtil.getInstalledPackageInfo(mPackageName);
        if (installedPackageInfo != null) {
            mIsUpdate = true;
            mLastUpdateTime = installedPackageInfo.lastUpdateTime;
        }
        return true;
    }

    /**
     *
     */
    private boolean checkRemovePackageOperationIsValid() {

        if (!PackageUtil.packageIsInstalled(mPackageName)) {
            callOnOperationFail();
            return false;
        }
        return true;
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
        void onPackageOperationSuccess(PMOperation pmOperation);

        /**
         * @param pmOperation
         */
        void onPackageOperationFail(PMOperation pmOperation);
    }

    @Override
    public String toString() {
        return "PMOperation{" +
                "mPackageName='" + mPackageName + '\'' +
                ", mPackageArchiveLocalPath='" + mPackageArchiveLocalPath + '\'' +
                ", mExtraData=" + mExtraData +
                ", mOperationType=" + mOperationType +
                ", mLastUpdateTime=" + mLastUpdateTime +
                ", mIsUpdate=" + mIsUpdate +
                ", mPMOperationCallback=" + mPMOperationCallback +
                '}';
    }
}