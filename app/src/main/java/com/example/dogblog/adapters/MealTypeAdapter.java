package com.example.dogblog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogblog.R;
import com.example.dogblog.callbacks.MealTypeCallback;
import com.example.dogblog.model.MealType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class MealTypeAdapter extends RecyclerView.Adapter<MealTypeAdapter.MealTypeViewHolder> {

    private List<MealType> mealTypes;
    private MealTypeCallback mealTypeCallback;
    private Context context;

    public MealTypeAdapter(Context context, List<MealType> mealTypes) {
        this.context = context;
        this.mealTypes = mealTypes;
    }

    @NonNull
    @Override
    public MealTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lst_meal_type, parent, false);
        return new MealTypeViewHolder(view);
    }

    public MealTypeAdapter setMealTypeCallback(MealTypeCallback mealTypeCallback) {
        this.mealTypeCallback = mealTypeCallback;
        return this;
    }

    @Override
    public void onBindViewHolder(@NonNull MealTypeViewHolder holder, int position) {
        MealType mealType = mealTypes.get(position);
        holder.meal_type_TV_Name.setText(mealType.getName());
        holder.meal_type_BTN_delete.setOnClickListener(v -> {
            mealTypes.remove(position);
            notifyItemRemoved(position);
        });
    }

    private MealType getItem(int position) {
        return mealTypes.get(position);
    }

    @Override
    public int getItemCount() {
        return mealTypes.size();
    }

    public class MealTypeViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView meal_type_TV_Name;
        MaterialButton meal_type_BTN_delete;

        public MealTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            meal_type_TV_Name = itemView.findViewById(R.id.meal_type_TV_name);
            meal_type_BTN_delete = itemView.findViewById(R.id.meal_type_BTN_delete);
            meal_type_BTN_delete.setOnClickListener(view -> mealTypeCallback.removeClicked(getItem(getAdapterPosition()), getAdapterPosition()));
        }
    }
}
