package com.tagmarshal.golf.view.circularviewpager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public abstract class BaseCircularViewPagerAdapter<Item> extends FixedFragmentStatePagerAdapter {
    private List<Item> mItems;

    public BaseCircularViewPagerAdapter(final FragmentManager fragmentManager, final List<Item> items) {
        super(fragmentManager);
        mItems = items;
    }

    protected abstract Fragment getFragmentForItem(final Item item);

    @Override
    public Fragment getItem(final int position) {
        final int itemsSize = mItems.size();
        if(position == 0) {
            return getFragmentForItem(mItems.get(itemsSize - 1));
        } else if(position == itemsSize + 1) {
            return getFragmentForItem(mItems.get(0));
        } else {
            return getFragmentForItem(mItems.get(position - 1));
        }
    }

    @Override
    public int getCount() {
        final int itemsSize = mItems.size();
        return itemsSize > 1 ? itemsSize + 2 : itemsSize;
    }

    public int getCountWithoutFakePages() {
        return mItems.size();
    }

    public void setItems(final List<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }
}

