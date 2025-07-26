package com.tagmarshal.golf.fragment.calibration;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.util.TMUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentCalibrationMode extends BaseFragment implements CalibrationView {
    private final int LAYOUT = R.layout.fragment_calibration;

    private CalibrationPresenter presenter;

    @BindView(R.id.top_margin)
    View mTopMargin;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        presenter.startDismissingTimer();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, view);
        presenter = new CalibrationPresenter(this);
        return view;
    }

    @OnClick(R.id.okBtn)
    void onOkClick() {
        requireActivity().onBackPressed();
    }


    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onCalibrationFinish() {
        requireActivity().onBackPressed();
    }

    @Override
    public void onCalibrationError(Throwable e) {
        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
