package com.tagmarshal.golf.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.LogInModel;
import com.tagmarshal.golf.view.TMButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class DialogAdminEnterCreds extends Dialog {

    @BindView(R.id.btn_login)
    TMButton mLogin;

    @BindView(R.id.username)
    EditText mUserName;

    @BindView(R.id.password)
    EditText mPassword;

    private OnAdminEnterCredsListener onCredsCheckListener;

    Unbinder unbinder;


    public DialogAdminEnterCreds(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_admin_enter_creds);
        Window window = getWindow();
        window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        unbinder = ButterKnife.bind(this);
    }

    public void setOnCredsCheckListener(OnAdminEnterCredsListener onCredsCheckListener) {
        this.onCredsCheckListener = onCredsCheckListener;
    }

    @Override
    public void onDetachedFromWindow() {
        onCredsCheckListener = null;
        unbinder.unbind();
        unbinder = null;
        super.onDetachedFromWindow();
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.btn_login)
    void onLoginClick() {
        LogInModel logInModel = new LogInModel(
            mUserName.getText().toString(),
            mPassword.getText().toString()
        );

        GolfAPI.getGolfApi().adminLogin(logInModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Response<ResponseBody> response) {
                        if(onCredsCheckListener != null) {
                            if(response.isSuccessful()) {
                                onCredsCheckListener.onSuccess();
                            } else {
                                onCredsCheckListener.onFailure(GolfApplication.context.getString(R.string.wrong_credentials));
                            }
                            dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss();
                    }
                });
    }

    public interface OnAdminEnterCredsListener {
        void onSuccess();

        void onFailure(String message);
    }
}
