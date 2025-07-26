package com.tagmarshal.golf.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.GolfGeniusModel;
import com.tagmarshal.golf.view.TMTextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScoreCardViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tvHoleNumber)
    TextView mHoleNumber;

    // p1
    @BindView(R.id.tvTMPlayerOneName)
    TMTextView mPlayerOneName;

    @BindView(R.id.tvPlayerOneStrokes)
    TextView mPlayerOneStrokes;

    // p2
    @BindView(R.id.tvTMPlayerTwoName)
    TMTextView mPlayerTwoName;

    @BindView(R.id.tvPlayerTwoStrokes)
    TextView mPlayerTwoStrokes;

    // p3
    @BindView(R.id.tvTMPlayerThreeName)
    TMTextView mPlayerThreeName;

    @BindView(R.id.tvPlayerThreeStrokes)
    TextView mPlayerThreeStrokes;

    // p4
    @BindView(R.id.tvTMPlayerFourName)
    TMTextView mPlayerFourName;

    @BindView(R.id.tvPlayerFourStrokes)
    TextView mPlayerFourStrokes;

    public ScoreCardViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(GolfGeniusModel.Score score, List<GolfGeniusModel.Player> players) {
        mHoleNumber.setText(String.format(Locale.ENGLISH, "Hole %d", score.getHole()));
        List<Integer> scores = score.getScores();

        if(scores.size() > 0) { // player 1 score
            mPlayerOneStrokes.setText(String.valueOf(scores.get(0)));
            mPlayerOneName.setText(players.get(0).getName());
        } else {
            mPlayerOneStrokes.setVisibility(View.GONE);
            mPlayerOneName.setVisibility(View.GONE);
        }

        if(scores.size() > 1) { // player 2 score
            mPlayerTwoStrokes.setText(String.valueOf(scores.get(1)));
            mPlayerTwoName.setText(players.get(1).getName());
        } else {
            mPlayerTwoStrokes.setVisibility(View.GONE);
            mPlayerTwoName.setVisibility(View.GONE);
        }

        if(scores.size() > 2) { // player 3 score
            mPlayerThreeStrokes.setText(String.valueOf(scores.get(2)));
            mPlayerThreeName.setText(players.get(2).getName());
        } else {
            mPlayerThreeStrokes.setVisibility(View.GONE);
            mPlayerThreeName.setVisibility(View.GONE);
        }

        if(scores.size() > 3) { // player 4 score
            mPlayerFourStrokes.setText(String.valueOf(scores.get(3)));
            mPlayerFourName.setText(players.get(3).getName());
        } else {
            mPlayerFourStrokes.setVisibility(View.GONE);
            mPlayerFourName.setVisibility(View.GONE);
        }
    }
}
