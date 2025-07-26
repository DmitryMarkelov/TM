package com.tagmarshal.golf.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.view.TMButton;
import com.tagmarshal.golf.view.TMCheckBox;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GolfAlertDialog {

    android.app.AlertDialog.Builder builder;
    android.app.AlertDialog alertDialog;

    @BindView(R.id.title)
    TMTextView mTitle;

    @BindView(R.id.message)
    TMTextView mMessage;

    @BindView(R.id.icon)
    ImageView mIcon;

    @BindView(R.id.ok)
    TMButton mOk;

    @BindView(R.id.what)
    TMButton mWhat;

    @BindView(R.id.checkbox)
    TMCheckBox checkBox;


    GolfCallback.GolfDialogListener dialogListener;
    private boolean isShowing;


    public GolfAlertDialog(Context context) {
        super();
        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null);
        builder = new android.app.AlertDialog.Builder(context);
        builder.setView(view);
        ButterKnife.bind(this, view);
        checkBox.setVisibility(View.GONE);

        builder.setCancelable(false);
        mOk.setClickable(true);
        mWhat.setClickable(true);
        alertDialog = builder.create();
    }

    public void show() {
        isShowing = true;
        alertDialog.show();
    }


    public void dissmiss() {
        isShowing = false;
        checkBox.setChecked(false);
        alertDialog.dismiss();
    }

    public boolean isShowing() {
        return isShowing;
    }

    public GolfAlertDialog setCancelable(boolean cancelable) {
        alertDialog.setCancelable(cancelable);
        return this;
    }

    public GolfAlertDialog setTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    public GolfAlertDialog setAllCaps(boolean allCaps) {
        mTitle.setAllCaps(allCaps);
        return this;
    }

    public GolfAlertDialog setMessage(String message) {
        mMessage.setText(message);
        return this;
    }

    public GolfAlertDialog setIcon(int icon) {
        mIcon.setImageResource(icon);
        return this;
    }

    public GolfAlertDialog setOkText(String text) {
        mOk.setText(text);
        return this;
    }

    public GolfAlertDialog setBottomBtnText(String text) {
        mWhat.setText(text);
        return this;
    }

    public GolfAlertDialog setCheckBoxVisibility(boolean visible) {
        if (visible) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        return this;
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public void setOnDialogListener(GolfCallback.GolfDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public GolfAlertDialog hideBottomButton() {
        mWhat.setVisibility(View.GONE);
        return this;
    }

    @OnClick(R.id.ok)
    void onOkClick() {
        if (dialogListener != null) {
            dialogListener.onOkClick();
        }

        alertDialog.dismiss();
    }

    @OnClick(R.id.what)
    void onWhatClick() {
        if (dialogListener != null) {
            dialogListener.onBottomBtnClick();
        }
    }
}
