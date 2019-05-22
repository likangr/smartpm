package com.likangr.smartpm.lib.core.guid;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.likangr.smartpm.lib.R;
import com.likangr.smartpm.lib.core.SmartPM;
import com.likangr.smartpm.lib.util.AccessibilityUtil;
import com.likangr.smartpm.lib.util.IntentManager;
import com.likangr.smartpm.lib.util.PackageUtil;

import java.util.HashMap;

/**
 * @author likangren
 */
public class UserActionBridgeActivity extends AppCompatActivity {

    private static final String TAG = "UserActionBridgeActivity";

    public static final int USER_ACTION_CODE_ENABLE_ACCESSIBILITY_SERVICE = 1;
    public static final int USER_ACTION_CODE_ENABLE_INSTALL_UNKNOWN_SOURCES = 2;

    public static final String INTENT_EXTRA_KEY_USER_ACTION_CODE = "user_action_code";
    public static final String INTENT_EXTRA_KEY_USER_ACTION_DONE_CALLBACK_ID = "user_action_done_callback_id";

    private static HashMap<Integer, OnUserActionDoneCallback> sOnUserActionDoneCallbacks;
    private int mUserActionDoneCallbackId;
    private int mUserActionCode;
    private boolean mIsFirstOnResume = true;

    private Runnable mCheckUserHasDoneRunnable = new Runnable() {
        @Override
        public void run() {
            if (checkUserDoneIsWeExcepted()) {
                startActivity(new Intent(UserActionBridgeActivity.this, UserActionBridgeActivity.class));
                //for compat:
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.moveTaskToFront(UserActionBridgeActivity.this.getTaskId(), 0);
            } else {
                sendCheckUserHasDoneSignal();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }

        Intent intent = getIntent();
        mUserActionDoneCallbackId = intent.getIntExtra(INTENT_EXTRA_KEY_USER_ACTION_DONE_CALLBACK_ID, 0);
        mUserActionCode = intent.getIntExtra(INTENT_EXTRA_KEY_USER_ACTION_CODE, 0);

        switch (mUserActionCode) {
            case USER_ACTION_CODE_ENABLE_ACCESSIBILITY_SERVICE:
                if (!IntentManager.gotoAccessibilitySettings(this)) {
                    invokeCallback(false);
                } else {
                    sendCheckUserHasDoneSignal();
                    UserActionGuideToast.show(this, "需要先开启" + getString(R.string.lib_smartpm_service_label),
                            "操作指南：\n找到并开启" + getString(R.string.lib_smartpm_service_label), Toast.LENGTH_LONG);
                }
                break;
            case USER_ACTION_CODE_ENABLE_INSTALL_UNKNOWN_SOURCES:
                sendCheckUserHasDoneSignal();
                IntentManager.gotoInstallPermissionSettingActivity(this);
                UserActionGuideToast.show(this, "需要先允许" + getString(R.string.install_unknown_sources),
                        "操作指南：\n找到并开启" + getString(R.string.allow_sources), Toast.LENGTH_LONG);
                break;
            default:
                break;
        }
    }

    private void sendCheckUserHasDoneSignal() {
        SmartPM.getHandler().postDelayed(mCheckUserHasDoneRunnable, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsFirstOnResume) {
            mIsFirstOnResume = false;
        } else {
            UserActionGuideToast.dismiss();
            SmartPM.getHandler().removeCallbacks(mCheckUserHasDoneRunnable);
            invokeCallback(checkUserDoneIsWeExcepted());
        }

    }

    /**
     * @return
     */
    private boolean checkUserDoneIsWeExcepted() {
        boolean userDoneIsWeExcepted = false;
        switch (mUserActionCode) {
            case USER_ACTION_CODE_ENABLE_ACCESSIBILITY_SERVICE:
                userDoneIsWeExcepted = AccessibilityUtil.checkIsHasAccessibility();
                break;
            case USER_ACTION_CODE_ENABLE_INSTALL_UNKNOWN_SOURCES:
                userDoneIsWeExcepted = PackageUtil.canRequestPackageInstalls();
                break;
            default:
                break;
        }
        return userDoneIsWeExcepted;
    }


    /**
     * @param onUserActionDoneCallback
     */
    public static void setOnUserActionDoneCallback(OnUserActionDoneCallback
                                                           onUserActionDoneCallback) {
        if (sOnUserActionDoneCallbacks == null) {
            sOnUserActionDoneCallbacks = new HashMap<>(5);
        }
        sOnUserActionDoneCallbacks.put(onUserActionDoneCallback.hashCode(), onUserActionDoneCallback);
    }

    /**
     * @param userDoneIsWeExcepted
     */
    private void invokeCallback(boolean userDoneIsWeExcepted) {
        finish();
        try {
            OnUserActionDoneCallback onUserDoneCallback = sOnUserActionDoneCallbacks.get(mUserActionDoneCallbackId);
            if (userDoneIsWeExcepted) {
                onUserDoneCallback.onUserActionDoneIsWeExcepted();
            } else {
                onUserDoneCallback.onUserActionDoneIsNotWeExcepted();
            }
        } finally {
            sOnUserActionDoneCallbacks.remove(mUserActionDoneCallbackId);
        }
    }

    /**
     *
     */
    public interface OnUserActionDoneCallback {

        /**
         *
         */
        void onUserActionDoneIsWeExcepted();

        /**
         *
         */
        void onUserActionDoneIsNotWeExcepted();
    }

}
