package com.example.dogblog.current_state.singletons;

import androidx.annotation.NonNull;

import com.example.dogblog.current_state.observers.UserPetsListObserver;
import com.example.dogblog.current_state.observers.UserProfileObserver;
import com.example.pawsome.dal.DataCrud;
import com.example.pawsome.model.PetProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrentUserPetsList implements UserProfileObserver {
    private static CurrentUserPetsList currentUserPetsList = null;
    private List<PetProfile> pets = new ArrayList<>();
    private final List<UserPetsListObserver> observers = new ArrayList<>();

    private boolean isPetsListLoaded = false;

    private CurrentUserPetsList() {
        CurrentUser.getInstance().registerListener(this);
//        getPetsData();
    }

    public static CurrentUserPetsList getInstance() {
        if (currentUserPetsList == null)
            currentUserPetsList = new CurrentUserPetsList();
        return currentUserPetsList;
    }

    public List<PetProfile> getPets() {
        removeDuplicates();
        return pets;
    }

    public void getPetsData() {
        if (CurrentUser.getInstance().getUserProfile().hasPets()) {
            isPetsListLoaded = false;
            pets.clear();
            for (String petId : CurrentUser.getInstance().getUserProfile().getPetsIds()) {
                DataCrud.getInstance().getPetReference(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            pets.add(snapshot.getValue(PetProfile.class));
                            if (pets.size() == CurrentUser.getInstance().getUserProfile().getPetsIds().size()) {
                                isPetsListLoaded = true;
                                notifyObservers();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        isPetsListLoaded = true;
                    }
                });
            }
        } else {
            isPetsListLoaded = true;
            notifyObservers();
        }
    }

    private void removeDuplicates() {
        Map<String, PetProfile> petProfileMap = pets.stream()
                .collect(Collectors.toMap(PetProfile::getId, petProfile -> petProfile, (oldValue, newValue) -> oldValue));

        pets = new ArrayList<>(petProfileMap.values());
    }

    public void registerListener(UserPetsListObserver observer) {
        observers.add(observer);
    }

    public void unregisterListener(UserPetsListObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (UserPetsListObserver observer : observers) {
            observer.onPetsListChanged();
        }
    }

    @Override
    public void onPetsListChanged() {
//        if (isPetsListLoaded)
            getPetsData();
    }
}
