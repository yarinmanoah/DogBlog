package com.example.dogblog.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dogblog.adapters.MealsAdapter;
import com.example.dogblog.callbacks.MealCallback;
import com.example.dogblog.callbacks.PetListCallback;
import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.current_state.observers.PetMealsObserver;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.databinding.FragmentMealLogBinding;
import com.example.dogblog.model.Meal;
import com.example.dogblog.model.PetProfile;

import java.util.Comparator;
import java.util.List;

public class MealLogFragment extends Fragment implements PetMealsObserver {
    private FragmentMealLogBinding binding;
    private MealsAdapter mealsAdapter;
    private List<Meal> meals;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMealLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        CurrentPet.getInstance().addMealsObserver(this);

        if(CurrentPet.getInstance().getPetProfile() != null)
            updateFragmentData();

        return root;
    }

    private void updateFragmentData() {
        getMealsList();
        setMealsListView();
        initListeners();
    }

    private void initListeners() {
    }

    private void getMealsList() {
        if (CurrentPet.getInstance().getPetProfile() != null) {
            this.meals = CurrentPet.getInstance().getPetProfile().getMeals();
            this.meals.sort(Comparator.comparingLong(Meal::getDateTime).reversed());
        }
    }

    private void setMealsListView() {
        if (meals == null || meals.isEmpty()) {
            binding.mealsLogLSTMeals.setVisibility(View.GONE);
            binding.mealsLogTVEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.mealsLogLSTMeals.setVisibility(View.VISIBLE);
            binding.mealsLogTVEmpty.setVisibility(View.GONE);
        }

        mealsAdapter = new MealsAdapter(this, meals);
        binding.mealsLogLSTMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.mealsLogLSTMeals.setAdapter(mealsAdapter);
        setMealsCallbacks();
    }

    private void setMealsCallbacks() {
        mealsAdapter.setMealCallback(new MealCallback() {
            @Override
            public void itemClicked(Meal meal, int position) {
                // Do nothing for now, later on we will add the option to view the meal
            }

            @Override
            public void deleteClicked(Meal meal, int position) {
                deleteMeal(meal);
            }
        });
    }

    private void deleteMeal(Meal meal) {
        if(CurrentPet.getInstance().getPetProfile().removeMeal(meal)) {
            DataCrud.getInstance().setPetInDB(CurrentPet.getInstance().getPetProfile());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        CurrentPet.getInstance().removeMealsObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onMealsListChanged() {
        updateFragmentData();
    }
}