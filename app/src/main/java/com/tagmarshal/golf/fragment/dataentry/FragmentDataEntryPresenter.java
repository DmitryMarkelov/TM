package com.tagmarshal.golf.fragment.dataentry;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.CompletedFields;
import com.tagmarshal.golf.rest.model.Fields;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class FragmentDataEntryPresenter implements FragmentDataEntryContract.Presenter {

    FragmentDataEntryContract.View view;
    private DataEntryModel model;
    private final CompositeDisposable disposable;

    public FragmentDataEntryPresenter(FragmentDataEntryContract.View view) {
        this.view = view;
        this.disposable = new CompositeDisposable();
        this.model = new DataEntryModel();
    }

    @Override
    public void getFields() {
        List<Fields> fields = PreferenceManager.getInstance().getDisclaimer().getFields();
        if (!fields.isEmpty()) {
            view.showFields(fields);
        } else {

        }
    }

    @Override
    public void onContinue(List<CompletedFields> completedFields) {
        disposable.add(
                model.submitData(completedFields)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(responseBody -> {
                            view.onDataCaptureResponse(true);
                            Timber.tag("Disclaimer").d(responseBody.toString());
                        }, e -> {
                            view.onDataCaptureResponse(false);
                            Timber.tag("Disclaimer Error").d(e.toString());
                        }));
    }
}
