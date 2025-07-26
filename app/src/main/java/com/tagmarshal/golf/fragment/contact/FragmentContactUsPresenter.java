package com.tagmarshal.golf.fragment.contact;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentContactUsPresenter implements FragmentContactUsContract.Presenter {

    private final FragmentContactUsContract.View view;
    private final FragmentContactUsContract.Model model;
    private final CompositeDisposable disposable;


    public FragmentContactUsPresenter(FragmentContactUsContract.View view) {
        this.view = view;
        this.model = new FragmentContactUsModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void getContactNumber() {
        view.shoWaitDialog(true);

        disposable.add(model.getContactNumber()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response.body().string());
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            view.onGetContactNumber(jsonObject.getString("phone"));
                        } catch (Exception e) {
                            view.showRequestFailure(e.toString());
                        }

                        view.shoWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showRequestFailure(e.toString());
                        view.shoWaitDialog(false);
                    }
                }));
    }

    @Override
    public void sendMessage(Map<String, String> body) {
        view.shoWaitDialog(true);

        disposable.add(model.sendMessage(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        switch (response.code()) {
                            case 201:
                                try {
                                    view.onMessageSent();
                                } catch (Exception e) {
                                    view.showRequestFailure(GolfApplication.context.getString(R.string.message_sending_fail));
                                }
                                break;
                            default:
                                view.showRequestFailure(GolfApplication.context.getString(R.string.message_sending_fail));
                                break;
                        }

                        view.shoWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showRequestFailure(GolfApplication.context.getString(R.string.message_sending_fail));

                        view.shoWaitDialog(false);
                    }
                }));
    }

    @Override
    public void onDestroy() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
