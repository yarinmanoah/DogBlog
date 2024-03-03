package com.example.dogblog.callbacks;

import com.example.dogblog.model.PetProfile;

public interface PetListCallback {
    void deleteClicked(PetProfile pet, int position);
    void itemClicked(PetProfile pet, int position);
}
