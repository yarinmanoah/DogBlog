package com.example.dogblog.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogblog.R;
import com.example.dogblog.callbacks.WalkCallback;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.model.Walk;
import com.example.dogblog.utils.DateTimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.Comparator;
import java.util.List;

public class WalksAdapter extends RecyclerView.Adapter<WalksAdapter.WalkViewHolder> {
    private final Fragment fragment;
    private List<Walk> WalksList;
    private WalkCallback walkCallback;

    public WalksAdapter(Fragment fragment, List<Walk> WalksList) {
        this.WalksList = WalksList;
        this.fragment = fragment;
    }

    public WalksAdapter setWalkCallback(WalkCallback walkCallback) {
        this.walkCallback = walkCallback;
        return this;
    }

    @NonNull
    @Override
    public WalkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lst_walk, parent, false);
        WalkViewHolder walkViewHolder = new WalkViewHolder(view);
        return walkViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WalkViewHolder holder, int position) {
        Walk walk = getItem(position);
        holder.walk_TV_user.setText(walk.getOwner().getName());
        holder.walk_TV_time.setText(DateTimeConverter.longToStringTime(walk.getDateTime()));
        holder.walk_TV_date.setText(DateTimeConverter.longToStringDate(walk.getDateTime()));
        holder.walk_TV_note.setText(walk.getNote());
        holder.walk_TV_duration.setText(walk.getName() + ", " + walk.getDurationInMinutes() + " min");
        holder.walk_TV_rate.setText((int)walk.getRate() + "/5");
        holder.walk_IMG_poop.setVisibility(walk.getPoop() ? View.GONE : View.VISIBLE);
        holder.walk_IMG_play.setVisibility(walk.getPlay() ? View.VISIBLE : View.GONE);
        Glide.
                with(fragment.getContext()).
                load(walk.getOwner().getProfileImage()).
                into(holder.walk_IMG_user);

        if(walk.getOwner().getUid().equals(CurrentUser.getInstance().getUid()))
            holder.walk_CV_item.setStrokeWidth(5);
        else
            holder.walk_CV_item.setStrokeWidth(0);
    }

    private Walk getItem(int position) {
        return WalksList.get(position);
    }

    @Override
    public int getItemCount() {
        return WalksList == null ? 0 : WalksList.size();
    }

    public void updateWalks(List<Walk> walksList) {
        this.WalksList = walksList;
        notifyDataSetChanged();
    }

    public List<Walk> getWalksList() {
        WalksList.sort(Comparator.comparing(Walk::getDateTime).reversed());
        return WalksList;
    }

    public class WalkViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView walk_CV_item;
        private ImageView walk_IMG_user;
        private MaterialTextView walk_TV_user;
        private MaterialTextView walk_TV_time;
        private MaterialTextView walk_TV_date;
        private MaterialTextView walk_TV_duration;
        private MaterialTextView walk_TV_rate;
        private ImageView walk_IMG_poop;
        private ImageView walk_IMG_play;
        private MaterialTextView walk_TV_note;
        private MaterialButton walk_BTN_delete;

        public WalkViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            walk_BTN_delete.setOnClickListener(view -> walkCallback.deleteClicked(getItem(getAdapterPosition()), getAdapterPosition()));
//            itemView.setOnClickListener(view -> walkCallback.itemClicked(getItem(getAdapterPosition()), getAdapterPosition()));
        }

        private void initViews() {
            walk_CV_item = itemView.findViewById(R.id.walk_CV_item);
            walk_IMG_user = itemView.findViewById(R.id.walk_IMG_user);
            walk_TV_user = itemView.findViewById(R.id.walk_TV_user);
            walk_TV_time = itemView.findViewById(R.id.walk_TV_time);
            walk_TV_date = itemView.findViewById(R.id.walk_TV_date);
            walk_TV_duration = itemView.findViewById(R.id.walk_TV_duration);
            walk_TV_rate = itemView.findViewById(R.id.walk_TV_rate);
            walk_IMG_poop = itemView.findViewById(R.id.walk_IMG_poop);
            walk_IMG_play = itemView.findViewById(R.id.walk_IMG_play);
            walk_TV_note = itemView.findViewById(R.id.walk_TV_note);
            walk_BTN_delete = itemView.findViewById(R.id.walk_BTN_delete);
        }
    }
}
