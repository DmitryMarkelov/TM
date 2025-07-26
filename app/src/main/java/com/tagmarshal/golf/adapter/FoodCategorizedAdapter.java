package com.tagmarshal.golf.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.FoodModel;
import com.tagmarshal.golf.viewholder.FoodCategorizedViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FoodCategorizedAdapter extends RecyclerView.Adapter<FoodCategorizedViewHolder> {

    private final boolean isFood;
    private final Map<String, Collection<FoodModel>> foodMap;
    private final ArrayList<String> keySet;

    public FoodCategorizedAdapter(Map<String, Collection<FoodModel>> categoriedFoodMap, boolean isFood) {
        this.foodMap = categoriedFoodMap;
        this.isFood = isFood;
        this.keySet = new ArrayList<>(foodMap.keySet());
    }

    @NonNull
    @Override
    public FoodCategorizedViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FoodCategorizedViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_catigorized_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodCategorizedViewHolder viewHolder, int position) {
        String currentKey = keySet.get(position);
        viewHolder.bind(currentKey, new ArrayList<>(foodMap.get(currentKey)), isFood);
    }

    @Override
    public int getItemCount() {
        return foodMap.size();
    }
}
