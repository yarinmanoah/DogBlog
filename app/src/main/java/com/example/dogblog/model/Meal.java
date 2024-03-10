package com.example.dogblog.model;

public class Meal {
    private UserProfile owner;
    private long dateTime;
    private String note;
    private MealType mealType;
    private String name;

    private int amount;
    private String unit;

    public Meal() {
    }

    public UserProfile getOwner() {
        return owner;
    }

    public Meal setOwner(UserProfile owner) {
        this.owner = owner;
        return this;
    }

    public long getDateTime() {
        return dateTime;
    }

    public Meal setDateTime(long dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Meal setNote(String note) {
        this.note = note;
        return this;
    }

    public MealType getMealType() {
        return mealType;
    }

    public Meal setMealType(MealType mealType) {
        this.mealType = mealType;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public Meal setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public Meal setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public String getName() {
        return name;
    }

    public Meal setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "owner=" + owner +
//                ", petId='" + petId + '\'' +
                ", dateTime=" + dateTime +
                ", note='" + note + '\'' +
                ", mealType=" + mealType +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", unit='" + unit + '\'' +
                '}';
    }
}
