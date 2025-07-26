package com.tagmarshal.golf.fragment.roundrate;

import android.util.Log;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.rest.model.RestRatingModel;
import com.tagmarshal.golf.rest.model.SaveScoreModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class RoundRateFragmentPresenter implements RoundRateFragmentContract.Presenter {

    private final RoundRateFragmentContract.View view;
    private final RoundRateFragmentContract.Model model;
    private final CompositeDisposable disposable;
    private Disposable timer;

    public RoundRateFragmentPresenter(RoundRateFragmentContract.View view) {
        this.view = view;
        this.model = new RoundRateFragmentModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void rateRound(String roundId, String position, int rating, String comment) {

        RestRatingModel ratingModel = new RestRatingModel(
                position,
                rating,
                comment
        );

        disposable.add(model.rateRound(roundId, ratingModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            view.onRateSuccess();
                        } else {
                            try {
                                view.onRateFailure(response.errorBody().string());
                            } catch (Exception e) {
                                view.onRateFailure(GolfApplication.context.getString(R.string.rate_round_failure));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onRateFailure(e.toString());
                    }
                }));
    }

    @Override
    public void closeTimer() {
        timer = Observable.timer(5, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnDispose(() -> Log.d(getClass().getName(), "closeTimer onDispose: timer isDisposed"))
                .subscribe(
                        __ -> view.openEndOfRoundScreen(),
                        e -> Log.d(getClass().getName(), "closeTimer: " + e.getMessage())
                );

    }

    @Override
    public void sendBadRound(String imei) {
        disposable.add(model.sendBadRound(imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            view.onSuccessBadRating();
                        } else {
                            try {
                                view.onRateFailure(response.errorBody().string());
                            } catch (Exception e) {
                                view.onRateFailure(GolfApplication.context.getString(R.string.rate_round_failure));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onRateFailure(e.toString());
                    }
                }));
    }

    @Override
    public void onDestroy() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }

        if (timer != null && !timer.isDisposed()) {
            timer.dispose();
        }
    }
}
