package com.example.dogblog.adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogblog.R;
import com.example.dogblog.callbacks.PetCurrentCallback;
import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.model.PetProfile;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class PetsCurrentAdapter extends RecyclerView.Adapter<PetsCurrentAdapter.PetViewHolder> {

    private final Fragment fragment;
    private List<PetProfile> petsList;
    private PetCurrentCallback petCurrentCallback;

    public PetsCurrentAdapter(Fragment fragment, List<PetProfile> petsList) {
        this.petsList = petsList;
        this.fragment = fragment;
    }

    public PetsCurrentAdapter setPetCurrentCallback(PetCurrentCallback petCurrentCallback) {
        this.petCurrentCallback = petCurrentCallback;
        return this;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_current_pet, parent, false);
        PetViewHolder petViewHolder = new PetViewHolder(view);
        return petViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        PetProfile pet = getItem(position);
        holder.top_item_TV_name.setText(pet.getName());
        Glide.
                with(fragment.getContext()).
                load(pet.getProfileImage()).
                into(holder.top_item_IMG_pet);
        holder.updateChosenPetView();
    }

    private PetProfile getItem(int position) {
        return petsList.get(position);
    }

    @Override
    public int getItemCount() {
        return petsList == null ? 0 : petsList.size();
    }

    public class PetViewHolder extends RecyclerView.ViewHolder {
        private ImageView top_item_IMG_pet;
        private MaterialTextView top_item_TV_name;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            itemView.setOnClickListener(view -> {
                petCurrentCallback.itemClicked(getItem(getAdapterPosition()), getAdapterPosition());
            });
        }

        private void initViews() {
            top_item_IMG_pet = itemView.findViewById(R.id.top_item_IMG_pet);
            top_item_TV_name = itemView.findViewById(R.id.top_item_TV_name);

        }

        public void updateChosenPetView() {
            boolean isChosen = getItem(getAdapterPosition()).getId().equals(CurrentPet.getInstance().getPetId());

            ViewGroup.LayoutParams params = top_item_IMG_pet.getLayoutParams();
            if (isChosen) {
                params.width = (int) fragment.getResources().getDimension(R.dimen.large_image_size);
                params.height = (int) fragment.getResources().getDimension(R.dimen.large_image_size);
                top_item_IMG_pet.setAlpha(1f);
                top_item_TV_name.setTextColor(fragment.getResources().getColor(R.color.white, fragment.getContext().getTheme()));

            } else {
                params.width = (int) fragment.getResources().getDimension(R.dimen.normal_image_size);
                params.height = (int) fragment.getResources().getDimension(R.dimen.normal_image_size);
                top_item_IMG_pet.setAlpha(0.75f);
                top_item_TV_name.setTextColor(fragment.getResources().getColor(R.color.white_alpha_75, fragment.getContext().getTheme()));
            }
            top_item_IMG_pet.setLayoutParams(params);
        }
    }
}