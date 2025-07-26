package com.tagmarshal.golf.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Objects;

public class MenuItem implements Parcelable {
    private String id;
    private String name;
    private String description;
    private double price;
    private List<MenuItemModifier> modifiers;

    protected MenuItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
    }

    public static final Creator<MenuItem> CREATOR = new Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel in) {
            return new MenuItem(in);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public List<MenuItemModifier> getModifiers() {
        return modifiers;
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem item = (MenuItem) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}