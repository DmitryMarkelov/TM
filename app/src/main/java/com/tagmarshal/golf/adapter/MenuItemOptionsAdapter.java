package com.tagmarshal.golf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.BaseActivity;
import com.tagmarshal.golf.rest.model.MenuItemModifier;
import com.tagmarshal.golf.rest.model.MenuItemModifierOption;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuItemOptionsAdapter extends RecyclerView.Adapter<MenuItemOptionsAdapter.ViewHolder> {
    private final List<MenuItemModifier> menuItemModifierList;
    private final Context context;
    private final OnMenuOptionClickListener onMenuOptionClickListener;
    private final List<MenuItemModifierOption> selectedOptions;
    private final BaseActivity baseActivity;
    private MenuItemOptionsItemAdapter menuItemOptionsItemAdapter;

    public MenuItemOptionsAdapter(List<MenuItemModifier> menuItemModifierList, List<MenuItemModifierOption> selectedOptions, Context context, OnMenuOptionClickListener onMenuOptionClickListener, BaseActivity baseActivity) {
        this.menuItemModifierList = menuItemModifierList;
        this.selectedOptions = selectedOptions;
        this.context = context;
        this.onMenuOptionClickListener = onMenuOptionClickListener;
        this.baseActivity = baseActivity;
    }

    @NonNull
    @io.reactivex.annotations.NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.menu_item_options, parent, false));
    }

    public boolean selectedOptionsValid() {
        if (menuItemOptionsItemAdapter != null) {
            List<MenuItemModifierOption> selectedOptions = menuItemOptionsItemAdapter.getSelectedOptions();
            for (MenuItemModifier modifier : menuItemModifierList) {
                if (modifier.isChoiceRequired()) {
                    boolean hasValidOption = false;
                    for (MenuItemModifierOption option : selectedOptions) {
                        if (modifier.getOptions().contains(option)) {
                            hasValidOption = true;
                            break;
                        }
                    }
                    if (!hasValidOption) {
                        Snackbar.make(baseActivity.findViewById(android.R.id.content),
                                "You are required to make a selection for " + modifier.getName(), Snackbar.LENGTH_LONG).show();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<MenuItemModifierOption> getSelectedOptions() {
        if (menuItemOptionsItemAdapter != null) {
            return menuItemOptionsItemAdapter.getSelectedOptions();
        }
        return new ArrayList<MenuItemModifierOption>();
    }

    @Override
    public void onBindViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewHolder holder, int position) {
        MenuItemModifier modifier = menuItemModifierList.get(position);
        menuItemOptionsItemAdapter = new MenuItemOptionsItemAdapter(modifier.getOptions(), selectedOptions, modifier.isSelectMultiple(), modifier.isChoiceRequired(), context, new MenuItemOptionsItemAdapter.OnOptionClickListener() {
            @Override
            public void addOptionPrice(Double price) {
                onMenuOptionClickListener.addOptionPrice(price);
            }

            @Override
            public void removeOptionPrice(Double price) {
                onMenuOptionClickListener.removeOptionPrice(price);
            }
        });
        holder.menuItemOptionTitle.setText(modifier.getName());
        holder.menuItemOptionRecyclerview.setLayoutManager(new LinearLayoutManager(context));
        holder.menuItemOptionRecyclerview.setAdapter(menuItemOptionsItemAdapter);

        if (modifier.isChoiceRequired()) {
            holder.requiredChip.setVisibility(View.VISIBLE);
        } else if (!modifier.isSelectMultiple()) {
            holder.chooseOneChip.setVisibility(View.VISIBLE);
        } else {
            holder.optionalChip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return menuItemModifierList.size();
    }

    public interface OnMenuOptionClickListener {
        void addOptionPrice(Double price);

        void removeOptionPrice(Double price);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.menu_item_options_title)
        TextView menuItemOptionTitle;

        @BindView(R.id.chooseOneChip)
        TextView chooseOneChip;

        @BindView(R.id.requiredChip)
        TextView requiredChip;

        @BindView(R.id.optionalChip)
        TextView optionalChip;

        @BindView(R.id.menu_item_options_recyclerview)
        RecyclerView menuItemOptionRecyclerview;

        public ViewHolder(@NonNull @io.reactivex.annotations.NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
