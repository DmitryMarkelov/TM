package com.tagmarshal.golf.fragment.groups.pager;

import android.annotation.SuppressLint;
import android.util.Log;

import com.tagmarshal.golf.rest.model.RestTeeTimesModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentGroupsPagerPresenter implements FragmentGroupsPagerContract.Presenter {

    private final FragmentGroupsPagerContract.Model model;
    private final FragmentGroupsPagerContract.View view;
    private final CompositeDisposable disposable;

    private Disposable disposableInactivity;

    public FragmentGroupsPagerPresenter(FragmentGroupsPagerContract.View view) {
        this.view = view;
        this.model = new FragmentGroupsPagerModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        view.showWaitDialog(false);
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public void startInactiveStateTimer() {
        if (disposableInactivity != null && !disposableInactivity.isDisposed()) {
            disposableInactivity.dispose();
        }

        disposable.add(disposableInactivity = model.startInactiveStateTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(()-> view.showWaitDialog(false))
                .subscribeWith(new DisposableSingleObserver<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        if (disposableInactivity != null && !disposableInactivity.isDisposed()) {
                            disposableInactivity.dispose();
                        }

                        view.onInactiveState();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ERROR", e.toString());
                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getGroups() {
        view.showWaitDialog(true);

        disposable.add(model.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(()-> view.showWaitDialog(false))
                .subscribeWith(new DisposableSingleObserver<List<RestTeeTimesModel>>() {
                    @Override
                    public void onSuccess(List<RestTeeTimesModel> restTeeTimesModels) {
                        view.showWaitDialog(false);
                        view.showGroups(restTeeTimesModels);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showWaitDialog(false);
                        view.showFailureMessage(e.toString());
                    }
                }));
    }
}
