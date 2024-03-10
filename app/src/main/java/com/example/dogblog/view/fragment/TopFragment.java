package com.example.dogblog.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dogblog.adapters.PetsCurrentAdapter;
import com.example.dogblog.current_state.observers.UserPetsListObserver;
import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.current_state.singletons.CurrentUserPetsList;
import com.example.dogblog.databinding.FragmentTopBinding;
import com.example.dogblog.model.PetProfile;
import com.example.dogblog.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class TopFragment extends Fragment /* implements UserPetsListObserver */ {
    private FragmentTopBinding binding;
    private PetsCurrentAdapter petsCurrentAdapter;
    private List<PetProfile> pets = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initButtonsListeners();
        setUserNameView();
//        CurrentUserPetsList.getInstance().registerListener(this);

        if(CurrentUser.getInstance().getUserProfile().hasPets()) {
                pets = CurrentUserPetsList.getInstance().getPets();
                binding.topTVEmpty.setVisibility(View.GONE);
                initPetListView();
        }
        else
            binding.topTVEmpty.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initButtonsListeners() {
        binding.topBTNLogout.setOnClickListener(v -> ((MainActivity) getActivity()).signOut());
        binding.topBTNRefresh.setOnClickListener(v -> CurrentUserPetsList.getInstance().getPetsData());
    }

    private void initPetListView() {
        petsCurrentAdapter = new PetsCurrentAdapter(this, pets);
        binding.topLSTPets.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.topLSTPets.setAdapter(petsCurrentAdapter);
        setCurrentPetsListCallbacks();
    }

    private void setCurrentPetsListCallbacks() {
        petsCurrentAdapter.setPetCurrentCallback((pet, position) -> {
            petsCurrentAdapter.notifyDataSetChanged();
            CurrentPet.getInstance().setPetProfile(pet);
            ((MainActivity) getActivity()).selectHomeFragmentOnMenu();
        });
    }

    private void setUserNameView() {
        binding.topTVTitle.setText("Hello " + CurrentUser.getInstance().getUserProfile().getName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
//        CurrentUserPetsList.getInstance().unregisterListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

//    @Override
//    public void onPetsListChanged() {
//        pets = CurrentUserPetsList.getInstance().getPets();
//        initPetListView();
//    }

}