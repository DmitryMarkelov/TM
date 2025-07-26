// DebugConsoleActivity.java
package com.tagmarshal.golf.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.util.CartControlHelper;

public class DebugConsoleActivity extends Activity {

    private TextView logTextView;
    private static StringBuilder logBuilder = new StringBuilder();
    private static DebugConsoleActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_console);

        logTextView = findViewById(R.id.log_text_view);
        Button closeButton = findViewById(R.id.close_button);
        Button upgradeButton = findViewById(R.id.upgrade_button);

        // Set initial log text
        logTextView.setText(logBuilder.toString());

        closeButton.setOnClickListener(v -> finish());

        upgradeButton.setOnClickListener(v -> {
            // Handle UPGRADE request
            requestUpgrade();
        });

        instance = this;
    }

    public static DebugConsoleActivity getInstance() {
        return instance;
    }

    public void writeToLog(String message) {
        runOnUiThread(() -> {
            logBuilder.append(message).append("\n");
            logTextView.setText(logBuilder.toString());

            // Check if the log content height exceeds the TextView height
            logTextView.post(() -> {
                int logContentHeight = logTextView.getLayout().getLineTop(logTextView.getLineCount());
                int logTextViewHeight = logTextView.getHeight();
                if (logContentHeight > logTextViewHeight) {
                    logBuilder.setLength(0); // Clear the log
                    logTextView.setText(logBuilder.toString());
                }
            });
        });
    }

    private void requestUpgrade() {
        CartControlHelper.hasFirmwareUpdateBeenRequested = true;
    }
}