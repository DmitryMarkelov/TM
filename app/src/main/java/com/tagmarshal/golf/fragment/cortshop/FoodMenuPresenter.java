package com.tagmarshal.golf.fragment.cortshop;

import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.ActiveTimeDaysItemModel;
import com.tagmarshal.golf.rest.model.MenuGroup;
import com.tagmarshal.golf.rest.model.OrderItem;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FoodMenuPresenter implements FoodMenuContract.Presenter {

    private final CompositeDisposable disposable;
    private final FoodMenuFragmentModel model;
    List<ActiveTimeDaysItemModel> kitchenHours = new ArrayList<>();
    private FoodMenuContract.View view;

    public FoodMenuPresenter(FoodMenuContract.View foodMenuFragment) {
        view = foodMenuFragment;
        this.model = new FoodMenuFragmentModel();
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void attachView(FoodMenuContract.View view) {
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void getMenu() {
        disposable.add(
                model.getMenuGroup()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(__ -> view.showLoader(true))
                        .doOnTerminate(() -> view.showLoader(false))
                        .subscribe(menuList -> {
                                    List<MenuGroup> activeMenuGroup = getActiveMenuItems(menuList);
                                    if (activeMenuGroup.isEmpty()) {
                                        view.showClosedKitchenDialog();
                                    } else {
                                        view.setMenuData(activeMenuGroup, getOrderItems());
                                    }
                                },
                                e -> view.showError(e.getMessage())
                        )
        );
    }

    private List<MenuGroup> getActiveMenuItems(List<MenuGroup> menuGroups) {
        List<MenuGroup> activeMenuGroup = new ArrayList<>();
        for (MenuGroup menuGroup : menuGroups) {
            if (TMUtil.activeTimeDaysPass(menuGroup.getActiveTimeDays())) {
                activeMenuGroup.add(menuGroup);
            }
        }
        return activeMenuGroup;
    }

    @Override
    public List<OrderItem> getOrderItems() {
        return PreferenceManager.getInstance().getOrderItems();
    }

    @Override
    public void getKitchenHours() {
        if (kitchenHours.isEmpty()) {
            disposable.add(
                    model.getKitchenHours()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(__ -> view.showLoader(true))
                            .doOnTerminate(() -> view.showLoader(false))
                            .subscribe(kitchenHoursList -> {
                                        kitchenHours = kitchenHoursList;
                                        if (!TMUtil.activeTimeDaysPass(kitchenHoursList)) {
                                            view.showClosedKitchenDialog();
                                        } else {
                                            getMenu();
                                        }
                                    },
                                    e -> view.showError(e.getMessage())
                            )
            );
        } else {
            if (!TMUtil.activeTimeDaysPass(kitchenHours)) {
                view.showClosedKitchenDialog();
            } else {
                getMenu();
            }
        }
    }
}
