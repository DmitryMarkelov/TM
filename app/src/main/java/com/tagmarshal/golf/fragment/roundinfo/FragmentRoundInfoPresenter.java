package com.tagmarshal.golf.fragment.roundinfo;

import android.annotation.SuppressLint;

import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentRoundInfoPresenter implements FragmentRoundInfoContract.Presenter {

    private final FragmentRoundInfoContract.View view;
    private final FragmentRoundInfoContract.Model model;

    public FragmentRoundInfoPresenter(FragmentRoundInfoContract.View view) {
        this.view = view;
        this.model = new FragmentRoundInfoModel();
    }

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @SuppressLint("CheckResult")
    @Override
    public void getRoundInfo() {
        view.showLoadingDialog(true);

        compositeDisposable.add(model.getRoundInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RestInRoundModel>() {
                    @Override
                    public void onSuccess(@NonNull RestInRoundModel restInRoundModel) {
                        view.onGetRoundInfo(restInRoundModel);
                        view.showLoadingDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onGetRoundInfoFailure(e.toString());
                        view.showLoadingDialog(false);
                    }
                }));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }


}
