package com.skyobservatory.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.developer.crashx.CrashActivity;
import com.developer.crashx.config.CrashConfig;
import com.skyobservatory.renderer.R;

public class SkyCrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sky_crash);

        CrashConfig config = CrashActivity.getConfigFromIntent(getIntent());
        String details = CrashActivity.getAllErrorDetailsFromIntent(this, getIntent());

        TextView reportView = findViewById(R.id.crash_details);
        reportView.setText(details);

        Button btnCopy = findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(v -> {
            copyToClipboard(details);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        Button btnRestart = findViewById(R.id.btn_restart);
        btnRestart.setOnClickListener(v ->
                CrashActivity.restartApplication(this, config));

        Button btnOpenIssue = findViewById(R.id.btn_open_issue);
        btnOpenIssue.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/AstroNexis/Sky-Observatory/issues"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("Crash Report", text));
        }
    }
}