package com.tagmarshal.golf.fragment.devicesetup;

import android.annotation.SuppressLint;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.GolfAPI;

import org.json.JSONArray;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FragmentDeviceSetupPresenter implements FragmentDeviceSetupContract.Presenter {

    private final FragmentDeviceSetupContract.View view;
    private final FragmentDeviceSetupContract.Model model;
    private final CompositeDisposable disposable;

    public FragmentDeviceSetupPresenter(FragmentDeviceSetupContract.View view) {
        this.view = view;
        this.model = new FragmentDeviceSetupModel();
        this.disposable = new CompositeDisposable();
    }

    @SuppressLint("CheckResult")
    @Override
    public void registerVariableDevice(String imei, String type, String build) {
        view.showWaitDialog(true);

        disposable.add(model.registerVariableDevice(imei, type, build)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        switch (response.code()) {
                            case 200:
                                try {
                                    JSONArray jsonArray = new JSONArray(response.body().string());
                                    if (jsonArray.getJSONObject(0).has("tag")) {
                                        view.onVariableRegistrationSuccess(jsonArray.getJSONObject(0).getString("tag"));
                                        GolfAPI.bindBaseCourseUrl(PreferenceManager.getInstance().getCurrentCourseModel().getCourseUrl());
                                    } else if (jsonArray.getJSONObject(0).has("Error")) {
                                        view.onVariableRegistrationFailure(jsonArray.getJSONObject(0).getString("Error"));
                                    } else{
                                        view.onVariableRegistrationFailure(jsonArray.toString());
                                    }
                                } catch (Exception e) {
                                    view.onVariableRegistrationFailure(e.toString());
                                }
                                break;
                            default:
                                try {
                                    view.onVariableRegistrationFailure(response.errorBody().string());
                                } catch (Exception e) {
                                    view.onVariableRegistrationFailure(e.toString());
                                }
                                break;
                        }

                        view.showWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onVariableRegistrationFailure(e.toString());
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
