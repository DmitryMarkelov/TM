package com.tagmarshal.golf.callback;

import com.google.android.gms.maps.model.LatLng;
import com.tagmarshal.golf.rest.model.RestAlertModel;
import com.tagmarshal.golf.rest.model.RestHoleDistanceModel;
import com.tagmarshal.golf.rest.model.RestInRoundModel;

import java.util.List;

public class GolfCallback {

    public interface GolfServiceListener {
        void onGetPaceInfo(RestInRoundModel inRoundModel);

        void onGetHoleDistanceFromPoint(RestHoleDistanceModel distanceModel);

        void onGetAlerts(List<RestAlertModel> alertsList);

        void onLocationChanged(LatLng location, float accuracy, boolean inGeofence);

        void onCanUpdateDistanceFromMeToCurrentHole(RestHoleDistanceModel distanceModel);

        void onFocusBetweenMeAndHole();

    }

    public interface GolfDialogListener {
        void onOkClick();

        void onBottomBtnClick();
    }

    public interface OnGroupPagerRefreshListener {
        void onRefreshGroupsClick();
    }

    public interface OnGroupSwipePageListener {
        void onPrevPage();

        void onNextPage();
    }
}
