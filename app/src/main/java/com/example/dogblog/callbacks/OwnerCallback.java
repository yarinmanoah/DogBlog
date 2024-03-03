package com.example.dogblog.callbacks;

import com.example.dogblog.model.UserProfile;

public interface OwnerCallback {
    void deleteClicked(UserProfile user, int position);
}
