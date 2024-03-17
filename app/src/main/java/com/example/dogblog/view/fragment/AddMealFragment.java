package com.example.dogblog.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.databinding.FragmentAddMealBinding;
import com.example.dogblog.model.Meal;
import com.example.dogblog.model.MealType;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.utils.Constants;
import com.example.dogblog.utils.DateTimeConverter;
import com.example.dogblog.view.activity.MainActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AddMealFragment extends Fragment {
    private FragmentAddMealBinding binding;
    private List<String> mealTypes;
    private MealType selectedMealType;
    private List<UserProfile> owners;
    private UserProfile selectedOwner;
    private String note;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddMealBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (CurrentPet.getInstance().getPetProfile() != null)
            updateFragmentData();

        return root;
    }

    private void updateFragmentData() {
        getMealsTypesList();
        initMealTypesSpinner();
        setMealTypeDataInView();
        setCurrentDateTimeSelected();

        getOwnersList();

        initListeners();
    }

    private void initListeners() {
        binding.addMealACTVMealType.setOnItemClickListener((parent, view, position, id) -> {
            this.selectedMealType = getSelectedMealType();
            setMealTypeDataInView();
        });

        binding.addMealACTVOwner.setOnItemClickListener((parent, view, position, id) -> {
            this.selectedOwner = (UserProfile) parent.getItemAtPosition(position);
        });

        binding.addMealBTNSave.setOnClickListener(v -> {
            saveMeal();
        });

        binding.addMealBTNTime.setOnClickListener(v -> setTime());
        binding.addMealBTNDate.setOnClickListener(v -> setDate());
        binding.addMealBTNAddNote.setOnClickListener(v -> showAddNote());
        binding.addMealCVNote.noteBTNCancel.setOnClickListener(v -> hideAddNote());
        binding.addMealCVNote.noteBTNSave.setOnClickListener(v -> saveNoteText());

        setTextChangedListeners();
    }



    private void showAddNote() {
        binding.addMealCVNote.getRoot().setVisibility(View.VISIBLE);
        disableMealFields();
    }

    private void hideAddNote() {
        binding.addMealCVNote.getRoot().setVisibility(View.GONE);
        enableMealFields();
    }

    private void saveNoteText() {
        note = binding.addMealCVNote.noteEDTNote.getEditText().getText().toString();
        hideAddNote();
    }

    private void disableMealFields() {
        binding.addMealTILMealType.setEnabled(false);
        binding.addMealBTNDate.setEnabled(false);
        binding.addMealBTNTime.setEnabled(false);
        binding.addMealEDTName.setEnabled(false);
        binding.addMealEDTAmount.setEnabled(false);
        binding.addMealEDTUnit.setEnabled(false);
        binding.addMealTILOwner.setEnabled(false);
        binding.addMealBTNAddNote.setEnabled(false);
        binding.addMealBTNSave.setEnabled(false);
    }

    private void enableMealFields() {
        binding.addMealTILMealType.setEnabled(true);
        binding.addMealBTNDate.setEnabled(true);
        binding.addMealBTNTime.setEnabled(true);
        binding.addMealEDTName.setEnabled(true);
        binding.addMealEDTAmount.setEnabled(true);
        binding.addMealEDTUnit.setEnabled(true);
        binding.addMealTILOwner.setEnabled(true);
        binding.addMealBTNAddNote.setEnabled(true);
        binding.addMealBTNSave.setEnabled(true);
    }

    private void initMealTypesSpinner() {
        if (this.mealTypes == null || this.mealTypes.isEmpty())
            binding.addMealTILMealType.setVisibility(View.GONE);
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, mealTypes);
            binding.addMealACTVMealType.setAdapter(adapter);
        }
    }

    private void initOwnersSpinner() {
        if (this.owners == null || this.owners.isEmpty())
            binding.addMealTILMealType.setVisibility(View.INVISIBLE);
        else {
            for (UserProfile owner : owners) {
                if(owner.getUid().equals(CurrentUser.getInstance().getUid()))
                    selectedOwner = owner;
            }
            if (selectedOwner == null)
                selectedOwner = owners.get(0);

            ArrayAdapter<UserProfile> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, owners);
            binding.addMealACTVOwner.setAdapter(adapter);
            binding.addMealTILOwner.setVisibility(View.VISIBLE);
            binding.addMealACTVOwner.setText(selectedOwner.getName(), false);
        }
    }

    private void setTime() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(LocalTime.now().getHour())
                .setMinute(LocalTime.now().getMinute())
                .setTitleText("Select meal time")
                .build();

        timePicker.show(getParentFragmentManager(), "tag");
        timePicker.addOnPositiveButtonClickListener(selection -> {
            LocalTime selectedTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            binding.addMealEDTTime.getEditText().setText(selectedTime.format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME)));
        });
    }

    private void setDate(){
        MaterialDatePicker datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
        datePicker.show(getParentFragmentManager(), "tag");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.FORMAT_DATE);
            binding.addMealEDTDate.getEditText().setText(simpleDateFormat.format(new Date((long)selection)));
        });
    }

    private MealType getSelectedMealType() {
        String selectedMealTypeName = binding.addMealACTVMealType.getText().toString();
        for (MealType mealType : CurrentPet.getInstance().getPetProfile().getMealTypes()) {
            if (mealType.getName().equals(selectedMealTypeName))
                return mealType;
        }
        return null;
    }

    private UserProfile getSelectedOwner() {
        String selectedOwnerName = binding.addMealACTVOwner.getText().toString();
        for (UserProfile owner : owners) {
            if (owner.getName().equals(selectedOwnerName))
                return owner;
        }

        return null;
    }

    private void setMealTypeDataInView() {
        if (this.selectedMealType != null) {
            if (selectedMealType.getName() != null && !selectedMealType.getName().isEmpty())
                binding.addMealEDTName.getEditText().setText(selectedMealType.getName());

            if (selectedMealType.getAmount() != 0)
                binding.addMealEDTAmount.getEditText().setText(String.valueOf(selectedMealType.getAmount()));

            if (selectedMealType.getUnit() != null && !selectedMealType.getUnit().isEmpty())
                binding.addMealEDTUnit.getEditText().setText(selectedMealType.getUnit());
        }

    }

    private void setCurrentDateTimeSelected() {
        binding.addMealEDTTime.getEditText().setText(LocalTime.now().format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME)));
        binding.addMealEDTDate.getEditText().setText(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.FORMAT_DATE)));
    }
    private void getMealsTypesList() {
        if (CurrentPet.getInstance().getPetProfile() != null)
            this.mealTypes = CurrentPet.getInstance().getPetProfile().getMealTypes().stream().map(MealType::getName).collect(Collectors.toList());

        this.mealTypes.add(Constants.MEAL_TYPE_REGULAR);
        this.mealTypes.add(Constants.MEAL_TYPE_TUNA);
        this.mealTypes.add(Constants.MEAL_TYPE_STEAK);
        this.mealTypes.add(Constants.MEAL_TYPE_OTHER);

    }

    private void getOwnersList() {
        binding.addMealTILOwner.setVisibility(View.INVISIBLE);

        if (CurrentPet.getInstance().getPetProfile() != null) {
            if (this.owners == null)
                this.owners = new ArrayList<>();

            for (String userId : CurrentPet.getInstance().getPetProfile().getOwnersIds()) {
                DataCrud.getInstance().getUserReference(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            owners.add(userProfile);
                            if (owners.size() == CurrentPet.getInstance().getPetProfile().getOwnersIds().size()) {
                                initOwnersSpinner();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
    }

    private void saveMeal() {
        if (validateMealFields() && CurrentPet.getInstance().getPetProfile() != null) {
            Meal meal = new Meal();

            String date = binding.addMealEDTDate.getEditText().getText().toString();
            String time = binding.addMealEDTTime.getEditText().getText().toString();
            LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            meal
                    .setName(binding.addMealEDTName.getEditText().getText().toString())
                    .setAmount(Integer.parseInt(binding.addMealEDTAmount.getEditText().getText().toString()))
                    .setUnit(binding.addMealEDTUnit.getEditText().getText().toString())
                    .setNote(note)
                    .setDateTime(DateTimeConverter.localDateTimeToLong(dateTime))
                    .setOwner(getSelectedOwner());


            CurrentPet.getInstance().getPetProfile().addMeal(meal);
            DataCrud.getInstance().setPetInDB(CurrentPet.getInstance().getPetProfile());

            ((MainActivity) getActivity()).selectMealLogFragmentOnMenu();
        }
    }

    private boolean validateMealFields() {
        if (binding.addMealEDTName.getEditText().getText().toString().isEmpty()) {
            binding.addMealEDTName.setError("Name is required!");
            return false;
        }
        if (binding.addMealEDTTime.getEditText().getText().toString().isEmpty()) {
            binding.addMealEDTTime.setError("Time is required!");
            return false;
        } 
        if (binding.addMealEDTDate.getEditText().getText().toString().isEmpty()) {
            binding.addMealEDTDate.setError("Date is required!");
            return false;
        }
        if (binding.addMealEDTAmount.getEditText().getText().toString().isEmpty()) {
            binding.addMealEDTAmount.setError("Amount is required!");
            return false;
        }
        if (binding.addMealEDTUnit.getEditText().getText().toString().isEmpty()) {
            binding.addMealEDTUnit.setError("Unit is required!");
            return false;
        }

        return true;
    }

    private void setTextChangedListeners() {
        binding.addMealEDTName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence name, int start, int before, int count) {
                if (!binding.addMealEDTName.getEditText().getText().toString().isEmpty())
                    binding.addMealEDTName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addMealEDTAmount.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence amount, int start, int before, int count) {
                if (!binding.addMealEDTAmount.getEditText().getText().toString().isEmpty())
                    binding.addMealEDTAmount.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addMealEDTUnit.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence unit, int start, int before, int count) {
                if (!binding.addMealEDTUnit.getEditText().getText().toString().isEmpty())
                    binding.addMealEDTUnit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addMealEDTTime.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.addMealEDTTime.getEditText().getText().toString().isEmpty())
                    binding.addMealEDTTime.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addMealEDTDate.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence date, int start, int before, int count) {
                if (!binding.addMealEDTDate.getEditText().getText().toString().isEmpty())
                    binding.addMealEDTDate.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
