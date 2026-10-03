package com.skyobservatory.util;

import android.app.Activity;

import com.developer.crashx.config.CrashConfig;

public final class CrashHandler {

    public static void init(Class<? extends Activity> mainActivity) {
        CrashConfig.Builder.create()
                .enabled(true)
                .backgroundMode(CrashConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .errorActivity(SkyCrashActivity.class)
                .restartActivity(mainActivity)
                .showErrorDetails(true)
                .trackActivities(true)
                .apply();
    }
}