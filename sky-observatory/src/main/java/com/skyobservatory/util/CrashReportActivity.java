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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.skyobservatory.renderer.R;

import java.io.File;
import java.io.FileInputStream;

public class CrashReportActivity extends AppCompatActivity {

    private static final String TAG = "CrashReportActivity";
    private String mReportText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_report);
        mReportText = readAndDeleteReportFile();
        buildUi();
    }

    private void buildUi() {
        TextView reportView = findViewById(R.id.report_text);
        reportView.setText(mReportText.isEmpty() ? "No crash report found." : mReportText);

        final String reportSnapshot = mReportText;

        Button btnCopyIssue = findViewById(R.id.btn_copy_issue);
        btnCopyIssue.setOnClickListener(v -> {
            copyToClipboard(reportSnapshot);
            openGithubIssue();
            finish();
        });

        Button btnCopy = findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(v -> {
            copyToClipboard(reportSnapshot);
            Toast.makeText(CrashReportActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            finish();
        });

        Button btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
    }

    private String readAndDeleteReportFile() {
        String path = null;
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra(CrashHandler.REPORT_FILE_KEY);
        }
        if (path == null) return "";
        File file = new File(path);
        if (!file.exists()) return "";
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String content = new String(data, "UTF-8");
            file.delete();
            return content;
        } catch (Exception e) {
            Log.e(TAG, "Failed to read crash report", e);
            file.delete();
            return "";
        }
    }

    private void copyToClipboard(String text) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("Crash Report", text));
            }
        } catch (Throwable ignored) {}
    }

    private void openGithubIssue() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/skyobservatory/astronomy-sdk/issues/new"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Throwable ignored) {}
    }
}
