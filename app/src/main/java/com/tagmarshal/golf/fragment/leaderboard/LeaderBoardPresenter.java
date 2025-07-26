package com.tagmarshal.golf.fragment.leaderboard;

import android.annotation.SuppressLint;

import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LeaderBoardPresenter implements LeaderBoardContract.Presenter {
    private final LeaderBoardContract.View view;
    private final LeaderBoardContract.Model model;

    private Disposable infoDisposable;

    public LeaderBoardPresenter(LeaderBoardContract.View view) {
        this.view = view;
        this.model = new LeaderBoardModel();
    }

    @SuppressLint("CheckResult")
    @Override
    public void getRoundInfo() {
        infoDisposable = model.getRoundInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.showLoading(true))
                .subscribeWith(new DisposableSingleObserver<RestInRoundModel>() {
                    @Override
                    public void onSuccess(RestInRoundModel restInRoundModel) {
                        view.onGetRoundInfo(restInRoundModel);
                        view.showLoading(false);
                        EventBus.getDefault().postSticky(new RoundInfoEvent(restInRoundModel));
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onFailure(e.toString());
                        view.showLoading(false);
                    }
                });
    }


    @Override
    public void onDestroy() {
        dispose();
    }

    private void dispose() {
        if (infoDisposable != null && !infoDisposable.isDisposed()) {
            infoDisposable.dispose();
            infoDisposable = null;
        }
    }
}
