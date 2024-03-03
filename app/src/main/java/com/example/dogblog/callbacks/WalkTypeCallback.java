package com.example.dogblog.callbacks;

import com.example.dogblog.model.WalkType;

public interface WalkTypeCallback {
    void removeClicked(WalkType walkType, int position);
}