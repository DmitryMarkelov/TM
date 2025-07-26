package com.tagmarshal.golf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.fragment.cortshop.food.MenuItemDetailsFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.MenuItem;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.rest.model.RestBoundsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private final List<MenuItem> menuItemList;
    private final Context context;
    private List<OrderItem> orderItems;
    private MainActivity mainActivity;

    public MenuItemAdapter(List<MenuItem> menuItemList, Context context, MainActivity mainActivity, List<OrderItem> orderItems) {
        this.menuItemList = menuItemList;
        this.mainActivity = mainActivity;
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @io.reactivex.annotations.NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewHolder holder, int position) {
        MenuItem menuItem = menuItemList.get(position);
        RestBoundsModel mapBounds = PreferenceManager.getInstance().getMapBounds();
        holder.name.setText(menuItem.getName());
        holder.description.setText(menuItem.getDescription());
        holder.price.setText(String.format("%s%s", mapBounds.getCurrency(), menuItem.getPrice()));
        holder.circleItemCount.setVisibility(View.GONE);
        holder.addToCartButton.setVisibility(View.VISIBLE);

        int menuItemQuantity = 0;
        for (OrderItem item : orderItems) {
            if (item.getId().equalsIgnoreCase(menuItem.getId()) && item.getQuantity() > 0) {
                menuItemQuantity += item.getQuantity();
                holder.circleItemCount.setVisibility(View.VISIBLE);
                holder.circleItemCount.setText(String.valueOf(menuItemQuantity));
                holder.addToCartButton.setVisibility(View.GONE);
            }
        }
        holder.menuItem.setOnClickListener(view -> openMenuDetails(menuItem));
        holder.addToCartButton.setOnClickListener(view -> openMenuDetails(menuItem));
    }

    void openMenuDetails(MenuItem menuItem) {
        mainActivity.addFragment(MenuItemDetailsFragment.getInstance(menuItem), true);
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.food_name_tv)
        TextView name;

        @BindView(R.id.food_description_tv)
        TextView description;

        @BindView(R.id.food_price_tv)
        TextView price;

        @BindView(R.id.add_to_cart_button)
        ImageButton addToCartButton;

        @BindView(R.id.item_count_circle)
        TextView circleItemCount;

        @BindView(R.id.menu_item)
        RelativeLayout menuItem;

        public ViewHolder(@NonNull @io.reactivex.annotations.NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
