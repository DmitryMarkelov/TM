package com.tagmarshal.golf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.BaseActivity;
import com.tagmarshal.golf.fragment.cortshop.order.UpdateOrderItemFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.MenuItemModifierOption;
import com.tagmarshal.golf.rest.model.OrderItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;
    private final List<OrderItem> orderItems;
    private final Context context;
    private final BaseActivity baseActivity;
    private final OnOrderItemRemovedListener orderItemRemovedListener;
    private FooterViewHolder footerViewHolder;

    public OrderAdapter(List<OrderItem> orderItems, Context context, BaseActivity baseActivity, OnOrderItemRemovedListener orderItemRemovedListener) {
        this.orderItems = orderItems;
        this.context = context;
        this.baseActivity = baseActivity;
        this.orderItemRemovedListener = orderItemRemovedListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == orderItems.size()) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public String getOrderNote() {
        if (footerViewHolder != null && footerViewHolder.orderNote != null) {
            return footerViewHolder.orderNote.getText().toString();
        }
        return "";
    }

    @NonNull
    @io.reactivex.annotations.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.order_item, parent, false));
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.order_note, parent, false);
            footerViewHolder = new FooterViewHolder(view);
            return footerViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @io.reactivex.annotations.NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {
            ViewHolder itemViewHolder = (ViewHolder) holder;
            OrderItem orderItem = orderItems.get(position);
            itemViewHolder.foodName.setText(orderItem.getName());
            itemViewHolder.foodPrice.setText(String.format("%s%.2f", PreferenceManager.getInstance().getMapBounds().getCurrency(), orderItem.getPrice()));
            itemViewHolder.itemCount.setText(String.valueOf(orderItem.getQuantity()));
            StringBuilder selectedOptionList = new StringBuilder();
            if (orderItem.getSelectedOptions() != null) {
                for (MenuItemModifierOption option : orderItem.getSelectedOptions()) {
                    selectedOptionList.append("\u2022").append(option.getName()).append("\n");
                }
                itemViewHolder.selectedOptions.setText(selectedOptionList.toString());
            }

            itemViewHolder.addItem.setOnClickListener(view -> {
                int quantity = orderItem.getQuantity() + 1;
                orderItem.setQuantity(quantity);
                PreferenceManager.getInstance().updateQuantity(orderItem, quantity);
                notifyItemChanged(position);
                orderItemRemovedListener.onItemRemoved(orderItems);
            });

            itemViewHolder.removeItem.setOnClickListener(view -> {
                if (orderItem.getQuantity() > 0) {
                    int quantity = orderItem.getQuantity() - 1;
                    orderItem.setQuantity(quantity);
                    if (orderItem.getQuantity() == 0) {
                        PreferenceManager.getInstance().removeItem(orderItem);
                        orderItems.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, orderItems.size());
                    } else {
                        PreferenceManager.getInstance().updateQuantity(orderItem, quantity);
                        notifyItemChanged(position);
                    }
                    orderItemRemovedListener.onItemRemoved(orderItems);
                }
            });

            itemViewHolder.orderItem.setOnClickListener(view -> {
                baseActivity.addFragment(UpdateOrderItemFragment.getInstance(orderItem), true);
            });
        } else if (holder instanceof FooterViewHolder) {

        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size() + 1;
    }

    public interface OnOrderItemRemovedListener {
        void onItemRemoved(List<OrderItem> orderItems);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.food_name_tv)
        TextView foodName;

        @BindView(R.id.food_price_tv)
        TextView foodPrice;

        @BindView(R.id.item_count_textview)
        TextView itemCount;

        @BindView(R.id.add_item_button)
        ImageButton addItem;

        @BindView(R.id.remove_item_button)
        ImageButton removeItem;

        @BindView(R.id.selectedOptionsTextView)
        TextView selectedOptions;

        @BindView(R.id.order_item)
        RelativeLayout orderItem;

        public ViewHolder(@NonNull @io.reactivex.annotations.NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.orderNote)
        EditText orderNote;

        public FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
