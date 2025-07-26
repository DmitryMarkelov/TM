package com.tagmarshal.golf.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class FoodModel implements Parcelable {

    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private String price ="0";
    @SerializedName("category")
    private String category;
    @SerializedName("active")
    private boolean active;
    @SerializedName("options")
    private List<String> options;
    @SerializedName("additions")
    private List<AdditionModel> additions;


    protected FoodModel(Parcel in) {
        name = in.readString();
        price = in.readString();
        category = in.readString();
        active = in.readByte() != 0;
        options = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(category);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeStringList(options);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FoodModel> CREATOR = new Creator<FoodModel>() {
        @Override
        public FoodModel createFromParcel(Parcel in) {
            return new FoodModel(in);
        }

        @Override
        public FoodModel[] newArray(int size) {
            return new FoodModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<AdditionModel> getAdditions() {
        return additions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAdditions(List<AdditionModel> additions) {
        this.additions = additions;
    }

    public String getPrice() {
        return price ==null ? "0.00" : price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public class AdditionModel {
        @SerializedName("name")
        private String name;
        @SerializedName("price")
        private String price ="0";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrice() {
            return price ==null ? "0" : price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) return true;
            if (!(o instanceof AdditionModel)) {
                return false;
            }
            AdditionModel addition = (AdditionModel) o;
            return name.equals(addition.name) &&
                    Objects.equals(name, addition.name) &&
                    Objects.equals(price, addition.price)

                    ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, price);
        }
    }
}
