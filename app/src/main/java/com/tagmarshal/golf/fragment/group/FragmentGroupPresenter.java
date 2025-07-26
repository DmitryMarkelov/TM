package com.tagmarshal.golf.fragment.group;

import android.annotation.SuppressLint;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentGroupPresenter implements FragmentGroupContract.Presenter {

    private FragmentGroupContract.Model model;
    private final FragmentGroupContract.View view;
    private final CompositeDisposable disposable;

    public FragmentGroupPresenter(FragmentGroupContract.View view) {
        this.view = view;
        this.model = new FragmentGroupModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public void sendSkipMessage() {
        disposable.add(model.sendSkipMessage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(__ ->view.showWaitDialog(true))
                .subscribe(
                        __ -> {
                            view.onSkipMessageSuccess();
                            view.showWaitDialog(false);
                        },
                        e -> view.showWaitDialog(false)

                )
        );
    }

    @SuppressLint("CheckResult")
    @Override
    public void confirmOrRemoveGroup(String id, String action) {
        view.showWaitDialog(true);

        disposable.add(model.confirmOrRemoveGroup(id, action)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        if(action.equals("assign")) {
                            view.onConfirmationSuccess();
                        } else {
                            view.onChangeGroupSuccess();
                        }
                        view.showWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(action.equals("assign")) {
                            view.onConfirmationFailure(e.toString());
                        }
                        view.showWaitDialog(false);
                    }
                }));
    }
}
