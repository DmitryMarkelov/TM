package com.tagmarshal.golf.rest.model;

import java.util.List;
import java.util.Objects;

public class MenuItemModifier
{
    private String id;
    private String name;
    private boolean selectMultiple;
    private boolean choiceRequired;
    private List<MenuItemModifierOption> options;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelectMultiple() {
        return selectMultiple;
    }

    public boolean isChoiceRequired() {
        return choiceRequired;
    }

    public List<MenuItemModifierOption> getOptions() {
        return options;
    }

    public void setOptions(List<MenuItemModifierOption> options) {
        this.options = options;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItemModifier modifier = (MenuItemModifier) o;
        return Objects.equals(id, modifier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
