package com.tagmarshal.golf.fragment.waiver;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.Disclaimer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class FragmentWaiverPresenter implements FragmentWaiverContract.Presenter {
    private FragmentWaiverContract.View view;
    private FragmentWaiverModel model;
    private final CompositeDisposable disposable;

    public FragmentWaiverPresenter(FragmentWaiverContract.View view) {
        this.view = view;
        this.disposable = new CompositeDisposable();
        this.model = new FragmentWaiverModel();
    }

    @Override
    public void getWaiver() {
        Disclaimer disclaimer = PreferenceManager.getInstance().getDisclaimer();
        view.showWaiver(disclaimer);
    }

    @Override
    public void onAgree() {
        disposable.add(
                model.onAgree()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(responseBody -> Timber.tag("Waiver").d(responseBody.toString()), e -> Timber.tag("Waiver Error").d(e.toString()))
        );

    }
}
