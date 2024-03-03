package com.example.dogblog.current_state.singletons;

import androidx.annotation.NonNull;

import com.example.dogblog.current_state.observers.PetMealsObserver;
import com.example.dogblog.current_state.observers.UserProfileObserver;
import com.example.pawsome.dal.DataCrud;
import com.example.pawsome.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CurrentUser {

    private static CurrentUser currentUser = null;
    private UserProfile userProfile = null;
    private final FirebaseUser user;
    private List<UserProfileObserver> observers = new ArrayList<>();

    private CurrentUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            getUserData(user.getUid());
        }
    }

    public static CurrentUser getInstance(){
        if (currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public CurrentUser setUserLoggedProfile() {
        getUserData(user.getUid());
        return this;
    }

    public CurrentUser setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        return this;
    }

    private void getUserData(String userId) {
        DataCrud.getInstance().getUserReference(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if(CurrentUser.getInstance().getUid().equals(snapshot.getValue(UserProfile.class).getUid())) {
                        userProfile = (snapshot.getValue(UserProfile.class));
                        getPetListData(userId);
                    }
                }
                else if(CurrentUser.getInstance().getUid().equals(snapshot.getValue(UserProfile.class).getUid()))
                        userProfile = null;
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getPetListData(String userId) {
        DataCrud.getInstance().getUserReference(userId).child("petsIds").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    notifyPetListChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public String getUid() {
        if(getUserProfile() != null)
            return getUserProfile().getUid();

        return null;
    }

    public void saveCurrentUserToDB() {
        DataCrud.getInstance().setUserInDB(this.userProfile);
    }

    @Override
    public String toString() {
        return "CurrentUser{" +
                "userProfile=" + userProfile +
                ", user=" + user +
                '}';
    }

    public void registerListener(UserProfileObserver observer) {
        observers.add(observer);
    }

    public void removeListener(PetMealsObserver observer) {
        observers.remove(observer);
    }

    private void notifyPetListChanged() {
        for (UserProfileObserver observer : observers) {
            observer.onPetsListChanged();
        }
    }

}
