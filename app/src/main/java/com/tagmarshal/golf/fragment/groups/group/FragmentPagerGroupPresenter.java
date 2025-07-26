package com.tagmarshal.golf.fragment.groups.group;

import android.annotation.SuppressLint;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentPagerGroupPresenter implements FragmentPagerGroupContract.Presenter {

    private final FragmentPagerGroupContract.Model model;
    private final FragmentPagerGroupContract.View view;
    private final CompositeDisposable disposable;

    public FragmentPagerGroupPresenter(FragmentPagerGroupContract.View view) {
        this.view = view;
        this.model = new FragmentPagerGroupModel();
        this.disposable = new CompositeDisposable();
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
                        view.onConfirmationSuccess();
                        view.showWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onConfirmationFailure(e.toString());
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
