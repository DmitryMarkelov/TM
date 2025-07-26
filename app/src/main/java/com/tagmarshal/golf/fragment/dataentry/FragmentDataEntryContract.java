package com.tagmarshal.golf.fragment.dataentry;

import com.tagmarshal.golf.rest.model.CompletedFields;
import com.tagmarshal.golf.rest.model.Fields;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

public class FragmentDataEntryContract {
    public interface View {
        void showFields(List<Fields> fieldsList);
        void onDataCaptureResponse(boolean isSuccessful);
    }

    public interface Presenter {
        void getFields();

        void onContinue(List<CompletedFields> completedFields);
    }

    public interface Model {
        Observable<ResponseBody> submitData(List<CompletedFields> completedFields);
    }
}
