package com.tagmarshal.golf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.rest.model.MenuGroup;
import com.tagmarshal.golf.rest.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuGroupAdapter extends RecyclerView.Adapter<MenuGroupAdapter.ViewHolder> {

    private List<MenuGroup> menuGroupList = new ArrayList<>();
    private List<OrderItem> orderItemList = new ArrayList<>();
    private Context context;
    private MainActivity mainActivity;

    public MenuGroupAdapter(List<MenuGroup> menuGroupList, Context context, MainActivity baseActivity) {
        this.menuGroupList = menuGroupList;
        this.mainActivity = baseActivity;
        this.context = context;
    }

    public MenuGroupAdapter(Context context, MainActivity baseActivity) {
        this.context = context;
        this.mainActivity = baseActivity;
    }

    public void updateAdapterData(List<MenuGroup> menuGroups, List<OrderItem> orderItems) {
        this.menuGroupList = menuGroups;
        this.orderItemList = orderItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.food_catigorized_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @io.reactivex.annotations.NonNull ViewHolder holder, int position) {
        MenuGroup menuGroup = menuGroupList.get(position);
        holder.groupName.setText(menuGroup.getName());
        holder.groupItemsRecyclerview.setLayoutManager(new LinearLayoutManager(context));
        holder.groupItemsRecyclerview.setAdapter(new MenuItemAdapter(menuGroup.getItems(), context, mainActivity, orderItemList));
    }

    @Override
    public int getItemCount() {
        return menuGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.category_name)
        TextView groupName;

        @BindView(R.id.category_recycler)
        RecyclerView groupItemsRecyclerview;

        public ViewHolder(@NonNull @io.reactivex.annotations.NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
