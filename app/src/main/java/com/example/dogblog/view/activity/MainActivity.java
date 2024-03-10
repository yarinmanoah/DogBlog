package com.example.dogblog.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;

import com.example.dogblog.R;
import com.example.dogblog.current_state.observers.UserPetsListObserver;
import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.current_state.singletons.CurrentUserPetsList;
import com.example.dogblog.databinding.ActivityMainBinding;
import com.example.dogblog.utils.Constants;
import com.example.dogblog.view.fragment.AddMealFragment;
import com.example.dogblog.view.fragment.AddWalkFragment;
import com.example.dogblog.view.fragment.MealLogFragment;
import com.example.dogblog.view.fragment.HomeFragment;
import com.example.dogblog.view.fragment.SettingsFragment;
import com.example.dogblog.view.fragment.TopFragment;
import com.example.dogblog.view.fragment.WalkLogFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements UserPetsListObserver {
    private static final int mainFragmentLocation = R.id.main_FRAME_fragments;
    private static final int topFragmentLocation = R.id.main_FRAME_top;
    private static final int menu_home = R.id.menu_FRG_home;
    private static final int menu_meals = R.id.menu_FRG_meals;
    private static final int menu_add = R.id.menu_FRG_add;
    private static final int menu_walks = R.id.menu_FRG_walks;
    private static final int menu_settings = R.id.menu_FRG_settings;
    private boolean isActivityVisible = true;
    private boolean isPetsListChangedPending = false;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CurrentUserPetsList.getInstance().registerListener(this);
        setBottomNaviMenuListener();

        if(CurrentUser.getInstance().getUserProfile().hasPets()) {
            CurrentUserPetsList.getInstance().getPetsData();
        }
        else {
            setNoPets();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;
        if(isPetsListChangedPending) {
            isPetsListChangedPending = false;
            petsListChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
    }

    private void setBottomNaviMenuListener() {
        binding.mainBNVMenu.setOnItemSelectedListener(item -> {
            if (item.getItemId() == menu_home) {
                replaceFragment(HomeFragment.class, mainFragmentLocation);
            } else if (item.getItemId() == menu_meals) {
                replaceFragment(MealLogFragment.class, mainFragmentLocation);
            } else if (item.getItemId() == menu_add) {
                setAddDialog();
            } else if (item.getItemId() == menu_walks) {
                replaceFragment(WalkLogFragment.class, mainFragmentLocation);
            } else if (item.getItemId() == menu_settings) {
                replaceFragment(SettingsFragment.class, mainFragmentLocation);
            } else {
                replaceFragment(HomeFragment.class, mainFragmentLocation);
            }
            return true;
        });
    }

    private void setTopFragment() {
        replaceFragment(TopFragment.class, topFragmentLocation);
    }

    public void replaceFragment(Class fragmentClass, int fragmentLocation) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(fragmentLocation, fragment)
                .commit();
    }

    public void selectHomeFragmentOnMenu() {
        binding.mainBNVMenu.setSelectedItemId(menu_home);
    }
    public void selectMealLogFragmentOnMenu() {
        binding.mainBNVMenu.setSelectedItemId(menu_meals);
    }
    public void selectWalkLogFragmentOnMenu() {
        binding.mainBNVMenu.setSelectedItemId(menu_walks);
    }
    public void selectSettingsFragmentOnMenu() {
        binding.mainBNVMenu.setSelectedItemId(menu_settings);
    }

    private void setAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setNeutralButton("Meal", (dialog, which) -> replaceFragment(AddMealFragment.class, mainFragmentLocation));
        builder.setNegativeButton("Walk", (dialog, which) -> replaceFragment(AddWalkFragment.class, mainFragmentLocation));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

//    private void setNoPets() {
//        binding.mainBNVMenu.setEnabled(false);
//        setTopFragment();
//    }

    private void setNoPets() {
        for (int i = 0; i < binding.mainBNVMenu.getMenu().size(); i++) {
            MenuItem menuItem = binding.mainBNVMenu.getMenu().getItem(i);
            menuItem.setEnabled(menuItem.getItemId() == menu_settings);
        }
        setTopFragment();
        binding.mainBNVMenu.setSelectedItemId(menu_settings);
    }


    public void goToProfileActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.KEY_NEW_USER, false);
        bundle.putBoolean(Constants.KEY_FROM_MAIN, true);
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void goToPetProfileActivity(String petId) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PET_ID, petId);
        bundle.putBoolean(Constants.KEY_NEW_PET, petId == null || petId.isEmpty());
        bundle.putBoolean(Constants.KEY_FROM_MAIN, true);

        Intent intent = new Intent(this, PetProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void goToPetProfileActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.KEY_NEW_PET, true);
        bundle.putBoolean(Constants.KEY_FROM_MAIN, true);

        Intent intent = new Intent(this, PetProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void signOut() {
        CurrentPet.getInstance().setPetProfile(null);
        CurrentUser.getInstance().setUserProfile(null);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void petsListChanged() {
        if(!CurrentUser.getInstance().getUserProfile().hasPets()) {
            setNoPets();
        }
        else {
            if(CurrentPet.getInstance().getPetProfile() == null) {
                CurrentPet.getInstance().setPetProfile(CurrentUserPetsList.getInstance().getPets().get(0));
            }
            else if(!CurrentPet.getInstance().getPetProfile().isContainsOwner(CurrentUser.getInstance().getUid())) {
                CurrentPet.getInstance().setPetProfile(CurrentUserPetsList.getInstance().getPets().get(0));
            }

            setTopFragment();
            binding.mainBNVMenu.setSelectedItemId(binding.mainBNVMenu.getSelectedItemId());
//            binding.mainBNVMenu.setSelectedItemId(menu_home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CurrentUserPetsList.getInstance().unregisterListener(this);
    }

    @Override
    public void onPetsListChanged() {
        if(isActivityVisible)
            petsListChanged();
        else
            isPetsListChangedPending = true;
    }
}