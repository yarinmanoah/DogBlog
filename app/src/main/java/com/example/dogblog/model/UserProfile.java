package com.example.dogblog.model;

import com.example.dogblog.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String name;
    private String uid;
    private String profileImage;
    private String email;
    private String phoneNumber;
    private List<String> petsIds = new ArrayList<>();
    private boolean registered;

    public UserProfile() {
        this.profileImage = Constants.DEFAULT_USER_IMAGE_URL;
    }

    public UserProfile(String name, String uid, String email) {
        this.name = name;
        this.uid = uid;
        this.email = email;
        this.registered = false;
        this.profileImage = Constants.DEFAULT_USER_IMAGE_URL;
    }

    public String getName() {
        return name;
    }

    public UserProfile setName(String name) {
        this.name = name;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public UserProfile setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public UserProfile setProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserProfile setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserProfile setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public List<String> getPetsIds() {
        return petsIds;
    }

    public UserProfile setPetsIds(List<String> petsIds) {
        this.petsIds = petsIds;
        return this;
    }

    public boolean getRegistered() {
        return registered;
    }

    public UserProfile setRegistered(boolean registered) {
        this.registered = registered;
        return this;
    }

    public UserProfile deletePet(String petId) {
        this.petsIds.remove(petId);
        return this;
    }

    public boolean hasPets() {
        return !petsIds.isEmpty();
    }

    public UserProfile removePet(String id) {
        this.petsIds.remove(id);
        return this;
    }

    public boolean isOwner(String petId) {
        return petsIds.contains(petId);
    }

    @Override
    public String toString() {
        return this.name;
    }


}
