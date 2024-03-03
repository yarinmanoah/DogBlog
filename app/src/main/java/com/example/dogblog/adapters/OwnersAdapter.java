package com.example.dogblog.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogblog.R;
import com.example.dogblog.callbacks.OwnerCallback;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.model.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class OwnersAdapter extends RecyclerView.Adapter<OwnersAdapter.OwnerViewHolder> {

    private final AppCompatActivity activity;
    private List<UserProfile> owners;

    private OwnerCallback ownerCallback;


    public OwnersAdapter(AppCompatActivity activity, List<UserProfile> owners) {
        this.owners = owners;
        this.activity = activity;
    }

    public OwnersAdapter setOwnerCallback(OwnerCallback ownerCallback) {
        this.ownerCallback = ownerCallback;
        return this;
    }

    @NonNull
    @Override
    public OwnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lst_owner, parent, false);
        OwnerViewHolder ownerViewHolder = new OwnerViewHolder(view);
        return ownerViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerViewHolder holder, int position) {
        UserProfile owner = getItem(position);

        String ownerName = owner.getName();
        if(ownerName.equals(CurrentUser.getInstance().getUserProfile().getName())) {
            ownerName += " (You)";
            holder.owner_BTN_delete.setVisibility(View.GONE);
        }

        holder.owner_TV_name.setText(ownerName);
        Glide.
                with(activity).
                load(owner.getProfileImage()).
                into(holder.owner_IMG_user);
    }

    private UserProfile getItem(int position) {
        return owners.get(position);
    }

    @Override
    public int getItemCount() {
        return owners == null ? 0 : owners.size();
    }

    public class OwnerViewHolder extends RecyclerView.ViewHolder {
        private ImageView owner_IMG_user;
        private MaterialTextView owner_TV_name;
        private MaterialButton owner_BTN_delete;

        public OwnerViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            owner_BTN_delete.setOnClickListener(view -> ownerCallback.deleteClicked(getItem(getAdapterPosition()), getAdapterPosition()));
        }

        private void initViews() {
            owner_IMG_user = itemView.findViewById(R.id.owner_IMG_user);
            owner_TV_name = itemView.findViewById(R.id.owner_TV_name);
            owner_BTN_delete = itemView.findViewById(R.id.owner_BTN_delete);
        }
    }
}
