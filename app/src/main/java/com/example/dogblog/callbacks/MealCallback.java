package com.example.dogblog.callbacks;

import com.example.dogblog.model.Meal;

public interface MealCallback {
    void itemClicked(Meal meal, int position);
    void deleteClicked(Meal meal, int position);
}
