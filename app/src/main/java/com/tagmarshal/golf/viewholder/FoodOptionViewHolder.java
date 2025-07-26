package com.tagmarshal.golf.viewholder;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.adapter.OnOptionClickListener;
import com.tagmarshal.golf.rest.model.FoodModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class FoodOptionViewHolder extends RecyclerView.ViewHolder {


    @BindView(R.id.food_option_tv)
    AppCompatTextView optionTv;

    private OnOptionClickListener onOptionClickListener;
    private FoodModel.AdditionModel option;

    public FoodOptionViewHolder(@NonNull View itemView, OnOptionClickListener onOptionClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onOptionClickListener = onOptionClickListener;

    }

    public void bindData(FoodModel.AdditionModel option){
        this.option = option;
        optionTv.setText(option.getName());

    }

    @OnCheckedChanged(R.id.food_cb)
    public void onOptionCheck(CompoundButton btn, boolean checked){
        if(checked){
            onOptionClickListener.onCheckItem(option);
        }else{
            onOptionClickListener.onUncheckItem(option);
        }
    }

}
