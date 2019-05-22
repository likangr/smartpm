package com.likangr.smartpm.lib.util;

import android.app.Application;

public class ApplicationHolder {

    /**
     *
     */
    private static Application APPLICATION;

    /**
     * @param application
     */
    public static void init(Application application) {
        APPLICATION = application;
    }

    /**
     * @return
     */
    public static Application getApplication() {
        return APPLICATION;
    }
}
