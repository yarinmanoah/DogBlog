package com.example.dogblog.dal;

import com.example.dogblog.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseDB {
    private static FirebaseDB instance;
    private final FirebaseStorage storageDB;
    private final FirebaseDatabase realtimeDB;

    private FirebaseDB() {
        storageDB = FirebaseStorage.getInstance();
        realtimeDB = FirebaseDatabase.getInstance();
    }

    public static FirebaseDB getInstance() {
        if (instance == null) {
            instance = new FirebaseDB();
        }
        return instance;
    }

    public FirebaseStorage getStorageDB() {
        return storageDB;
    }

    public FirebaseDatabase getRealtimeDB() {
        return realtimeDB;
    }

    public DatabaseReference getDatabaseReference(String path) {
        return realtimeDB.getReference(path);
    }

    public StorageReference getStorageReference(String path) {
        return storageDB.getReference(path);
    }

    public DatabaseReference getUsersReference() {
        return getDatabaseReference(Constants.DB_USERS);
    }

    public DatabaseReference getPetsReference() {
        return getDatabaseReference(Constants.DB_PETS);
    }

}
