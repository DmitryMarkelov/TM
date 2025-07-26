package com.tagmarshal.golf.fragment.admin;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.FileWorkerContract;
import com.tagmarshal.golf.data.FileWorkerModel;
import com.tagmarshal.golf.eventbus.RoundInfoEvent;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.SupportLogModel;
import com.tagmarshal.golf.util.TMUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class FragmentAdminPresenter implements FragmentAdminContract.Presenter, FileWorkerContract.Presenter {

    private final FragmentAdminContract.Model model;
    private final FragmentAdminContract.View view;
    private final FileWorkerModel fileModel;

    private CompositeDisposable disposable = new CompositeDisposable();

    public FragmentAdminPresenter(FragmentAdminContract.View view) {
        this.view = view;
        this.model = new FragmentAdminModel();
        this.fileModel = new FileWorkerModel();
    }


    @Override
    public void sendSupportLogs() {
        disposable.add(model.sendLogsToSupport(new SupportLogModel(
                TMUtil.getTimeUTC(System.currentTimeMillis()),
                PreferenceManager.getInstance().getCourseName(),
                PreferenceManager.getInstance().getCourse(),
                TMUtil.getDeviceIMEI(),
                getLogsFromFile()))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(__ -> view.showWaitDialog(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {
                            clearFixesFile();
                            view.showWaitDialog(false);
                        },
                        e -> {
                            view.showWaitDialog(false);
                            view.onRequestFailure(e.getMessage());
                        }

                )

        );
    }

    private void clearFixesFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/fixes_info.txt");
        PrintWriter writer = null;
        if (file.exists()) {
            try {
                writer = new PrintWriter(file);
                writer.print("");
                writer.close();
            } catch (FileNotFoundException e) {
            }
        }
    }

    private List<String> getLogsFromFile() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(LogFileConstants.fixes_filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> logList = new ArrayList<>();
        if (scanner != null) {
            while (scanner.hasNextLine()) {
                logList.add(scanner.nextLine());
            }
            return logList;
        } else {
            try {
                throw new FileNotFoundException();
            } catch (FileNotFoundException e) {
                view.onRequestFailure("File does not exists!");
            }
            return null;
        }
    }

    @Override
    public void sendEndOfRound() {
        view.showWaitDialog(true);

        disposable.add(model.sendEndOfRound()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<retrofit2.Response<ResponseBody>>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        view.onSentEndOfRound();
                        view.showWaitDialog(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onRequestFailure(e.toString());
                        view.showWaitDialog(false);
                    }
                }));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getRoundInfo() {
        disposable.add(model.getRoundInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.showWaitDialog(false))
                .subscribe(round -> {
                            view.onGetRoundInfo(round);
                            EventBus.getDefault().postSticky(new RoundInfoEvent(round));

                            view.showWaitDialog(false);
                        },
                        e -> view.showWaitDialog(false)
                ));

    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }

    @Override
    public void writeToFile(String tag, String timeUTC, String valueOf) {
        disposable.add(fileModel.writeToFile(tag, timeUTC, valueOf)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("LOG COMPLETED");
                }, Timber::d)
        );
        ;
    }
}
