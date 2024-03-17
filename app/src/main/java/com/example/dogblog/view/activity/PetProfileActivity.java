package com.example.dogblog.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.dogblog.R;
import com.example.dogblog.adapters.MealTypeAdapter;
import com.example.dogblog.adapters.OwnersAdapter;
import com.example.dogblog.adapters.WalkTypeAdapter;
import com.example.dogblog.current_state.singletons.CurrentUserPetsList;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.dal.FilesCrud;
import com.example.dogblog.dal.FirebaseDB;
import com.example.dogblog.databinding.ActivityPetProfileBinding;
import com.example.dogblog.model.MealType;
import com.example.dogblog.model.PetProfile;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.model.WalkType;
import com.example.dogblog.utils.Constants;
import com.example.dogblog.utils.DateTimeConverter;
import com.example.dogblog.utils.SignalUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PetProfileActivity extends AppCompatActivity {
    private static final String WALK = "Walk";
    private static final String MEAL = "Meal";
    private ActivityPetProfileBinding binding;
    private MealTypeAdapter mealTypeAdapter;
    private WalkTypeAdapter walkTypeAdapter;
    private OwnersAdapter ownersAdapter;
    private static final int IMAGE_UPLOAD_REQUEST_CODE = 1;
    private Uri imageUri;
    String imageUrl;
    private String fileName;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isImageUploaded = true;
    private PetProfile petProfile;
    private String petId;
    private boolean isNewPet = false;
    private boolean isFromActivityMain = false;
    private int walk_duration_hours = 0;
    private int walk_duration_minutes = 0;
    private List<UserProfile> owners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isNewPet = getIntent().getBooleanExtra(Constants.KEY_NEW_PET, true);
        isFromActivityMain = getIntent().getBooleanExtra(Constants.KEY_FROM_MAIN, true);
        if (!isNewPet) {
            petId = getIntent().getStringExtra(Constants.KEY_PET_ID);
            if (petId == null || petId.isEmpty())
                errorGetPetData();

            loadPetData();
        } else
            initPetProfile();

        initView();

        isImageUploaded = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null || CurrentUser.getInstance().getUserProfile() == null)
            goToLoginActivity();
    }

    private void initView() {
        initPetView();
        initAddMealTypeView();
        initAddWalkTypeView();
        initImagePickerLauncher();
        if(isNewPet) {
            initMealTypesListView();
            initWalkTypesListView();
        }
    }

    private void initPetView() {
        setPetButtonsListener();
        setPetTextChangedListener();
    }

    private void initAddMealTypeView() {
        setAddMealTypeButtonsListener();
        setAddMealTypeTextChangedListener();
        cancelMealType();
    }

    private void initAddWalkTypeView() {
        setAddWalkTypeButtonsListener();
        setAddWalkTypeTextChangedListener();
        cancelWalkType();
    }

    private void initMealTypesListCallbacks() {
        mealTypeAdapter.setMealTypeCallback((mealType, position) -> petProfile.removeMealType(mealType));
    }

    private void initWalkTypesListCallbacks() {
        walkTypeAdapter.setWalkTypeCallback((walkType, position) -> petProfile.removeWalkType(walkType));
    }

    private void initOwnersListCallbacks() {
        ownersAdapter.setOwnerCallback((user, position) -> deleteOwnerPressed(user));
    }

    private void deleteOwnerPressed(UserProfile user) {
        String alertMsg;

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Remove Owner");
        alertDialogBuilder.setIcon(android.R.drawable.ic_menu_delete);

        if (user.getUid().equals(CurrentUser.getInstance().getUid())) {
            if (petProfile.isOnlyOneOwner())
                alertMsg = "You are the only owner of this pet. If you remove yourself, the pet will be deleted and it's data. Are you sure you want to remove yourself?";
            else
                alertMsg = "You are deleting yourself from this pet, you will no longer be able to see it's data. Are you sure?";

            alertDialogBuilder.setMessage(alertMsg);
            alertDialogBuilder.setPositiveButton("Delete", (dialog, which) -> deletePetFromCurrentUser(user));
            alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.show();
        } else {
            alertDialogBuilder.setMessage("You are deleting " + user.getName() + " from this pet, he/she will no longer be able to see it's data. Are you sure?");
            alertDialogBuilder.setPositiveButton("Remove", (dialog, which) -> deletePetFromOtherUser(user));
            alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.show();
        }

    }

    private void deletePetFromCurrentUser(UserProfile user) {
        petProfile.removeOwner(user.getUid());
        DataCrud.getInstance().setPetInDB(petProfile);
        DataCrud.getInstance().deletePetFromDB(user.getUid());
        CurrentUser.getInstance().getUserProfile().removePet(user.getUid());
        DataCrud.getInstance().setUserInDB(CurrentUser.getInstance().getUserProfile());

        owners.remove(user);
        ownersAdapter.notifyDataSetChanged();
    }

    private void deletePetFromOtherUser(UserProfile user) {
        petProfile.removeOwner(user.getUid());
        DataCrud.getInstance().setPetInDB(petProfile);
        DataCrud.getInstance().deletePetFromUser(user.getUid(), petProfile.getId());

        owners.remove(user);
        ownersAdapter.notifyDataSetChanged();
    }

    private void setPetButtonsListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.petIMGProfile.setOnClickListener(v -> checkPermissionAndUploadImage());
        }
        binding.petBTNSave.setOnClickListener(v -> updatePetProfile());
        binding.petBTNDateOfBirth.setOnClickListener(v -> setDate());
        binding.petBTNAddMeal.setOnClickListener(v -> addMealType());
        binding.petBTNAddWalk.setOnClickListener(v -> addWalkType());
        binding.petBTNOwnersList.setOnClickListener(v -> ownersListPressed());
        binding.petBTNAddOwner.setOnClickListener(v -> addOwnerPressed());
        binding.petBTNSearchOwner.setOnClickListener(v -> getOwnerByEmail());
    }

    private void setAddMealTypeButtonsListener() {
        binding.petCVAddMealType.petMealBTNSave.setOnClickListener(v -> saveMealType());
        binding.petCVAddMealType.petMealBTNCancel.setOnClickListener(v -> cancelMealType());
        binding.petCVAddMealType.petMealBTNTime.setOnClickListener(v -> setTime(MEAL));
    }

    private void setAddWalkTypeButtonsListener() {
        binding.petCVAddWalkType.petWalkBTNSave.setOnClickListener(v -> saveWalkType());
        binding.petCVAddWalkType.petWalkBTNCancel.setOnClickListener(v -> cancelWalkType());
        binding.petCVAddWalkType.petWalkBTNTime.setOnClickListener(v -> setTime(WALK));
        binding.petCVAddWalkType.petWalkBTNDuration.setOnClickListener(v -> setDuration());
    }

    private void setAddMealTypeTextChangedListener() {
        binding.petCVAddMealType.petMealEDTName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence name, int start, int before, int count) {
                if (!binding.petCVAddMealType.petMealEDTName.getEditText().getText().toString().isEmpty())
                    binding.petCVAddMealType.petMealEDTName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.petCVAddMealType.petMealEDTAmount.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence amount, int start, int before, int count) {
                if (!binding.petCVAddMealType.petMealEDTAmount.getEditText().getText().toString().isEmpty())
                    binding.petCVAddMealType.petMealEDTAmount.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.petCVAddMealType.petMealEDTUnit.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence unit, int start, int before, int count) {
                if (!binding.petCVAddMealType.petMealEDTUnit.getEditText().getText().toString().isEmpty())
                    binding.petCVAddMealType.petMealEDTUnit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.petCVAddMealType.petMealEDTTime.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.petCVAddMealType.petMealEDTTime.getEditText().getText().toString().isEmpty())
                    binding.petCVAddMealType.petMealEDTTime.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setAddWalkTypeTextChangedListener() {
        binding.petCVAddWalkType.petWalkEDTName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence name, int start, int before, int count) {
                if (!binding.petCVAddWalkType.petWalkEDTName.getEditText().getText().toString().isEmpty())
                    binding.petCVAddWalkType.petWalkEDTName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.petCVAddWalkType.petWalkEDTTime.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.petCVAddWalkType.petWalkEDTTime.getEditText().getText().toString().isEmpty())
                    binding.petCVAddWalkType.petWalkEDTTime.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.petCVAddWalkType.petWalkEDTDuration.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                if (!binding.petCVAddWalkType.petWalkEDTDuration.getEditText().getText().toString().isEmpty())
                    binding.petCVAddWalkType.petWalkEDTDuration.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setPetTextChangedListener() {
        binding.petEDTName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence name, int start, int before, int count) {
                if (!binding.petEDTName.getEditText().getText().toString().isEmpty())
                    binding.petEDTName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.petEDTDateOfBirth.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence amount, int start, int before, int count) {
                if (!binding.petEDTDateOfBirth.getEditText().getText().toString().isEmpty())
                    binding.petEDTDateOfBirth.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.petEDTOwnerEmail.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence amount, int start, int before, int count) {
                if (!binding.petEDTOwnerEmail.getEditText().getText().toString().isEmpty())
                    binding.petEDTOwnerEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initMealTypesListView() {
        mealTypeAdapter = new MealTypeAdapter(this, petProfile.getMealTypes());
        binding.petLSTMeals.setLayoutManager(new LinearLayoutManager(this));
        binding.petLSTMeals.setAdapter(mealTypeAdapter);
        initMealTypesListCallbacks();
    }

    private void initWalkTypesListView() {
        walkTypeAdapter = new WalkTypeAdapter(this, petProfile.getWalkTypes());
        binding.petLSTWalks.setLayoutManager(new LinearLayoutManager(this));
        binding.petLSTWalks.setAdapter(walkTypeAdapter);
        initWalkTypesListCallbacks();
    }

    private void initOwnersListView() {
        hideOwnersList();
        ownersAdapter = new OwnersAdapter(this, owners);
        binding.petLSTOwners.setLayoutManager(new LinearLayoutManager(this));
        binding.petLSTOwners.setAdapter(ownersAdapter);
        initOwnersListCallbacks();
    }

    private void initImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        imageUri = result.getData().getData();
                        binding.petIMGProfile.setImageURI(imageUri);
                        uploadImage();
                    }
                }
        );
    }

    private void clearAddMealTypeView() {
        binding.petCVAddMealType.petMealEDTName.getEditText().setText("");
        binding.petCVAddMealType.petMealEDTAmount.getEditText().setText("");
        binding.petCVAddMealType.petMealEDTUnit.getEditText().setText("");
        binding.petCVAddMealType.petMealEDTTime.getEditText().setText("");

        binding.petCVAddMealType.petMealEDTName.setError(null);
        binding.petCVAddMealType.petMealEDTAmount.setError(null);
        binding.petCVAddMealType.petMealEDTUnit.setError(null);
        binding.petCVAddMealType.petMealEDTTime.setError(null);
    }

    private void clearAddWalkTypeView() {
        binding.petCVAddWalkType.petWalkEDTName.getEditText().setText("");
        binding.petCVAddWalkType.petWalkEDTTime.getEditText().setText("");
        binding.petCVAddWalkType.petWalkEDTDuration.getEditText().setText("");
        binding.petCVAddWalkType.petWalkCBPee.setChecked(true);
        binding.petCVAddWalkType.petWalkCBPoop.setChecked(true);
        binding.petCVAddWalkType.petWalkCBPlay.setChecked(false);

        binding.petCVAddWalkType.petWalkEDTName.setError(null);
        binding.petCVAddWalkType.petWalkEDTTime.setError(null);
        binding.petCVAddWalkType.petWalkEDTDuration.setError(null);
    }

    private void initPetProfile() {
        petProfile = new PetProfile().addOwner(CurrentUser.getInstance().getUid());
    }

    private void cancelMealType() {
        binding.petCVAddMealType.getRoot().setVisibility(View.GONE);
        clearAddMealTypeView();
        enablePetProfileFields();
    }

    private void cancelWalkType() {
        binding.petCVAddWalkType.getRoot().setVisibility(View.GONE);
        enablePetProfileFields();
        clearAddWalkTypeView();
    }

    private void saveMealType() {
        if (!validateMealTypeFields())
            return;

        MealType mealType = new MealType();

        mealType.setName(binding.petCVAddMealType.petMealEDTName.getEditText().getText().toString());
        mealType.setAmount(Integer.parseInt(binding.petCVAddMealType.petMealEDTAmount.getEditText().getText().toString()));
        mealType.setUnit(binding.petCVAddMealType.petMealEDTUnit.getEditText().getText().toString());
        mealType.setTimeFromString(binding.petCVAddMealType.petMealEDTTime.getEditText().getText().toString());

        if (petProfile.addMealType(mealType)) {
            mealTypeAdapter.notifyDataSetChanged();
            cancelMealType();
        } else {
            Toast.makeText(this, "This meal type already exists!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveWalkType() {
        if (!validateWalkTypeFields())
            return;

        WalkType walkType = new WalkType();

        walkType.setName(binding.petCVAddWalkType.petWalkEDTName.getEditText().getText().toString());
        walkType.setTimeFromString(binding.petCVAddWalkType.petWalkEDTTime.getEditText().getText().toString());
        walkType.setDuration(walk_duration_hours, walk_duration_minutes);
        walkType.setPee(binding.petCVAddWalkType.petWalkCBPee.isChecked());
        walkType.setPoop(binding.petCVAddWalkType.petWalkCBPoop.isChecked());
        walkType.setPlay(binding.petCVAddWalkType.petWalkCBPlay.isChecked());


        if (petProfile.addWalkType(walkType)) {
            walkTypeAdapter.notifyDataSetChanged();
            cancelWalkType();
        } else {
            Toast.makeText(this, "This walk type already exists!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateMealTypeFields() {
        if (binding.petCVAddMealType.petMealEDTName.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddMealType.petMealEDTName.setError("Name is required!");
            return false;
        }
        if (binding.petCVAddMealType.petMealEDTTime.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddMealType.petMealEDTTime.setError("Time is required!");
            return false;
        }
        if (binding.petCVAddMealType.petMealEDTUnit.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddMealType.petMealEDTUnit.setError("Amount is required!");
            return false;
        }
        if (binding.petCVAddMealType.petMealEDTAmount.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddMealType.petMealEDTAmount.setError("Unit is required!");
            return false;
        }

        return true;
    }

    private boolean validateWalkTypeFields() {
        if (binding.petCVAddWalkType.petWalkEDTName.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddWalkType.petWalkEDTName.setError("Name is required!");
            return false;
        }
        if (binding.petCVAddWalkType.petWalkEDTTime.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddWalkType.petWalkEDTTime.setError("Time is required!");
            return false;
        }
        if (binding.petCVAddWalkType.petWalkEDTDuration.getEditText().getText().toString().isEmpty()) {
            binding.petCVAddWalkType.petWalkEDTDuration.setError("Duration is required!");
            return false;
        }

        return true;
    }

    private boolean validatePetFields() {
        if (binding.petEDTName.getEditText().getText().toString().isEmpty()) {
            binding.petEDTName.setError("Name is required!");
            return false;
        }
        if (binding.petEDTDateOfBirth.getEditText().getText().toString().isEmpty()) {
            binding.petEDTDateOfBirth.setError("Date of birth is required!");
            return false;
        }
        if (!isImageUploaded) {
            Toast.makeText(this, "Please upload the image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setTime(String type) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(LocalTime.now().getHour())
                .setMinute(LocalTime.now().getMinute())
                .setTitleText("Select time of the day")
                .build();

        timePicker.show(getSupportFragmentManager(), "tag");
        timePicker.addOnPositiveButtonClickListener(selection -> {
            LocalTime selectedTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            if (type.equals(MEAL)) // meal type
                binding.petCVAddMealType.petMealEDTTime.getEditText().setText(selectedTime.format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME)));
            else // walk type
                binding.petCVAddWalkType.petWalkEDTTime.getEditText().setText(selectedTime.format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME)));
        });
    }

    private void setDuration() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogTheme);
        LayoutInflater inflater = LayoutInflater.from(this);
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
                    walk_duration_hours = npHours.getValue();
                    walk_duration_minutes = npMinutes.getValue();
                    binding.petCVAddWalkType.petWalkEDTDuration.getEditText().setText(DateTimeConverter.durationToString(walk_duration_hours, walk_duration_minutes));
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                })
                .show();
    }

    private void addMealType() {
        if (binding.petCVAddMealType.getRoot().getVisibility() == View.VISIBLE)
            return;

        clearAddMealTypeView();
        disablePetProfileFields();
        binding.petCVAddMealType.getRoot().setVisibility(View.VISIBLE);
        binding.petCVAddWalkType.getRoot().setVisibility(View.GONE);
        clearAddWalkTypeView();;
    }

    private void addWalkType() {
        if (binding.petCVAddWalkType.getRoot().getVisibility() == View.VISIBLE)
            return;

        clearAddWalkTypeView();
        disablePetProfileFields();
        binding.petCVAddWalkType.getRoot().setVisibility(View.VISIBLE);
        binding.petCVAddMealType.getRoot().setVisibility(View.GONE);
        clearAddMealTypeView();
    }

    private void setDate() {
        MaterialDatePicker datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
        datePicker.show(getSupportFragmentManager(), "tag");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.FORMAT_DATE);
            binding.petEDTDateOfBirth.getEditText().setText(simpleDateFormat.format(new Date((long) selection)));
        });
    }

    private void updatePetProfile() {
        if (!validatePetFields())
            return;

        String name = binding.petEDTName.getEditText().getText().toString();
        String gender = binding.petSPGender.getSelectedItem().toString();
        String dateOfBirth = binding.petEDTDateOfBirth.getEditText().getText().toString();
        LocalDate localDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern(Constants.FORMAT_DATE));

        petProfile
                .setName(name)
                .setGender(gender)
                .setDateOfBirth(DateTimeConverter.localDateToLong(localDate))
                .setProfileImage(imageUrl);

        addOwnerToPet(CurrentUser.getInstance().getUserProfile());
        DataCrud.getInstance().setPetInDB(petProfile);

        CurrentUserPetsList.getInstance().getPetsData();

        if (isFromActivityMain) {
            finish();
        }
        else
            goToMainActivity();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadPetData() {
        setPetLoadingView(true);
        DataCrud.getInstance().getPetReference(petId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    petProfile = (snapshot.getValue(PetProfile.class));
                    imageUrl = petProfile.getProfileImage();
                    loadOwnersData();
                    setPetDataInView();
                    initMealTypesListView();
                    initWalkTypesListView();
                    setPetLoadingView(false);
                } else
                    errorGetPetData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorGetPetData();
            }
        });
    }

    private void loadOwnersData() {
        if (petProfile.getOwnersIds() != null && !petProfile.getOwnersIds().isEmpty()) {
            owners.clear();
            for (String ownerId : petProfile.getOwnersIds()) {
                DataCrud.getInstance().getUserReference(ownerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserProfile owner = snapshot.getValue(UserProfile.class);
                            owners.add(owner);
                            if (owners.size() == petProfile.getOwnersIds().size()) {
                                initOwnersListView();
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

    private void setPetDataInView() {
        binding.petEDTName.getEditText().setText(petProfile.getName());
        binding.petSPGender.setSelection(getGender(petProfile.getGender()));
        binding.petEDTDateOfBirth.getEditText().setText(DateTimeConverter.longToStringDate(petProfile.getDateOfBirth()));

        Glide
                .with(this).
                load(petProfile.getProfileImage()).
                into(binding.petIMGProfile);

        initMealTypesListView();
    }

    private int getGender(String gender) {
        switch (gender) {
            case "Male":
                return 0;
            case "Female":
                return 1;
            case "Undefined":
                return 2;
        }
        return 0;
    }

    private void disablePetProfileFields() {
        binding.petIMGProfile.setEnabled(false);
        binding.petBTNOwnersList.setEnabled(false);
        binding.petBTNDateOfBirth.setEnabled(false);
        binding.petBTNAddWalk.setEnabled(false);
        binding.petBTNAddMeal.setEnabled(false);
        binding.petBTNSave.setEnabled(false);
        binding.petBTNAddOwner.setEnabled(false);
        binding.petEDTName.setEnabled(false);
        binding.petSPGender.setEnabled(false);
        binding.petLSTMeals.setEnabled(false);
        binding.petLSTMeals.setEnabled(false);
    }

    private void enablePetProfileFields() {
        binding.petIMGProfile.setEnabled(true);
        binding.petEDTName.setEnabled(true);
        binding.petSPGender.setEnabled(true);
        binding.petBTNAddMeal.setEnabled(true);
        binding.petBTNDateOfBirth.setEnabled(true);
        binding.petBTNAddWalk.setEnabled(true);
        binding.petBTNSave.setEnabled(true);
        binding.petLSTMeals.setEnabled(true);
        binding.petBTNOwnersList.setEnabled(true);
        binding.petBTNAddOwner.setEnabled(true);
    }

    private void ownersListPressed() {
        if (binding.petLSTOwners.getVisibility() == View.VISIBLE)
            hideOwnersList();
        else {
            hideAddOwner();
            showOwnersList();
        }
    }

    private void addOwnerPressed() {
        if (binding.petLAYAddOwner.getVisibility() == View.VISIBLE)
            hideAddOwner();
        else {
            hideOwnersList();
            showAddOwner();
        }
    }

    private void hideOwnersList() {
        binding.petLSTOwners.setVisibility(View.GONE);
        binding.petBTNOwnersList.setBackgroundColor(getResources().getColor(R.color.colorPrimary, getTheme()));
    }

    private void showOwnersList() {
        binding.petLSTOwners.setVisibility(View.VISIBLE);
        binding.petBTNOwnersList.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight, getTheme()));
    }

    private void hideAddOwner() {
        binding.petLAYAddOwner.setVisibility(View.GONE);
        binding.petBTNAddOwner.setBackgroundColor(getResources().getColor(R.color.colorPrimary, getTheme()));
    }

    private void showAddOwner() {
        binding.petLAYAddOwner.setVisibility(View.VISIBLE);
        binding.petBTNAddOwner.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight, getTheme()));
    }

    private void getOwnerByEmail() {
        String emailToSearch = binding.petEDTOwnerEmail.getEditText().getText().toString();
        if (emailToSearch.isEmpty()) {
            binding.petEDTOwnerEmail.setError("Email not valid");
            return;
        }

        if (emailToSearch.equals(CurrentUser.getInstance().getUserProfile().getEmail())) {
            binding.petEDTOwnerEmail.setError("You already own this pet");
            return;
        }

        binding.petBTNSearchOwner.setEnabled(false);
        binding.petCPISeaching.setVisibility(View.VISIBLE);

        Query query = FirebaseDB.getInstance().getUsersReference().orderByChild("email").equalTo(emailToSearch);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean isFound = false;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    isFound = true;
                    UserProfile user = userSnapshot.getValue(UserProfile.class);
                    binding.petBTNSearchOwner.setEnabled(true);
                    binding.petCPISeaching.setVisibility(View.INVISIBLE);

                    if (petProfile.isContainsOwner(user.getUid()))
                        SignalUtils.getInstance().toast("Owner already added");
                    else
                        addOwnerToPet(user);
                }
                if (!isFound)
                    errorGetOwnerByEmail();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorGetOwnerByEmail();
            }
        });

    }

    private void addOwnerToPet(UserProfile user) {
        if (user == null)
            return;

        boolean isOwnerAdded = false;

        if (!user.getPetsIds().contains(petProfile.getId())) {
            user.getPetsIds().add(petProfile.getId());
            isOwnerAdded = true;
        }

        if (!petProfile.getOwnersIds().contains(user.getUid())) {
            petProfile.addOwner(user.getUid());
            isOwnerAdded = true;
        }

        DataCrud.getInstance().setUserInDB(user);
        DataCrud.getInstance().setPetInDB(petProfile);

        if (isOwnerAdded) {
            SignalUtils.getInstance().toast("Owner added successfully");
            loadOwnersData();
        }
    }

    private void setImageUploadingView(boolean isUploading) {
        if (isUploading) {
            binding.petCPIUpload.setVisibility(View.VISIBLE);
            binding.petIMGProfile.setAlpha(0.5f);
            binding.petIMGProfile.setEnabled(false);
            binding.petBTNSave.setEnabled(false);
        } else {
            binding.petCPIUpload.setVisibility(View.INVISIBLE);
            binding.petIMGProfile.setAlpha(1f);
            binding.petIMGProfile.setEnabled(true);
            binding.petBTNSave.setEnabled(true);
        }
    }

    private void setPetLoadingView(boolean isLoading) {
        if (isLoading) {
            binding.petLAYProfile.setVisibility(View.INVISIBLE);
            binding.petCPIPetsLoading.setVisibility(View.VISIBLE);
        } else {
            binding.petLAYProfile.setVisibility(View.VISIBLE);
            binding.petCPIPetsLoading.setVisibility(View.INVISIBLE);
        }
    }

    private void uploadImage() {
        setImageUploadingView(true);
        isImageUploaded = false;

        this.fileName = petProfile.getId();

        FilesCrud.getInstance().getPetFileReference(this.fileName).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                        imageUrl = task.getResult().toString();
                        isImageUploaded = true;
                    });
                    setImageUploadingView(true);
                    Toast.makeText(PetProfileActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    setImageUploadingView(false);
                    Toast.makeText(PetProfileActivity.this, "Failed Uploaded Image", Toast.LENGTH_SHORT).show();
                });
    }

    private void errorGetPetData() {
        SignalUtils.getInstance().toast("Error loading pet data");
        finish();
    }

    private void errorGetOwnerByEmail() {
        binding.petBTNSearchOwner.setEnabled(true);
        binding.petEDTOwnerEmail.setError("Email not found");
        binding.petBTNSearchOwner.setVisibility(View.VISIBLE);
        binding.petCPISeaching.setVisibility(View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkPermissionAndUploadImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES},
                    IMAGE_UPLOAD_REQUEST_CODE);
        } else
            openImagePicker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_UPLOAD_REQUEST_CODE)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openImagePicker();
            else
                Log.e("Permission Denied", "Storage permission denied");
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}