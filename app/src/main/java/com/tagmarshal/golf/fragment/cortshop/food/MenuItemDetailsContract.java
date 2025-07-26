package com.tagmarshal.golf.fragment.cortshop.food;

import com.tagmarshal.golf.rest.model.FoodModel;
import com.tagmarshal.golf.rest.model.MenuItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

interface MenuItemDetailsContract {
    interface View {
        void showError(String error);
        void updateUI(MenuItem item);

        void showLoader(boolean show);
    }

    interface Presenter {

        void showMenuItemDetails(MenuItem menuItem);

    }

}
