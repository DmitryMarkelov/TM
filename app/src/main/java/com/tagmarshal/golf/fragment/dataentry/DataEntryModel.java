package com.tagmarshal.golf.fragment.dataentry;

import com.tagmarshal.golf.rest.GolfAPI;
import com.tagmarshal.golf.rest.model.CompletedFields;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

public class DataEntryModel implements FragmentDataEntryContract.Model {
    @Override
    public Observable<ResponseBody> submitData(List<CompletedFields> completedFields) {
        return GolfAPI.getGolfCourseApi().submitDataEntry(completedFields);
    }
}
