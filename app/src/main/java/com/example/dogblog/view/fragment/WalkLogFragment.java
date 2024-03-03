package com.example.dogblog.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dogblog.adapters.WalksAdapter;
import com.example.dogblog.callbacks.WalkCallback;
import com.example.dogblog.current_state.observers.PetWalksObserver;
import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.databinding.FragmentWalkLogBinding;
import com.example.dogblog.model.Meal;
import com.example.dogblog.model.Walk;

import java.util.Comparator;
import java.util.List;

public class WalkLogFragment extends Fragment implements PetWalksObserver {
    private FragmentWalkLogBinding binding;
    private WalksAdapter walksAdapter;
    private List<Walk> walks;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWalkLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        CurrentPet.getInstance().addWalksObserver(this);

        if(CurrentPet.getInstance().getPetProfile() != null)
            updateFragmentData();

        return root;
    }


    private void updateFragmentData() {
        getMealsList();
        setWalksListView();
        initListeners();
    }

    private void initListeners() {
    }

    private void getMealsList() {
        if (CurrentPet.getInstance().getPetProfile() != null) {
            this.walks = CurrentPet.getInstance().getPetProfile().getWalks();
            this.walks.sort(Comparator.comparingLong(Walk::getDateTime).reversed());
        }
    }

    private void setWalksListView() {
        if (walks == null || walks.isEmpty()) {
            binding.walksLogLSTWalks.setVisibility(View.GONE);
            binding.walksLogTVEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.walksLogLSTWalks.setVisibility(View.VISIBLE);
            binding.walksLogTVEmpty.setVisibility(View.GONE);
        }

        walksAdapter = new WalksAdapter(this, walks);
        binding.walksLogLSTWalks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.walksLogLSTWalks.setAdapter(walksAdapter);
        setWalksCallbacks();
    }

    private void setWalksCallbacks() {
        walksAdapter.setWalkCallback(new WalkCallback() {
            @Override
            public void itemClicked(Walk walk, int position) {
                // Do nothing for now, later on we will add the option to view the meal
            }

            @Override
            public void deleteClicked(Walk walk, int position) {
                deleteWalk(walk);
            }
        });
    }

    private void deleteWalk(Walk walk) {
        if(CurrentPet.getInstance().getPetProfile().removeWalk(walk)) {
            DataCrud.getInstance().setPetInDB(CurrentPet.getInstance().getPetProfile());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        CurrentPet.getInstance().removeWalksObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onWalksListChanged() {
        updateFragmentData();
    }
}