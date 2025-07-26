package com.tagmarshal.golf.fragment.groups.group;

import com.tagmarshal.golf.rest.GolfAPI;

import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Response;

public class FragmentPagerGroupModel implements FragmentPagerGroupContract.Model {
    @Override
    public Single<Response<ResponseBody>> confirmOrRemoveGroup(String id, String action) {
        return GolfAPI.getGolfCourseApi().confirmOrRemoveGroup(id, action);
    }

    @Override
    public Single<Response<ResponseBody>> sendSkipMessage() {
        return Single.just(Response.success(new ResponseBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public BufferedSource source() {
                return null;
            }
        }));
    }


}
