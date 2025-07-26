package com.tagmarshal.golf.fragment.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.constants.LogFileConstants;
import com.tagmarshal.golf.data.Maintenance;
import com.tagmarshal.golf.eventbus.EventAdminScreenEntered;
import com.tagmarshal.golf.eventbus.SetMaintenanceEvent;
import com.tagmarshal.golf.eventbus.StopMaintenanceEvent;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.fragment.devicesetup.FragmentDeviceSetup;
import com.tagmarshal.golf.fragment.groups.pager.FragmentGroupsPager;
import com.tagmarshal.golf.fragment.selectcourse.FragmentSelectCourse;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.CartControlHelper;
import com.tagmarshal.golf.util.GeofenceHelper;
import com.tagmarshal.golf.util.SnapToPlayHelper;
import com.tagmarshal.golf.util.TMUtil;

import com.tagmarshal.golf.view.SideMenuItem;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentAdmin extends BaseFragment implements FragmentAdminContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    private FragmentAdminPresenter presenter;

    Unbinder unbinder;
    private boolean isNullModel = true;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialogEnd;
    TimePickerDialog timePickerDialogEnd;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_panel, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentAdminPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        EventBus.getDefault().post(new EventAdminScreenEntered());
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceManager.getInstance().getPaceInfo() != null) {
            onGetRoundInfo(PreferenceManager.getInstance().getPaceInfo());
        }

        getBaseActivity().enableCheckingForNewZone(false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!SnapToPlayHelper.hasMusic) { // If there is no configuration set for hasMusic (set via RestBoundsModel.SnapToPlayModel) hide the pairing item
                    View itemPairSnapToPlayDivider = view.findViewById(R.id.pair_snaptoplay_divider);
                    if (itemPairSnapToPlayDivider != null) {
                        getActivity().runOnUiThread(() -> itemPairSnapToPlayDivider.setVisibility(View.GONE));
                    }
                    SideMenuItem itemPairSnapToPlay = view.findViewById(R.id.item_pair_snaptoplay);
                    if (itemPairSnapToPlay != null) {
                        getActivity().runOnUiThread(() -> itemPairSnapToPlay.setVisibility(View.GONE));
                    }
                }
            }
        }, 50);
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;

        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    @OnClick(R.id.item_pair_snaptoplay)
    void onPairSnapToPlayClick() {
        showPairingAlert();
        clearAndAddRootFragment(new FragmentGroupsPager());
    }

    private void showPairingAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Enabling SnapToPlay Pairing Mode")
                .setMessage("Please wait for the SnapToPlay device to enter pairing mode.")
                .setCancelable(false)
                .create();

        alertDialog.show();
        new Handler().postDelayed(() -> {
            SnapToPlayHelper.turnOnPairingMode();
            alertDialog.dismiss();
        }, 4000);
    }

    @OnClick(R.id.item_change_course)
    void onItemChangeCourseClick() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        presenter.writeToFile(LogFileConstants.course_changed, TMUtil.getTimeUTC(System.currentTimeMillis()), String.valueOf(TMUtil.getBatteryLevel()));
        clearAndAddRootFragment(FragmentSelectCourse.newInstance(false));
    }

    @OnClick(R.id.item_change_group)
    void onItemChangeGroupClick() {
//        PreferenceManager.getInstance().setCourseGroupId(null);
        clearAndAddRootFragment(new FragmentGroupsPager());
    }

    @OnClick(R.id.item_clear_data)
    void onItemClearDataClick() {
        PreferenceManager.getInstance().clearData(requireContext());
        GeofenceHelper.geofenceList.clear();
        CartControlHelper.geofenceSendQueue.clear();
        CartControlHelper.geofenceList.clear();
        CartControlHelper.inCartControlGeofence = false;
        getBaseActivity().refreshCourse();
        getBaseActivity().clearLogo();
        clearAndAddRootFragment(new FragmentDeviceSetup());
    }

    @OnClick(R.id.rotate_screen_button)
    void onItemRotateScreen() {
        PreferenceManager.getInstance().setOrientation(PreferenceManager.getInstance().getOrientation() == PreferenceManager.ORIENTATION_NORMAL ?
                PreferenceManager.ORIENTATION_REVERSE : PreferenceManager.ORIENTATION_NORMAL);
        if (getActivity() != null) {
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        }
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.item_end_round)
    void onItemEndRoundClick() {
        presenter.sendEndOfRound();
    }

    @OnClick(R.id.btn_back)
    void onCancelClick() {
        clearAndAddRootFragment(new FragmentGroupsPager());
    }


    @OnClick(R.id.item_support_logs)
    void onSupportLogsClick() {
        presenter.sendSupportLogs();
    }

    @OnClick(R.id.item_set_maintenance)
    void onSetMaintenanceClick() {
        showDatePickerStart();
    }

    @OnClick(R.id.item_clear_maintenance)
    void onClearMaintenanceClick() {
        EventBus.getDefault().postSticky(new StopMaintenanceEvent());
    }

    private void showDatePickerStart() {
        Calendar dateAndTime = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(requireContext(), R.style.DateTimerPicker, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String dateToPass = String.valueOf(year) + "-" + checkDigit(month + 1) + "-" + checkDigit(day);
                datePickerDialog.dismiss();
                showTimerPickerStart(dateToPass);

            }
        },
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.setTitle("Pick start date");
        datePickerDialog.show();
    }

    private void showTimerPickerStart(String date) {
        Calendar dateAndTime = Calendar.getInstance();

        timePickerDialog = new TimePickerDialog(requireContext(), R.style.MyTimePickerWidgetStyle, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String startDateTime = date + " " + checkDigit(hour) + ":" + checkDigit(minute) + ":00";

                timePickerDialog.dismiss();
                showDatePickerEnd(startDateTime);

            }
        },
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true);
        timePickerDialog.setTitle("Pick start time");
        timePickerDialog.show();
    }

    private void showDatePickerEnd(String startTime) {
        Calendar dateAndTime = Calendar.getInstance();
        datePickerDialogEnd = new DatePickerDialog(requireContext(), R.style.DateTimerPicker, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String dateToPass = String.valueOf(year) + "-" + checkDigit(month + 1) + "-" + checkDigit(day);
                datePickerDialogEnd.dismiss();
                showTimerPickerEnd(startTime, dateToPass);

            }
        },
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialogEnd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialogEnd.setTitle("Pick end date");
        datePickerDialogEnd.show();
    }

    private void showTimerPickerEnd(String startTime, String date) {
        Calendar dateAndTime = Calendar.getInstance();

        timePickerDialogEnd = new TimePickerDialog(requireContext(), R.style.MyTimePickerWidgetStyle, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String endDateTime = date + " " + checkDigit(hour) + ":" + checkDigit(minute) + ":00";
                PreferenceManager.getInstance().saveMaintenance(new Maintenance(startTime, endDateTime));
                EventBus.getDefault().postSticky(new SetMaintenanceEvent());
                timePickerDialog.dismiss();
            }
        },
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true);
        timePickerDialogEnd.setTitle("Pick end time");
        timePickerDialogEnd.show();
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    @Override
    public void onSentEndOfRound() {
        clearAndAddRootFragment(new FragmentGroupsPager());
    }

    @Override
    public void onGetRoundInfo(RestInRoundModel restInRoundModel) {
        PreferenceManager.getInstance().saveLastRoundID(restInRoundModel.getId());
        isNullModel = restInRoundModel.isNullModel();
    }

    @Override
    public void onRequestFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }


    public boolean getIsNullModel() {
        return isNullModel;

    }
}
