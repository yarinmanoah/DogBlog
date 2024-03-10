package com.example.dogblog.model;

import com.example.dogblog.utils.Constants;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MealType {
    private long time;
    private int amount;
    private String unit;
    private String name;

    public MealType() {
    }

    public long getTime() {
        return time;
    }

    public MealType setTime(long time) {
        this.time = time;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public MealType setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public MealType setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public String getName() {
        return name;
    }

    public MealType setName(String name) {
        this.name = name;
        return this;
    }

    public String getTimeAsString() {
        LocalTime time = LocalTime.ofSecondOfDay((int)this.time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.FORMAT_TIME);
        return formatter.format(time);
    }

    public MealType setTimeFromString(String timeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.FORMAT_TIME);
        this.time = LocalTime.parse(timeString, formatter).toSecondOfDay();
        return this;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealType mealType = (MealType) o;
        return Objects.equals(name, mealType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}