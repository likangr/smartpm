package com.likangr.smartpm.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.likangr.smartpm.lib.SmartPM;
import com.likangr.smartpm.lib.core.PMOperation;
import com.likangr.smartpm.lib.core.exception.PMOperationInvalidException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author likangren
 */
public class MainActivity extends AppCompatActivity implements PMOperation.PMOperationCallback {

    private static final String TAG = "DemoActivity";
    private ProgressBar mPbWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPbWait = findViewById(R.id.pb_wait);
        try {
            copyApk(true);
            copyApk(false);
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtil.showShort(this, getString(R.string.notify_copy_failed));
            finish();
        }
    }


    public void installIndependentApp(View view) {
        installApp(true);
    }

    public void removeIndependentApp(View view) {
        removeApp(true);
    }

    public void runIndependentApp(View view) {
        runApp(true);
    }


    public void installPluginApp(View view) {
        installApp(false);
    }

    public void removePluginApp(View view) {
        removeApp(false);
    }

    public void runPluginApp(View view) {
        runApp(false);
    }


    public void installApp(boolean isIndependent) {
        try {
            SmartPM.postPMOperation(
                    PMOperation.createInstallPackageOperation(
                            getFilesDir()
                                    + File.separator
                                    + getTestAppFileName(isIndependent),
                            this));
        } catch (PMOperationInvalidException e) {
            e.printStackTrace();
            ToastUtil.showShort(this, getString(R.string.install_failed_invalid_operation));
        }
    }


    public void removeApp(boolean isIndependent) {
        try {
            SmartPM.postPMOperation(
                    PMOperation.createRemovePackageOperation(
                            getTestAppPackageName(isIndependent),
                            this));
        } catch (PMOperationInvalidException e) {
            e.printStackTrace();
            ToastUtil.showShort(this, getString(R.string.remove_failed_invalid_operation));
        }
    }

    public void runApp(boolean isIndependent) {
        if (!SmartPM.runPackage(getTestAppPackageName(isIndependent), !isIndependent)) {
            ToastUtil.showShort(this, getString(R.string.run_failed));
        }
    }

    @Override
    public void onPackageOperationStart(PMOperation pmOperation) {
        setPbVisible(true);
    }

    @Override
    public void onPackageOperationIsNeedUserAssist(PMOperation pmOperation) {
        if (pmOperation.isNeedUserAssist) {
            ToastUtil.showShort(this, getString(R.string.need_user_assist));
        }
    }

    @Override
    public void onPackageOperationSuccess(PMOperation pmOperation) {
        setPbVisible(false);
        ToastUtil.showShort(this,
                pmOperation.operationType == PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE ?
                        getString(R.string.install_succeed) :
                        getString(R.string.remove_succeed));
    }

    @Override
    public void onPackageOperationFail(PMOperation pmOperation) {
        setPbVisible(false);
        int failReason = pmOperation.failReason;
        if (failReason == SmartPM.FAIL_REASON_ENABLE_INSTALL_UNKNOWN_SOURCES_USER_REJECT) {
            ToastUtil.showShort(this,
                    pmOperation.operationType == PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE ?
                            getString(R.string.install_failed_reject_unknown_sources) :
                            getString(R.string.remove_failed_reject_unknown_sources));
        } else if (failReason == SmartPM.FAIL_REASON_UNKNOWN) {
            ToastUtil.showShort(this,
                    pmOperation.operationType == PMOperation.TYPE_PM_OPERATION_INSTALL_PACKAGE ?
                            getString(R.string.install_failed_unknown_reason) :
                            getString(R.string.remove_failed_unknown_reason));
        }

    }

    private String getTestAppPackageName(boolean isIndependent) {
        return isIndependent ? "com.likangr.smartpm.test" : "com.likangr.smartpm.test.plugin";
    }

    private String getTestAppFileName(boolean isIndependent) {
        return (isIndependent ? "test-independent-debug.apk" : "test-plugin-debug.apk");
    }

    private void setPbVisible(boolean visible) {
        mPbWait.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void copyApk(boolean isIndependent) throws IOException {

        String fileName = getTestAppFileName(isIndependent);
        InputStream in = getResources().getAssets().open(fileName);
        FileOutputStream out = new FileOutputStream(getFilesDir() + File.separator + fileName);

        byte[] buffer = new byte[1024 * 128];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        out.close();
        in.close();
    }
}
