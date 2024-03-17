package com.example.dogblog.view.activity;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import com.example.dogblog.R;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.dal.FirebaseDB;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.utils.Constants;
import com.example.dogblog.utils.SignalUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.emailAddressEt);
        etPassword = findViewById(R.id.passwordEt);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        // Set onClickListener for login button
        loginBtn.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString();
            String password = Objects.requireNonNull(etPassword.getText()).toString();
            validateAndSignIn(email, password, false); // Passing false to indicate login
        });

        // Set onClickListener for register button
        registerBtn.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString();
            String password = Objects.requireNonNull(etPassword.getText()).toString();
            validateAndSignIn(email, password, true); // Passing true to indicate registration
        });
    }


    private void validateAndSignIn(String email, String password, boolean isRegister) {
        // Validate email and password
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SignalUtils.getInstance().vibrate(Constants.VIBRATION_TIME);
            etEmail.setError("Enter a valid email address"); // Invalid email, prompt the user to enter a valid email
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8 || !password.matches(".*[a-zA-Z].*")) {
            SignalUtils.getInstance().vibrate(Constants.VIBRATION_TIME);
            etPassword.setError("Enter a password with at least 8 characters including letters");
            return;
        }

        // If both email and password are valid, proceed with Firebase sign-in or registration
        if (isRegister) {
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> loadInUserAndLogin())
                    .addOnFailureListener(e -> {
                        // Display error message if registration fails
                        SignalUtils.getInstance().toast(e.getMessage());
                    });
        } else {
            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> loadInUserAndLogin())
                    .addOnFailureListener(e -> {
                        // Display error message if sign-in fails
                        SignalUtils.getInstance().toast(e.getMessage());
                        // Clear password field for security reasons
                        etPassword.getText().clear();
                    });
        }
    }

    private void loadInUserAndLogin() {
        loadUserProfile(unused -> {
            if (!CurrentUser.getInstance().getUserProfile().getRegistered())
                goToProfileActivity();
            else {
                goToMainActivity();
            }
        }, e -> SignalUtils.getInstance().toast(e.getMessage()));


    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToProfileActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.KEY_NEW_USER, true);
        bundle.putBoolean(Constants.KEY_FROM_MAIN, false);

        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void loadUserProfile(OnSuccessListener<Void> onSuccess, OnFailureListener onFailureListener) {
        DataCrud.getInstance().getUserReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
                if (task.getResult().getValue() != null) {
                    CurrentUser.getInstance().setUserProfile(task.getResult().getValue(UserProfile.class));
                    onSuccess.onSuccess(null);
                } else {
                    createUser(onSuccess, onFailureListener);
                }

            }
        });
    }

    private void createUser(OnSuccessListener<Void> onSuccess, OnFailureListener onFailureListener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfile userProfile = new UserProfile(firebaseUser.getDisplayName(), firebaseUser.getUid(), firebaseUser.getEmail());
        CurrentUser.getInstance().setUserProfile(userProfile);
        DatabaseReference usersReference = FirebaseDB.getInstance().getUsersReference();
        usersReference.child(firebaseUser.getUid()).setValue(userProfile)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailureListener);
    }
}