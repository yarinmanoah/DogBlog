package com.example.dogblog.model;

import com.example.dogblog.utils.Constants;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetProfile {
    private String id;
    private String name;
    private String profileImage;
    private String gender;
    private long dateOfBirth;
    private List<String> ownersIds;
    private List<MealType> mealTypes;
    private List<WalkType> walkTypes;
    private List<Meal> meals;
    private List<Walk> walks;

    public PetProfile() {
        this.id = UUID.randomUUID().toString();
        this.profileImage = Constants.DEFAULT_PET_IMAGE_URL;
        this.ownersIds = new ArrayList<>();
        this.mealTypes = new ArrayList<>();
        this.walkTypes = new ArrayList<>();
        this.meals = new ArrayList<>();
        this.walks = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public PetProfile setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PetProfile setName(String name) {
        this.name = name;
        return this;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public PetProfile setProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public PetProfile setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public List<String> getOwnersIds() {
        return ownersIds;
    }

    public PetProfile setOwnersIds(List<String> ownersIds) {
        this.ownersIds = ownersIds;
        if(ownersIds == null)
            this.ownersIds = new ArrayList<>();
        return this;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public PetProfile setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public List<MealType> getMealTypes() {
        return mealTypes;
    }

    public PetProfile setMealTypes(List<MealType> mealTypes) {
        this.mealTypes = mealTypes;
        if (mealTypes == null)
            this.mealTypes = new ArrayList<>();
        return this;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public PetProfile setMeals(List<Meal> meals) {
        this.meals = meals;
        if (meals == null)
            this.meals = new ArrayList<>();
        return this;
    }

    public List<WalkType> getWalkTypes() {
        return walkTypes;
    }

    public PetProfile setWalkTypes(List<WalkType> walkTypes) {
        this.walkTypes = walkTypes;
        if (walkTypes == null)
            this.walkTypes = new ArrayList<>();
        return this;
    }

    public List<Walk> getWalks() {
        return walks;
    }

    public PetProfile setWalks(List<Walk> walks) {
        this.walks = walks;
        if (walks == null)
            this.walks = new ArrayList<>();
        return this;
    }

    public PetProfile addOwner(String ownerId) {
        this.ownersIds.add(ownerId);
        return this;
    }

    public PetProfile removeOwner(String ownerId) {
        this.ownersIds.remove(ownerId);
        return this;
    }

    public void addMeal(Meal meal) {
        this.meals.add(meal);
    }

    public void addWalk(Walk walk) {
        this.walks.add(walk);
    }

    public boolean addMealType(MealType mealType) {
        if(this.mealTypes.contains(mealType))
            return false;

        return this.mealTypes.add(mealType);
    }

    public boolean addWalkType(WalkType walkType) {
        if(this.walkTypes.contains(walkType))
            return false;

        return this.walkTypes.add(walkType);
    }

    public boolean isOnlyOneOwner() {
        return this.ownersIds.size() == 1;
    }

    public boolean removeMealType(MealType mealType) {
        if(!this.mealTypes.contains(mealType))
            return false;

        return this.mealTypes.remove(mealType);
    }

    public boolean removeMeal(Meal meal) {
        if(!this.meals.contains(meal))
            return false;

        return this.meals.remove(meal);
    }

    public boolean removeWalkType(WalkType walkType) {
        if(!this.mealTypes.contains(walkType))
            return false;

        return this.mealTypes.remove(walkType);
    }

    public boolean removeWalk(Walk walk) {
        if(!this.walks.contains(walk))
            return false;

        return this.walks.remove(walk);
    }

    public boolean isContainsOwner(String ownerId) {
        return this.ownersIds.contains(ownerId);
    }

    @Override
    public String toString() {
        return "PetProfile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", ownersIds=" + ownersIds +
                ", mealTypes=" + mealTypes +
                ", meals=" + meals +
                '}';
    }
}