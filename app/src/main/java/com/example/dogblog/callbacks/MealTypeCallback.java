package com.example.dogblog.callbacks;

import com.example.dogblog.model.MealType;

public interface MealTypeCallback {
    void removeClicked(MealType mealType, int position);
}