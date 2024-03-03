package com.example.dogblog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawsome.R;
import com.example.pawsome.callbacks.WalkTypeCallback;
import com.example.pawsome.model.WalkType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class WalkTypeAdapter extends RecyclerView.Adapter<WalkTypeAdapter.WalkTypeViewHolder> {

    private List<WalkType> walkTypes;
    private WalkTypeCallback walkTypeCallback;
    private Context context;

    public WalkTypeAdapter(Context context, List<WalkType> walkTypes) {
        this.context = context;
        this.walkTypes = walkTypes;
    }

    @NonNull
    @Override
    public WalkTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lst_walk_type, parent, false);
        return new WalkTypeViewHolder(view);
    }

    public WalkTypeAdapter setWalkTypeCallback(WalkTypeCallback walkTypeCallback) {
        this.walkTypeCallback = walkTypeCallback;
        return this;
    }

    @Override
    public void onBindViewHolder(@NonNull WalkTypeViewHolder holder, int position) {
        WalkType walkType = walkTypes.get(position);
        holder.walk_type_TV_Name.setText(walkType.getName());
        holder.walk_type_BTN_delete.setOnClickListener(v -> {
            walkTypes.remove(position);
            notifyItemRemoved(position);
        });
    }

    private WalkType getItem(int position) {
        return walkTypes.get(position);
    }

    @Override
    public int getItemCount() {
        return walkTypes.size();
    }

    public class WalkTypeViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView walk_type_TV_Name;
        MaterialButton walk_type_BTN_delete;

        public WalkTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            walk_type_TV_Name = itemView.findViewById(R.id.walk_type_TV_name);
            walk_type_BTN_delete = itemView.findViewById(R.id.walk_type_BTN_delete);
            walk_type_BTN_delete.setOnClickListener(view -> walkTypeCallback.removeClicked(getItem(getAdapterPosition()), getAdapterPosition()));
        }
    }
}
