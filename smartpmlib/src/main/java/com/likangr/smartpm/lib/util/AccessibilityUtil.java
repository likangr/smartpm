package com.likangr.smartpm.lib.util;

import android.provider.Settings;
import android.text.TextUtils;

import com.likangr.smartpm.lib.core.SmartPMService;

public class AccessibilityUtil {

    private static final String TAG = "AccessibilityUtil";

    /**
     * @return
     */
    public static boolean checkIsHasAccessibility() {
        int accessibilityEnabled = 0;
        final String service = ApplicationHolder.getApplication().getPackageName() + "/" + SmartPMService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    ApplicationHolder.getApplication().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Logger.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled != 1) {
            return false;
        }

        String settingValue = Settings.Secure.getString(
                ApplicationHolder.getApplication().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            mStringColonSplitter.setString(settingValue);
            while (mStringColonSplitter.hasNext()) {
                String accessibilityService = mStringColonSplitter.next();
                if (accessibilityService.equalsIgnoreCase(service)) {
                    return true;
                }
            }
        }

        return false;
    }
}
