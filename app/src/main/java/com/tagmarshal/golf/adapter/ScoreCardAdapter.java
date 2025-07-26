package com.tagmarshal.golf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.GolfGeniusModel;
import com.tagmarshal.golf.viewholder.ScoreCardViewHolder;

import java.util.List;

public class ScoreCardAdapter extends RecyclerView.Adapter<ScoreCardViewHolder> {

    private final List<GolfGeniusModel.Score> scores;
    private final List<GolfGeniusModel.Player> players;

    public ScoreCardAdapter(List<GolfGeniusModel.Score> scores, List<GolfGeniusModel.Player> players) {
        this.scores = scores;
        this.players = players;
    }

    @NonNull
    @Override
    public ScoreCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.score_item,
                viewGroup, false);
        return new ScoreCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreCardViewHolder courseViewHolder, int i) {
        courseViewHolder.bindData(scores.get(i), players);
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }
}
