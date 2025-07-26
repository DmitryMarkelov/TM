package com.tagmarshal.golf.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.FoodModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FoodCategorizedViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.category_name)
    TextView categoryName;
    @BindView(R.id.category_recycler)
    RecyclerView categoryRecycler;


    public FoodCategorizedViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void bind(String category, List<FoodModel> foodList, boolean isFood){
        if(category.equals("")){
            categoryName.setVisibility(View.GONE);
        }else{
            categoryName.setVisibility(View.VISIBLE);
            categoryName.setText(category);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
        categoryRecycler.setLayoutManager(layoutManager);
    }


}
