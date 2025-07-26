package com.tagmarshal.golf.activity.gg_scoring;

import android.annotation.SuppressLint;
import android.util.Log;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.SaveScoreModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ScoringPresenter implements ScoringContract.Presenter {

    private final ScoringContract.Model model;
    private final ScoringContract.View view;
    private final CompositeDisposable disposable;

    private Disposable inactiveTimerDisposable;
    private Disposable backToMapTimer;

    public ScoringPresenter(ScoringContract.View view) {
        this.view = view;
        this.model = new ScoringModel();
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

    @SuppressLint("CheckResult")
    @Override
    public void insertScore(SaveScoreModel saveScoreModel) {
        view.showWaitDialog(true);

        disposable.add(model.insertScore(saveScoreModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(() -> view.showWaitDialog(false))
                .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        view.showWaitDialog(false);
                        view.onInsertScore(saveScoreModel);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showWaitDialog(false);
                        view.showFailureMessage(saveScoreModel);
                    }
                }));
    }

    @Override
    public void startInactiveStateTimer() {
        disposable.add(model.startInactiveStateTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        List<SaveScoreModel> failedScores = PreferenceManager.getInstance().getGolfFailedScores();
                        Log.d("log-scoring", "Failed score count: " + failedScores.size());
                        for (SaveScoreModel saveScoreModel : failedScores) {
                            disposable.add(model.insertScore(saveScoreModel)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnDispose(() -> view.showWaitDialog(false))
                                    .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                                        @Override
                                        public void onSuccess(ResponseBody responseBody) {
                                            List<SaveScoreModel> oldFailedScores = PreferenceManager.getInstance().getGolfFailedScores();
                                            List<SaveScoreModel> newFailedScores = new ArrayList<>();
                                            for (SaveScoreModel m : oldFailedScores) {
                                                if (m.getHole() != saveScoreModel.getHole() && !m.getRoundId().equals(saveScoreModel.getRoundId())) {
                                                    newFailedScores.add(m);
                                                }
                                            }
                                            PreferenceManager.getInstance().saveFailedScores(newFailedScores);
                                            if (newFailedScores.size() == 0)
                                                view.showSignalRestored();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.d("log-scoring", "Failed to save score for hole: " + saveScoreModel.getHole() + " " + e.getMessage());
                                        }
                                    }));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void startBackToMapTimer() {
        stopBackToMapTimer();

        backToMapTimer = model.startBackToMapTimer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        view.backToMap();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        disposable.add(backToMapTimer);
    }

    @Override
    public void stopBackToMapTimer() {
        if (backToMapTimer != null) {
            if (!backToMapTimer.isDisposed()) {
                backToMapTimer.dispose();
            }
            backToMapTimer = null;
        }
    }
}
