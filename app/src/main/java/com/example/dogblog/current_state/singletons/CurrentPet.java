package com.example.dogblog.current_state.singletons;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dogblog.current_state.observers.PetMealsObserver;
import com.example.dogblog.current_state.observers.PetWalksObserver;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.model.PetProfile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CurrentPet {
    private static CurrentPet currentPet = null;
    private PetProfile petProfile = null;
    private final List<PetMealsObserver> mealsObservers = new ArrayList<>();
    private final List<PetWalksObserver> walksObservers = new ArrayList<>();

    private CurrentPet() {
    }

    public static CurrentPet getInstance() {
        if (currentPet == null)
            currentPet = new CurrentPet();
        return currentPet;
    }

    public PetProfile getPetProfile() {
        return petProfile;
    }

    public CurrentPet setPetProfile(PetProfile petProfile) {
        this.petProfile = petProfile;
        setMealsDataChangeListener();
        setWalksDataChangeListener();
        notifyMealsObservers();
        notifyWalksObservers();
        return this;
    }

    private void setMealsDataChangeListener() {
        if (petProfile != null) {
            DataCrud.getInstance().getPetReference(petProfile.getId()).child("meals").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    readPetData();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    readPetData();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    readPetData();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    readPetData();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void setWalksDataChangeListener() {
        if (petProfile != null) {
            DataCrud.getInstance().getPetReference(petProfile.getId()).child("walks").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    readPetData();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    readPetData();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    readPetData();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    readPetData();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void readPetData() {
        if (petProfile == null)
            return;

        DataCrud.getInstance().getPetReference(petProfile.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    petProfile = (snapshot.getValue(PetProfile.class));
                    notifyMealsObservers();
                    notifyWalksObservers();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public String getPetId() {
        return petProfile.getId();
    }

    public void addMealsObserver(PetMealsObserver observer) {
        mealsObservers.add(observer);
    }

    public void removeMealsObserver(PetMealsObserver observer) {
        mealsObservers.remove(observer);
    }

    private void notifyMealsObservers() {
        for (PetMealsObserver observer : mealsObservers) {
            observer.onMealsListChanged();
        }
    }

    public void addWalksObserver(PetWalksObserver observer) {
        walksObservers.add(observer);
    }

    public void removeWalksObserver(PetWalksObserver observer) {
        walksObservers.remove(observer);
    }

    private void notifyWalksObservers() {
        for (PetWalksObserver observer : walksObservers) {
            observer.onWalksListChanged();
        }
    }


}
