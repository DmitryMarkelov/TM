package com.tagmarshal.golf.activity.splash;

import android.annotation.SuppressLint;
import android.os.Build;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.rest.model.RestDeviceConfigModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class SplashActivityPresenter implements SplashActivityContract.Presenter {

    private final SplashActivityContract.View view;
    private final SplashActivityContract.Model model;

    private final CompositeDisposable disposables;

    public SplashActivityPresenter(SplashActivityContract.View view) {
        this.disposables = new CompositeDisposable();
        this.model = new SplashActivityModel();
        this.view = view;
    }

    @Override
    public void getCourseConfig(CourseModel course) {
        String url = course.getCourseUrl();
        GolfAPI.bindBaseCourseUrl(url);

        disposables.add(Observable.just(PreferenceManager.getInstance().isDeviceVariable())
                .subscribeOn(Schedulers.io())
                .flatMap(isVariable -> {
                    String type = isVariable
                            ? "variable"
                            : PreferenceManager.getInstance().getDeviceType();
                    return registerDevice(type, Build.DISPLAY);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if(response.isValid()) {
                                PreferenceManager.getInstance().clearGameData(url);
                                PreferenceManager.getInstance().clearCurrentCourse(url);
                                model.saveCourseConfig(response.getTag(), course, true);
                                model.saveNearestCoursesConfigs(response);
                                view.onSuccessSaveConfig();
                            }
                        },
                        e -> view.onSaveConfigFailure(e.getMessage())
                )

        );
    }

    @SuppressLint("CheckResult")
    @Override
    public Observable<RestDeviceConfigModel> registerDevice(String type, String build) {
        return model.registerDevice(type, build)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable();
    }

    @Override
    public void checkInRound() {
        disposables.add(model.checkInRound()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RestInRoundModel>() {
                    @Override
                    public void onSuccess(RestInRoundModel restInRoundModel) {
                        if (restInRoundModel != null) {
                            view.onGetInRoundInfo(restInRoundModel);
                        } else {
                            view.onGetInRoundInfoFailure("Round not found.");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onGetInRoundInfoFailure(e.toString());
                    }
                }));
    }
}
