/*
 * Copyright 2026 Phuc An <pan2512811@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skyobservatory.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashHandler {

    private static final String TAG = "CrashHandler";
    static final String REPORT_FILE_KEY = "crash_report_path";
    static final String REPORT_FILE_NAME = "crash_report_pending.txt";

    public static void init(final Context app) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                String report = buildReport(app, throwable);
                Log.e(TAG, report);

                writeReportToCache(app, report);
                launchReportActivity(app);
                Thread.sleep(3000);
            } catch (Throwable secondary) {
                Log.e(TAG, "CrashHandler failed", secondary);
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
    }

    public static void handleGlThreadCrash(Context app, Throwable throwable) {
        try {
            String report = buildReport(app, throwable);
            Log.e(TAG, report);
            writeReportToCache(app, report);
            launchReportActivity(app);
            Thread.sleep(3000);
        } catch (Throwable secondary) {
            Log.e(TAG, "GL crash handler failed", secondary);
        }
    }

    private static String buildReport(Context app, Throwable throwable) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z", Locale.US)
                .format(new Date());

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString().trim();

        String pkgName = app.getPackageName();
        String versionName = "unknown";
        long versionCode = 0;

        try {
            PackageInfo info = app.getPackageManager().getPackageInfo(pkgName, 0);
            versionName = info.versionName;
            versionCode = Build.VERSION.SDK_INT >= 28
                    ? info.getLongVersionCode()
                    : (long) info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {}

        StringBuilder sb = new StringBuilder();
        sb.append("## Report Info\n\n");
        sb.append("**User Action**: `app crash`\n");
        sb.append("**Sender**: `CrashHandler`\n");
        sb.append("**Report Timestamp**: `").append(timestamp).append("`\n");
        sb.append("##\n\n");
        sb.append("## App Crash\n\n```\n").append(stackTrace).append("\n```\n\n");
        sb.append("## App Info\n\n");
        sb.append("**APP_NAME**: `Sky Observatory`\n");
        sb.append("**PACKAGE_NAME**: `").append(pkgName).append("`\n");
        sb.append("**VERSION_NAME**: `").append(versionName).append("`\n");
        sb.append("**VERSION_CODE**: `").append(versionCode).append("`\n");
        sb.append("**TARGET_SDK**: `").append(Build.VERSION.SDK_INT).append("`\n");
        sb.append("##\n\n");
        sb.append("## Device Info\n\n");
        sb.append("**OS_VERSION**: `").append(System.getProperty("os.version", "-")).append("`\n");
        sb.append("**SDK_INT**: `").append(Build.VERSION.SDK_INT).append("`\n");
        sb.append("**RELEASE**: `").append(Build.VERSION.RELEASE).append("`\n");
        sb.append("**MODEL**: `").append(Build.MODEL).append("`\n");
        sb.append("**MANUFACTURER**: `").append(Build.MANUFACTURER).append("`\n");
        sb.append("**HARDWARE**: `").append(Build.HARDWARE).append("`\n");
        sb.append("##\n");
        return sb.toString();
    }

    private static boolean isCrashFromReportActivity(Throwable throwable) {
        String reportActivityName = CrashReportActivity.class.getName();
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            for (StackTraceElement frame : t.getStackTrace()) {
                if (frame.getClassName().startsWith(reportActivityName)) {
                    return true;
                }
                if (throwable instanceof android.view.WindowManager.BadTokenException
                        && frame.getClassName().contains("ActivityThread")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void writeReportToCache(Context app, String report) throws Exception {
        File file = new File(app.getCacheDir(), REPORT_FILE_NAME);
        if (file.exists()) file.delete();
        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write(report.getBytes("UTF-8"));
        fos.close();
    }

    private static void launchReportActivity(Context app) {
        Intent intent = new Intent(app, CrashReportActivity.class);
        intent.putExtra(REPORT_FILE_KEY,
                new File(app.getCacheDir(), REPORT_FILE_NAME).getAbsolutePath());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        app.startActivity(intent);
    }
}
