//package com.tagmarshal.golf.viewholder;
//
//import androidx.annotation.NonNull;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.constraintlayout.widget.ConstraintSet;
//import androidx.appcompat.widget.AppCompatImageView;
//import androidx.appcompat.widget.AppCompatSpinner;
//import androidx.appcompat.widget.AppCompatTextView;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Toast;
//
//import com.tagmarshal.golf.R;
//import com.tagmarshal.golf.adapter.FoodOptionsAdapter;
//import com.tagmarshal.golf.adapter.OnOptionClickListener;
//import com.tagmarshal.golf.eventbus.RefreshOrder;
//import com.tagmarshal.golf.manager.PreferenceManager;
//import com.tagmarshal.golf.rest.model.FoodModel;
//import com.tagmarshal.golf.rest.model.Order;
//import com.tagmarshal.golf.util.NotScrollingToFocuesChildrenLinearLayoutManager;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class FoodViewHolder extends RecyclerView.ViewHolder implements OnOptionClickListener {
//
//    private final boolean isFood;
//    @BindView(R.id.food_name_tv)
//    AppCompatTextView foodName;
//    @BindView(R.id.food_price_tv)
//    AppCompatTextView foodPrice;
//
//    @BindView(R.id.options_rv)
//    RecyclerView foodRecycler;
//
//    @BindView(R.id.option_spinner)
//    AppCompatSpinner optionalSpinner;
//
//    @BindView(R.id.option_view)
//    ConstraintLayout optionView;
//
//    @BindView(R.id.food_add_to_order_btn)
//    AppCompatImageView addToOrderBtn;
//
//    private Set<FoodModel.AdditionModel> selectedOptions;
//
//
//    private StringBuilder modelToOrder;
//
//     private String currency = PreferenceManager.getInstance().getCurrency();
//
//
//    private final int ANIMATION_DURATION = 500;
//
//
//    private FoodOptionsAdapter adapter;
//    private ArrayAdapter optionAdapter;
//    private LinearLayoutManager layoutManager;
//    private FoodModel foodModel;
//    private double currentPrice;
//
//    public FoodViewHolder(@NonNull View itemView, boolean isFood) {
//        super(itemView);
//        ButterKnife.bind(this, itemView);
//        this.isFood = isFood;
//        selectedOptions = new HashSet<>();
//    }
//
//    public void bindData(FoodModel food) {
//        this.foodModel = food;
//        foodName.setText(food.getName());
//        if(food.getPrice()!=null && !food.getPrice().equals("0")) {
//            foodPrice.setText(currency + food.getPrice());
//            foodPrice.setVisibility(View.VISIBLE);
//        }else{
//            foodPrice.setVisibility(View.GONE);
//        }
//        currentPrice = Double.parseDouble(food.getPrice());
//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(optionView);
//        if ((food.getAdditions() == null || food.getAdditions().isEmpty()) && (food.getOptions() == null || food.getOptions().isEmpty() )) {
//            constraintSet.connect(R.id.food_add_to_order_btn, ConstraintSet.TOP, optionView.getId(), ConstraintSet.TOP);
//            constraintSet.connect(R.id.food_add_to_order_btn, ConstraintSet.LEFT, optionView.getId(), ConstraintSet.LEFT);
//            constraintSet.clear(R.id.food_add_to_order_btn, ConstraintSet.BOTTOM);
//            constraintSet.applyTo(optionView);
//        } else {
//            constraintSet.connect(R.id.food_add_to_order_btn, ConstraintSet.TOP, optionalSpinner.getId(), ConstraintSet.TOP);
//            constraintSet.applyTo(optionView);
//        }
//        if (food.getAdditions() != null && !food.getAdditions().isEmpty()) {
//            foodRecycler.setVisibility(View.VISIBLE);
//            adapter = new FoodOptionsAdapter(food.getAdditions(), this);
//
//            layoutManager = new NotScrollingToFocuesChildrenLinearLayoutManager(itemView.getContext());
//            foodRecycler.setLayoutManager(layoutManager);
//            foodRecycler.setAdapter(adapter);
//        } else {
//            foodRecycler.setVisibility(View.GONE);
//        }
//        if (food.getOptions() != null && !food.getOptions().isEmpty()) {
//            optionalSpinner.setVisibility(View.VISIBLE);
//            optionAdapter = new ArrayAdapter<String>(itemView.getContext(), R.layout.tm_spinner_item, food.getOptions());
//            optionAdapter.setDropDownViewResource(R.layout.tm_drop_down_spinner_item);
//            optionalSpinner.setAdapter(optionAdapter);
//            optionalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            });
//        } else {
//            optionalSpinner.setVisibility(View.GONE);
//        }
//
//
//    }
//
//    @OnClick(R.id.layout_opener)
//    void onNameClick() {
//        if (optionView.getVisibility() == View.GONE) {
//            optionView.setVisibility(View.VISIBLE);
////            optionView.setAlpha(0.0f);
////            optionView.animate()
////                    .alpha(1f)
////                    .setDuration(ANIMATION_DURATION)
////                    .setListener(null);
//        } else {
//            optionView.setVisibility(View.GONE);
//
////            optionView.animate()
////                    .alpha(0f)
////                    .setDuration(ANIMATION_DURATION)
////                    .setListener(new AnimatorListenerAdapter() {
////                        @Override
////                        public void onAnimationEnd(Animator animation) {
////                            optionView.setVisibility(View.GONE);
////                        }
////                    });
//        }
//    }
//
//    @OnClick(R.id.food_add_to_order_btn)
//    void onAddOrderClick() {
//        Order order = new Order();
//        order.setName(foodModel.getName());
//        if(adapter != null && !getSelectedItems().isEmpty()){
//            order.setAdditions(getSelectedItems());
//        }
//        if(optionalSpinner.getSelectedItem()!=null) {
//            order.setOption(optionalSpinner.getSelectedItem().toString());
//        }
//        if(foodModel.getPrice()!=null) {
//            order.setPrice(foodModel.getPrice());
//        }
//        if (isFood) {
//            PreferenceManager.getInstance().saveFoodToOrder(order);
//        } else {
//            PreferenceManager.getInstance().saveDrinksToOrder(order);
//        }
//        Toast.makeText(itemView.getContext(), "Added", Toast.LENGTH_SHORT).show();
//        EventBus.getDefault().postSticky(new RefreshOrder());
//
//    }
//
//    private List<FoodModel.AdditionModel> getSelectedItems() {
//        return new ArrayList<>(selectedOptions);
//    }
//
//    @Override
//    public void onCheckItem(FoodModel.AdditionModel option) {
//        selectedOptions.add(option);
//        currentPrice += Double.parseDouble(option.getPrice());
//        foodPrice.setText(currency+String.format("%.2f", currentPrice).replace(",","."));
//    }
//
//    @Override
//    public void onUncheckItem(FoodModel.AdditionModel option) {
//        selectedOptions.remove(option.getName());
//        currentPrice -= Double.parseDouble(option.getPrice());
//        foodPrice.setText(currency+String.format("%.2f", currentPrice).replace(",","."));
//
//    }
//}
