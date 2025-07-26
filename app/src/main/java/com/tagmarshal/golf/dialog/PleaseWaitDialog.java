package com.tagmarshal.golf.dialog;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

import com.tagmarshal.golf.R;

public class PleaseWaitDialog {

    AlertDialog dialog;

    public PleaseWaitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(R.layout.alert_dialog_loading);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
