package com.tagmarshal.golf.rest.model;

import java.util.Objects;

public class MenuItemModifierOption
{
    private String id;
    private String name;
    private double price;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItemModifierOption that = (MenuItemModifierOption) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}