package com.example.dogblog.callbacks;

import com.example.dogblog.model.Meal;
import com.example.dogblog.model.Walk;

public interface WalkCallback {
    void itemClicked(Walk walk, int position);
    void deleteClicked(Walk walk, int position);
}
