package com.example.dogblog.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogblog.callbacks.MealCallback;
import com.example.dogblog.R;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.model.Meal;
import com.example.dogblog.utils.DateTimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.Comparator;
import java.util.List;

public class MealsAdapter extends RecyclerView.Adapter<MealsAdapter.MealViewHolder> {
    private final Fragment fragment;
    private List<Meal> mealsList;
    private MealCallback mealCallback;

    public MealsAdapter(Fragment fragment, List<Meal> mealsList) {
        this.mealsList = mealsList;
        this.fragment = fragment;
    }

    public MealsAdapter setMealCallback(MealCallback mealCallback) {
        this.mealCallback = mealCallback;
        return this;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lst_meal, parent, false);
        MealViewHolder mealViewHolder = new MealViewHolder(view);
        return mealViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = getItem(position);
        holder.meal_TV_user.setText(meal.getOwner().getName());
        holder.meal_TV_time.setText(DateTimeConverter.longToStringTime(meal.getDateTime()));
        holder.meal_TV_date.setText(DateTimeConverter.longToStringDate(meal.getDateTime()));
        holder.meal_TV_note.setText(meal.getNote());
        holder.meal_TV_type.setText(meal.getName());
        holder.meal_TV_amount_units.setText(meal.getAmount() + " " + meal.getUnit());
        Glide.
                with(fragment.getContext()).
                load(meal.getOwner().getProfileImage()).
                into(holder.meal_IMG_user);

        if(meal.getOwner().getUid().equals(CurrentUser.getInstance().getUid()))
            holder.meal_CV_item.setStrokeWidth(5);
        else
            holder.meal_CV_item.setStrokeWidth(0);
    }

    private Meal getItem(int position) {
        return mealsList.get(position);
    }

    @Override
    public int getItemCount() {
        return mealsList == null ? 0 : mealsList.size();
    }

    public void updateMeals(List<Meal> mealsList) {
        this.mealsList = mealsList;
        notifyDataSetChanged();
    }

    public List<Meal> getMealsList() {
        mealsList.sort(Comparator.comparing(Meal::getDateTime).reversed());
        return mealsList;
    }

    public class MealViewHolder extends RecyclerView.ViewHolder {
        private ImageView meal_IMG_user;
        private MaterialTextView meal_TV_user;
        private MaterialTextView meal_TV_time;
        private MaterialTextView meal_TV_date;
        private MaterialTextView meal_TV_type;
        private MaterialTextView meal_TV_amount_units;
        private MaterialTextView meal_TV_note;
        private MaterialButton meal_BTN_delete;
        private MaterialCardView meal_CV_item;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            meal_BTN_delete.setOnClickListener(view -> mealCallback.deleteClicked(getItem(getAdapterPosition()), getAdapterPosition()));
        }

        private void initViews() {
            meal_IMG_user = itemView.findViewById(R.id.meal_IMG_user);
            meal_TV_user = itemView.findViewById(R.id.meal_TV_user);
            meal_TV_time = itemView.findViewById(R.id.meal_TV_time);
            meal_TV_date = itemView.findViewById(R.id.meal_TV_date);
            meal_TV_type = itemView.findViewById(R.id.meal_TV_type);
            meal_TV_amount_units = itemView.findViewById(R.id.meal_TV_amount_units);
            meal_TV_note = itemView.findViewById(R.id.meal_TV_note);
            meal_BTN_delete = itemView.findViewById(R.id.meal_BTN_delete);
            meal_CV_item = itemView.findViewById(R.id.meal_CV_item);
        }
    }
}
