package com.tagmarshal.golf.fragment.emergency;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentEmergencyPresenter implements FragmentEmergencyContract.Presenter {

    private final FragmentEmergencyContract.View view;
    private final FragmentEmergencyContract.Model model;
    private final CompositeDisposable disposable;


    public FragmentEmergencyPresenter(FragmentEmergencyContract.View view) {
        this.view = view;
        this.model = new FragmentEmergencyModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void sendMessage(Map<String, String> body) {
        view.showWaitDialog(true);

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
                                    view.onRequestFailure(GolfApplication.context.getString(R.string.message_sending_fail));
                                }
                                break;
                            default:
                                view.onRequestFailure(GolfApplication.context.getString(R.string.message_sending_fail));
                                break;
                        }

                        view.showWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onRequestFailure(GolfApplication.context.getString(R.string.message_sending_fail));
                        view.showWaitDialog(false);
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
