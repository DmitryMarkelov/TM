package com.tagmarshal.golf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.MenuItemModifierOption;
import com.tagmarshal.golf.rest.model.RestBoundsModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuItemOptionsItemAdapter extends RecyclerView.Adapter<MenuItemOptionsItemAdapter.ViewHolder> {

    private final List<MenuItemModifierOption> menuItemModifierOptionList;
    private final Context context;
    private final boolean selectMultiple;
    private final OnOptionClickListener onOptionClickListener;
    private MenuItemModifierOption selectedOption;
    private List<MenuItemModifierOption> selectedOptions = new ArrayList<>();

    public MenuItemOptionsItemAdapter(List<MenuItemModifierOption> menuItemModifierOptionList, List<MenuItemModifierOption> selectedOptions, boolean selectMultiple, boolean choiceRequired, Context context, OnOptionClickListener onOptionClickListener) {
        this.menuItemModifierOptionList = menuItemModifierOptionList;
        this.selectMultiple = selectMultiple;
        this.selectedOptions = selectedOptions;
        this.context = context;
        this.onOptionClickListener = onOptionClickListener;
    }

    @NonNull
    @io.reactivex.annotations.NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.menu_item_options_item, parent, false));
    }

    public List<MenuItemModifierOption> getSelectedOptions() {
        return selectedOptions;
    }

    @Override
    public void onBindViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewHolder holder, int position) {
        MenuItemModifierOption menuItemModifierOption = menuItemModifierOptionList.get(position);
        RestBoundsModel mapBounds = PreferenceManager.getInstance().getMapBounds();
        holder.optionName.setText(menuItemModifierOption.getName());
        holder.optionPrice.setText(String.format("%s%s", mapBounds.getCurrency(), menuItemModifierOption.getPrice()));
        if (selectMultiple) {
            holder.optionRadioButton.setVisibility(View.GONE);
            holder.optionCheckBox.setVisibility(View.VISIBLE);
            holder.optionCheckBox.setChecked(selectedOptions.contains(menuItemModifierOption));
        } else {
            holder.optionCheckBox.setVisibility(View.GONE);
            holder.optionRadioButton.setVisibility(View.VISIBLE);
            holder.optionRadioButton.setChecked(selectedOptions.contains(menuItemModifierOption));
        }
        holder.optionItem.setOnClickListener(view -> {
            if (!selectMultiple) {
                //remove the previously selected option from selected options
                if (selectedOption != null) {
                    selectedOptions.remove(selectedOption);
                    onOptionClickListener.removeOptionPrice(selectedOption.getPrice());
                }
                //update selectedOption with the new selection
                selectedOption = menuItemModifierOption;
                //add the new selection to the list
                selectedOptions.add(menuItemModifierOption);
                onOptionClickListener.addOptionPrice(menuItemModifierOption.getPrice());

            } else {
                if (selectedOptions.contains(menuItemModifierOption)) {
                    selectedOptions.remove(menuItemModifierOption);
                    onOptionClickListener.removeOptionPrice(menuItemModifierOption.getPrice());
                } else {
                    selectedOptions.add(menuItemModifierOption);
                    onOptionClickListener.addOptionPrice(menuItemModifierOption.getPrice());
                }
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return menuItemModifierOptionList.size();
    }

    public interface OnOptionClickListener {
        void addOptionPrice(Double price);

        void removeOptionPrice(Double price);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.option_name)
        TextView optionName;

        @BindView(R.id.option_price)
        TextView optionPrice;

        @BindView(R.id.option_checkbox)
        CheckBox optionCheckBox;

        @BindView(R.id.option_radio)
        RadioButton optionRadioButton;

        @BindView(R.id.optionItem)
        LinearLayout optionItem;

        public ViewHolder(@NonNull @io.reactivex.annotations.NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
