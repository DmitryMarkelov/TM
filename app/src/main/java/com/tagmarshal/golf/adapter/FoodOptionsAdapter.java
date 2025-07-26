package com.tagmarshal.golf.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.FoodModel;
import com.tagmarshal.golf.viewholder.FoodOptionViewHolder;

import java.util.List;

public class FoodOptionsAdapter extends RecyclerView.Adapter<FoodOptionViewHolder> {

    private final OnOptionClickListener onOptionsClickListener;
    private List<FoodModel.AdditionModel> optionList;


    public FoodOptionsAdapter(List<FoodModel.AdditionModel> optionList, OnOptionClickListener onOptionClickListener) {
        this.optionList = optionList;
        this.onOptionsClickListener = onOptionClickListener;
    }

    @NonNull
    @Override
    public FoodOptionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.option_item,viewGroup, false);
        return new FoodOptionViewHolder(view, onOptionsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodOptionViewHolder foodOptionViewHolder, int i) {
        foodOptionViewHolder.bindData(optionList.get(i));
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }
}

