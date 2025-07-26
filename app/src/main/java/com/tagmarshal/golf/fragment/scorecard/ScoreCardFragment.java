package com.tagmarshal.golf.fragment.scorecard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.ScoreCardAdapter;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.GolfGeniusModel;
import com.tagmarshal.golf.util.TMUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScoreCardFragment extends BaseFragment {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.recyclerview)
    RecyclerView mRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_score_card, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);

        GolfGeniusModel golfGeniusModel = PreferenceManager.getInstance().getGolfGenius();

        if(golfGeniusModel != null && golfGeniusModel.getScores() != null)  {
            ScoreCardAdapter scoreCardAdapter = new ScoreCardAdapter(golfGeniusModel.getScores(), golfGeniusModel.getPlayers());
            mRecycler.setAdapter(scoreCardAdapter);
        }
    }
}
