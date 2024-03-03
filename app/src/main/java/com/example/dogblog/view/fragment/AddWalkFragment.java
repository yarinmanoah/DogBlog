package com.example.dogblog.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dogblog.R;
import com.example.dogblog.current_state.singletons.CurrentPet;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.databinding.FragmentAddWalkBinding;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.model.Walk;
import com.example.dogblog.model.WalkType;
import com.example.dogblog.utils.Constants;
import com.example.dogblog.utils.DateTimeConverter;
import com.example.dogblog.view.activity.MainActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

public class AddWalkFragment extends Fragment {

    private FragmentAddWalkBinding binding;
    private List<String> walkTypes;
    private WalkType selectedWalkType;
    private List<UserProfile> owners;
    private UserProfile selectedOwner;
    private String note;
    private int duration_hours = 0;
    private int duration_minutes = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddWalkBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (CurrentPet.getInstance().getPetProfile() != null)
            updateFragmentData();

        return root;
    }

    private void updateFragmentData() {
        getWalksTypesList();
        initWalkTypesSpinner();
        setWalkTypeDataInView();
        setCurrentDateTimeSelected();

        getOwnersList();

        initListeners();
    }

    private void initListeners() {
        binding.addWalkACTVWalkType.setOnItemClickListener((parent, view, position, id) -> {
            this.selectedWalkType = getSelectedWalkType();
            setWalkTypeDataInView();
        });

        binding.addWalkACTVOwner.setOnItemClickListener((parent, view, position, id) -> this.selectedOwner = (UserProfile) parent.getItemAtPosition(position));
        binding.addWalkBTNSave.setOnClickListener(v -> saveWalk());
        binding.addWalkBTNDuration.setOnClickListener(v -> setDuration());
        binding.addWalkBTNTime.setOnClickListener(v -> setTime());
        binding.addWalkBTNDate.setOnClickListener(v -> setDate());
        binding.addWalkBTNAddNote.setOnClickListener(v -> showAddNote());
        binding.addWalkCVNote.noteBTNCancel.setOnClickListener(v -> hideAddNote());
        binding.addWalkCVNote.noteBTNSave.setOnClickListener(v -> saveNoteText());

        setTextChangedListeners();
    }

    private void showAddNote() {
        binding.addWalkCVNote.getRoot().setVisibility(View.VISIBLE);
        disableWalkFields();
    }

    private void hideAddNote() {
        binding.addWalkCVNote.getRoot().setVisibility(View.GONE);
        enableWalkFields();
    }

    private void saveNoteText() {
        note = binding.addWalkCVNote.noteEDTNote.getEditText().getText().toString();
        hideAddNote();
    }

    private void disableWalkFields() {
        binding.addWalkTILWalkType.setEnabled(false);
        binding.addWalkBTNDate.setEnabled(false);
        binding.addWalkBTNTime.setEnabled(false);
        binding.addWalkBTNDuration.setEnabled(false);
        binding.addWalkEDTName.setEnabled(false);
        binding.addWalkCBPee.setEnabled(false);
        binding.addWalkCBPoop.setEnabled(false);
        binding.addWalkCBPlay.setEnabled(false);
        binding.addWalkTILOwner.setEnabled(false);
        binding.addWalkBTNAddNote.setEnabled(false);
        binding.addWalkBTNSave.setEnabled(false);
        binding.addWalkSLDRate.setEnabled(false);
    }

    private void enableWalkFields() {
        binding.addWalkTILWalkType.setEnabled(true);
        binding.addWalkBTNDate.setEnabled(true);
        binding.addWalkBTNTime.setEnabled(true);
        binding.addWalkBTNDuration.setEnabled(true);
        binding.addWalkEDTName.setEnabled(true);
        binding.addWalkCBPee.setEnabled(true);
        binding.addWalkCBPoop.setEnabled(true);
        binding.addWalkCBPlay.setEnabled(true);
        binding.addWalkTILOwner.setEnabled(true);
        binding.addWalkBTNAddNote.setEnabled(true);
        binding.addWalkBTNSave.setEnabled(true);
        binding.addWalkSLDRate.setEnabled(true);
    }

    private void initWalkTypesSpinner() {
        if (this.walkTypes == null || this.walkTypes.isEmpty())
            binding.addWalkTILWalkType.setVisibility(View.GONE);
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, walkTypes);
            binding.addWalkACTVWalkType.setAdapter(adapter);
        }
    }

    private void initOwnersSpinner() {
        if (this.owners == null || this.owners.isEmpty())
            binding.addWalkTILWalkType.setVisibility(View.INVISIBLE);
        else {
            for (UserProfile owner : owners) {
                if(owner.getUid().equals(CurrentUser.getInstance().getUid()))
                    selectedOwner = owner;
            }
            if (selectedOwner == null)
                selectedOwner = owners.get(0);

            ArrayAdapter<UserProfile> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, owners);
            binding.addWalkACTVOwner.setAdapter(adapter);
            binding.addWalkTILOwner.setVisibility(View.VISIBLE);
            binding.addWalkACTVOwner.setText(selectedOwner.getName(), false);
        }
    }

    private void setTime() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(LocalTime.now().getHour())
                .setMinute(LocalTime.now().getMinute())
                .setTitleText("Select walk time")
                .build();

        timePicker.show(getParentFragmentManager(), "tag");
        timePicker.addOnPositiveButtonClickListener(selection -> {
            LocalTime selectedTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            binding.addWalkEDTTime.getEditText().setText(selectedTime.format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME)));
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
            binding.addWalkEDTDate.getEditText().setText(simpleDateFormat.format(new Date((long)selection)));
        });
    }

    private void setDuration() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTheme);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_duration_picker, null);

        NumberPicker npHours = view.findViewById(R.id.dialog_duration_picker_NP_hours);
        NumberPicker npMinutes = view.findViewById(R.id.dialog_duration_picker_NP_minutes);

        npHours.setMinValue(0);
        npHours.setMaxValue(23);
        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(59);

        builder.setView(view)
                .setTitle("Select Duration")
                .setPositiveButton("OK", (dialog, id) -> {
                    duration_hours = npHours.getValue();
                    duration_minutes = npMinutes.getValue();
                    binding.addWalkEDTDuration.getEditText().setText(DateTimeConverter.durationToString(duration_hours, duration_minutes));
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                })
                .show();
    }

    private WalkType getSelectedWalkType() {
        String selectedWalkTypeName = binding.addWalkACTVWalkType.getText().toString();
        for (WalkType walkType : CurrentPet.getInstance().getPetProfile().getWalkTypes()) {
            if (walkType.getName().equals(selectedWalkTypeName))
                return walkType;
        }
        return null;
    }

    private UserProfile getSelectedOwner() {
        String selectedOwnerName = binding.addWalkACTVOwner.getText().toString();
        for (UserProfile owner : owners) {
            if (owner.getName().equals(selectedOwnerName))
                return owner;
        }

        return null;
    }

    private void setWalkTypeDataInView() {
        if (this.selectedWalkType != null) {
            if (selectedWalkType.getName() != null && !selectedWalkType.getName().isEmpty())
                binding.addWalkEDTName.getEditText().setText(selectedWalkType.getName());

            if (selectedWalkType.getDurationInMinutes() != 0) {
                binding.addWalkEDTDuration.getEditText().setText(selectedWalkType.getDurationAsString());
                duration_hours = selectedWalkType.getDurationInMinutes() / 60;
                duration_minutes = selectedWalkType.getDurationInMinutes() % 60;
            }

            binding.addWalkCBPee.setChecked(selectedWalkType.getPee());
            binding.addWalkCBPoop.setChecked(selectedWalkType.getPoop());
            binding.addWalkCBPlay.setChecked(selectedWalkType.getPlay());
        }
    }

    private void setCurrentDateTimeSelected() {
        binding.addWalkEDTTime.getEditText().setText(LocalTime.now().format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME)));
        binding.addWalkEDTDate.getEditText().setText(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.FORMAT_DATE)));
    }

    private void getWalksTypesList() {
        if (CurrentPet.getInstance().getPetProfile() != null)
            this.walkTypes = CurrentPet.getInstance().getPetProfile().getWalkTypes().stream().map(WalkType::getName).collect(Collectors.toList());

        this.walkTypes.add(Constants.WALK_TYPE_OTHER);
    }

    private void getOwnersList() {
        binding.addWalkTILOwner.setVisibility(View.INVISIBLE);

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

    private void saveWalk() {
        if (validateWalkFields() && CurrentPet.getInstance().getPetProfile() != null) {
            Walk walk = new Walk();

            String date = binding.addWalkEDTDate.getEditText().getText().toString();
            String time = binding.addWalkEDTTime.getEditText().getText().toString();
            LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, DateTimeFormatter.ofPattern(Constants.FORMAT_DATE + " " + Constants.FORMAT_TIME));

            walk
                    .setName(binding.addWalkEDTName.getEditText().getText().toString())
                    .setDuration(duration_hours, duration_minutes)
                    .setPee(binding.addWalkCBPee.isChecked())
                    .setPoop(binding.addWalkCBPoop.isChecked())
                    .setPlay(binding.addWalkCBPlay.isChecked())
                    .setNote(note)
                    .setRate(binding.addWalkSLDRate.getValue())
                    .setDateTime(DateTimeConverter.localDateTimeToLong(dateTime))
                    .setOwner(getSelectedOwner());

            CurrentPet.getInstance().getPetProfile().addWalk(walk);
            DataCrud.getInstance().setPetInDB(CurrentPet.getInstance().getPetProfile());

            ((MainActivity) getActivity()).selectWalkLogFragmentOnMenu();
        }
    }

    private boolean validateWalkFields() {
        if (binding.addWalkEDTName.getEditText().getText().toString().isEmpty()) {
            binding.addWalkEDTName.setError("Name is required!");
            return false;
        }
        if (binding.addWalkEDTTime.getEditText().getText().toString().isEmpty()) {
            binding.addWalkEDTTime.setError("Time is required!");
            return false;
        } 
        if (binding.addWalkEDTDate.getEditText().getText().toString().isEmpty()) {
            binding.addWalkEDTDate.setError("Date is required!");
            return false;
        }
        if (binding.addWalkEDTDuration.getEditText().getText().toString().isEmpty()) {
            binding.addWalkEDTDuration.setError("Duration is required!");
            return false;
        }

        return true;
    }

    private void setTextChangedListeners() {
        binding.addWalkEDTName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence name, int start, int before, int count) {
                if (!binding.addWalkEDTName.getEditText().getText().toString().isEmpty())
                    binding.addWalkEDTName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addWalkEDTTime.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.addWalkEDTTime.getEditText().getText().toString().isEmpty())
                    binding.addWalkEDTTime.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addWalkEDTDate.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.addWalkEDTDate.getEditText().getText().toString().isEmpty())
                    binding.addWalkEDTDate.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addWalkEDTDuration.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.addWalkEDTDuration.getEditText().getText().toString().isEmpty())
                    binding.addWalkEDTDuration.setError(null);
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
