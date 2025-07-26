package com.tagmarshal.golf.rest.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Objects;

@SuppressLint("ParcelCreator")
public class OrderItem implements Parcelable {
    private String id;
    private String name;
    private String description;
    private double price;
    private List<MenuItemModifier> modifiers;
    private String instructions;
    private int quantity;
    private List<MenuItemModifierOption> selectedOptions;

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

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<MenuItemModifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<MenuItemModifier> modifiers) {
        this.modifiers = modifiers;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getQuantity() {
        return quantity;
    }
    public String getInstructions(){return  instructions;}

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<MenuItemModifierOption> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(List<MenuItemModifierOption> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeDouble(price);
        parcel.writeList(modifiers);
        parcel.writeString(instructions);
        parcel.writeInt(quantity);
        parcel.writeList(selectedOptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem item = (OrderItem) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
