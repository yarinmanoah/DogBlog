package com.example.dogblog.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.dogblog.dal.FilesCrud;
import com.example.dogblog.dal.FirebaseDB;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.databinding.ActivityUserProfileBinding;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.utils.Constants;

import com.example.dogblog.view.fragment.SettingsFragment;
import com.example.dogblog.view.fragment.TopFragment;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private static final int IMAGE_UPLOAD_REQUEST_CODE = 1;
    private Uri imageUri;
    private String imageUrl;
    private String fileName;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isNewUser = false;
    private boolean isFromActivityMain = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isNewUser = getIntent().getBooleanExtra(Constants.KEY_NEW_USER, true);
        isFromActivityMain = getIntent().getBooleanExtra(Constants.KEY_FROM_MAIN, true);

        if (!isNewUser) {
            binding.profileBTNHome.setVisibility(View.VISIBLE);
            binding.profileBTNHome.setText("Back");
        } else {
            binding.profileLAYNewPet.setVisibility(View.GONE);
            binding.profileBTNHome.setText("Continue");
            binding.profileBTNHome.setVisibility(View.GONE);
        }

        setButtonsListener();
        setTextChangedListener();
        initImagePickerLauncher();
        initUserProfileData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null || CurrentUser.getInstance().getUserProfile() == null)
            goToLoginActivity();
    }

    private void setButtonsListener() {
        binding.profileIMGProfile.setOnClickListener(v -> checkPermissionAndUploadImage());
        binding.profileBTNAddPet.setOnClickListener(v -> goToPetProfileActivity());
        binding.profileBTNSave.setOnClickListener(v -> updateUserProfile(imageUrl));
        binding.profileBTNHome.setOnClickListener(v -> {
            if (isFromActivityMain)
                finish();
            else
                goToMainActivity();
        });
    }

    private void setTextChangedListener() {
        binding.profileEDTName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence name, int start, int before, int count) {
                if (!binding.profileEDTName.getEditText().getText().toString().isEmpty())
                    binding.profileEDTName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.profileEDTPhone.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence amount, int start, int before, int count) {
                if (!binding.profileEDTPhone.getEditText().getText().toString().isEmpty())
                    binding.profileEDTPhone.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private boolean validateFields() {
        String name = binding.profileEDTName.getEditText().getText().toString().trim();
        String phoneNumber = binding.profileEDTPhone.getEditText().getText().toString().trim();

        boolean isValidName = isValidName(name);
        boolean isValidPhoneNumber = isValidPhoneNumber(phoneNumber);
        boolean isNameEmpty = TextUtils.isEmpty(name);
        boolean isPhoneNumberEmpty = TextUtils.isEmpty(phoneNumber);

        // Validate name field
        if (isNameEmpty && !isPhoneNumberEmpty) {
            // Name is empty but phone number is not
            binding.profileEDTName.setError("Name is required!");
            binding.profileEDTName.setErrorEnabled(true);
        } else if (!isValidName && !isNameEmpty) {
            // Name is not empty but invalid
            binding.profileEDTName.setError("Invalid name! Name must contain First and Last name.");
            binding.profileEDTName.setErrorEnabled(true);
        } else {
            // Name is valid or empty
            binding.profileEDTName.setError(null);
            binding.profileEDTName.setErrorEnabled(false);
        }

        // Validate phone number field
        if (isPhoneNumberEmpty && !isNameEmpty) {
            // Phone number is empty but name is not
            binding.profileEDTPhone.setError("Phone is required!");
            binding.profileEDTPhone.setErrorEnabled(true);
        } else if (!isValidPhoneNumber && !isPhoneNumberEmpty) {
            // Phone number is not empty but invalid
            binding.profileEDTPhone.setError("Invalid phone number! Phone must contain 8 numbers.");
            binding.profileEDTPhone.setErrorEnabled(true);
        } else {
            // Phone number is valid or empty
            binding.profileEDTPhone.setError(null);
            binding.profileEDTPhone.setErrorEnabled(false);
        }

        // Return true only if both fields are valid
        return isValidName && isValidPhoneNumber;
    }

    private boolean isValidName(String name) {
        // Regex to check if the name contains only letters and only one space between words
        return name.matches("[a-zA-Z]+\\s[a-zA-Z]+");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Regular expression for Israeli mobile phone numbers without hyphens
        // The pattern allows for mobile numbers starting with "05" followed by 7 digits
        // Example valid formats: 0501234567, 0541234567, 0521234567
        String israeliMobileNumberPattern = "^05[0-9]{8}$";

        // Return true if the phone number matches the Israeli mobile phone number pattern
        return phoneNumber.matches(israeliMobileNumberPattern);
    }

    private void initImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        imageUri = result.getData().getData();
                        binding.profileIMGProfile.setImageURI(imageUri);
                        uploadImage();
                    }
                }
        );
    }

    private void initUserProfileData() {
        UserProfile userProfile = CurrentUser.getInstance().getUserProfile();
        validateFields();
        if (userProfile.getProfileImage() != null && !userProfile.getProfileImage().isEmpty()) {
            imageUrl = userProfile.getProfileImage();
            Glide.with(UserProfileActivity.this)
                    .load(userProfile.getProfileImage())
                    .into(binding.profileIMGProfile);
        }
    }

    private void updateUserProfile(String imageUrl) {
        if (!validateFields())
            return;

        String name = binding.profileEDTName.getEditText().getText().toString();
        String phoneNumber = binding.profileEDTPhone.getEditText().getText().toString();

        UserProfile userProfile = CurrentUser.getInstance().getUserProfile();
        userProfile.setName(name);
        userProfile.setPhoneNumber(phoneNumber);
        userProfile.setRegistered(true);

        if (!TextUtils.isEmpty(imageUrl) && !imageUrl.equals(userProfile.getProfileImage())) {
            userProfile.setProfileImage(imageUrl);
        }

        FirebaseDB.getInstance().getUsersReference().child(userProfile.getUid()).setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    profileSaved();
                    Toast.makeText(UserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImage() {
        setImageUploadingView(true);

        this.fileName = CurrentUser.getInstance().getUid();
        FilesCrud.getInstance().getUserFileReference(this.fileName).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null) {
                            imageUrl = task.getResult().toString();
                        }
                        else{
                            setImageUploadingView(false);
                            Toast.makeText(UserProfileActivity.this, "Failed Uploaded Image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    setImageUploadingView(false);
                    Toast.makeText(UserProfileActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkPermissionAndUploadImage() {
        // Check if permission to read external storage is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES},
                    IMAGE_UPLOAD_REQUEST_CODE);
        } else {
            // Permission already granted, start image selection process
            openImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_UPLOAD_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start image selection process
                openImagePicker();
            } else {
                // Permission denied
                Log.e("Permission Denied", "Storage permission denied");
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToPetProfileActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.KEY_NEW_PET, true);
        bundle.putBoolean(Constants.KEY_FROM_MAIN, false);

        Intent intent = new Intent(this, PetProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void profileSaved() {
        if (isNewUser) {
            binding.profileBTNSave.setEnabled(false);
            binding.profileBTNSave.setText("Profile Saved");
            binding.profileLAYNewPet.setVisibility(View.VISIBLE);
        }

        binding.profileBTNHome.setVisibility(View.VISIBLE);
    }

    private void setImageUploadingView(boolean isUploading) {
        if (isUploading) {
            binding.profileCPIUpload.setVisibility(View.VISIBLE);
            binding.profileIMGProfile.setAlpha(0.5f);
            binding.profileIMGProfile.setEnabled(false);
            binding.profileBTNSave.setEnabled(false);
        } else {
            binding.profileCPIUpload.setVisibility(View.INVISIBLE);
            binding.profileIMGProfile.setAlpha(1f);
            binding.profileIMGProfile.setEnabled(true);
            binding.profileBTNSave.setEnabled(true);
        }
    }

}