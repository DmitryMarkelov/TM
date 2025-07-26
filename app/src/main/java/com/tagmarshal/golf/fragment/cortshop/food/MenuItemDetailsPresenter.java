package com.tagmarshal.golf.fragment.cortshop.food;

import com.tagmarshal.golf.rest.model.MenuItem;

public class MenuItemDetailsPresenter implements MenuItemDetailsContract.Presenter {

    MenuItemDetailsContract.View view;

    MenuItemDetailsPresenter(MenuItemDetailsContract.View view) {
        this.view = view;
    }

    @Override
    public void showMenuItemDetails(MenuItem menuItem) {
        view.updateUI(menuItem);
    }
}
