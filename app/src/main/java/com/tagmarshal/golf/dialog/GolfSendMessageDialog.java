package com.tagmarshal.golf.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMButton;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class GolfSendMessageDialog {

    android.app.AlertDialog.Builder builder;
    android.app.AlertDialog alertDialog;

    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";

    @BindView(R.id.message)
    EditText mMessage;

    @BindView(R.id.send)
    TMButton mSendBtn;

    @BindView(R.id.cancel)
    TMButton mCancelBtn;


    public GolfSendMessageDialog(Context context) {
        super();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_send_message, null);
        builder = new android.app.AlertDialog.Builder(context);
        builder.setView(view);

        ButterKnife.bind(this, view);

        mSendBtn.setClickable(true);
        mCancelBtn.setClickable(true);
    }

    public void show() {
        alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.send)
    void onSendClick() {
        mSendBtn.showLoading(true);
        mCancelBtn.setActive(false);

        Map<String, String> body = new HashMap<>();
        body.put("message", mMessage.getText().toString());

        GolfAPI.getGolfCourseApi().sendMessage(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        switch (response.code()) {
                            case 201:
                                try {
                                    Toast.makeText(GolfApplication.context,
                                            GolfApplication.context.getString(R.string.message_sent),
                                            Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(GolfApplication.context,
                                            GolfApplication.context.getString(R.string.message_sending_fail),
                                            Toast.LENGTH_LONG).show();
                                }
                                break;
                            default:
                                Toast.makeText(GolfApplication.context,
                                        GolfApplication.context.getString(R.string.message_sending_fail),
                                        Toast.LENGTH_LONG).show();
                                break;
                        }

                        alertDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(GolfApplication.context,
                                GolfApplication.context.getString(R.string.message_sending_fail),
                                Toast.LENGTH_LONG).show();

                        alertDialog.dismiss();
                    }
                });
    }

    @OnClick(R.id.cancel)
    void onCancelClick() {
        alertDialog.dismiss();
    }
}
