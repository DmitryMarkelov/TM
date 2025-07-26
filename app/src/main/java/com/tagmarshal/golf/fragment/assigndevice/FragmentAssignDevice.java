package com.tagmarshal.golf.fragment.assigndevice;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.CourseAdapter;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentAssignDevice extends BaseFragment {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.info_tv)
    TMTextView mInfo;

    @BindView(R.id.recyclerview)
    RecyclerView mRecycler;

    private LinearLayoutManager layoutManager;

    private CourseAdapter courseAdapter;

    Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_device_number, container, false);

        unbinder = ButterKnife.bind(this, view);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ", new ImageSpan(getActivity(), R.drawable.ic_help_text), 0)
                .append(" Assign the device to the course it will be used on. Permantly assigning" +
                        " a device can be undone through Admin in the menu.");

        layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);

//        courseAdapter = new CourseAdapter();

        mRecycler.setAdapter(courseAdapter);

        mInfo.setText(builder);

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }

    @OnClick(R.id.btn_cancel)
    void onCancelClick() {
//        getActivity().onBackPressed();
//        openNewFragment(this, new FragmentGroupsPager(), "fragment_groups_pager");
    }
}
