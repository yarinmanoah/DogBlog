package com.example.dogblog.dal;

import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.current_state.singletons.CurrentUserPetsList;
import com.example.dogblog.model.PetProfile;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.utils.Constants;
import com.google.firebase.database.DatabaseReference;

public class DataCrud {
    private static DataCrud instance;
    private final DatabaseReference usersDatabaseReference;
    private final DatabaseReference petsDatabaseReference;

    private DataCrud() {
        usersDatabaseReference = FirebaseDB.getInstance().getDatabaseReference(Constants.DB_USERS);
        petsDatabaseReference = FirebaseDB.getInstance().getDatabaseReference(Constants.DB_PETS);
    }

    public static DataCrud getInstance() {
        if (instance == null) {
            instance = new DataCrud();
        }
        return instance;
    }

    public void setUserInDB(UserProfile userProfile) {
        usersDatabaseReference.child(userProfile.getUid()).setValue(userProfile);
    }

    public void setPetInDB(PetProfile petProfile) {
        petsDatabaseReference.child(petProfile.getId()).setValue(petProfile);
        if(CurrentUser.getInstance().getUserProfile().isOwner(petProfile.getId()))
            CurrentUserPetsList.getInstance().getPetsData();
    }

    public DatabaseReference getUserReference(String uid) {
        return usersDatabaseReference.child(uid);
    }

    public DatabaseReference getPetReference(String id) {
        return petsDatabaseReference.child(id);
    }

    public void deletePetFromDB(String petId) {
        petsDatabaseReference.child(petId).removeValue();
        FilesCrud.getInstance().deletePetImageFromDB(petId);
    }

    public void deletePetFromUser(String userId, String petId) {
        DataCrud.getInstance().getUserReference(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getValue() != null) {
                    UserProfile user = task.getResult().getValue(UserProfile.class);
                    _deletePetFromUser(petId, user);
                }
            }
        });
    }

    private void _deletePetFromUser(String petId, UserProfile user) {
        user.removePet(petId);
        DataCrud.getInstance().setUserInDB(user);
    }
}
