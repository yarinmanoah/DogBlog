package com.example.dogblog.view.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.dogblog.R;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.dal.FirebaseDB;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.utils.Constants;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail= findViewById(R.id.emailAddressEt);
        etPassword= findViewById(R.id.passwordEt);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            loadInUserAndLogin();


        loginBtn.setOnClickListener((v) -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Validate email and password
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Invalid email, prompt the user to enter a valid email
                etEmail.setError("Enter a valid email address");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                // Invalid password, prompt the user to enter a password
                etPassword.setError("Enter a password");
                return;
            }

            // If both email and password are valid, proceed with Firebase sign-in
            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> loadInUserAndLogin())
                    .addOnFailureListener(e -> {
                        // Display error message if sign-in fails
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Clear password field for security reasons
                        etPassword.getText().clear();
                    });
        });



        registerBtn.setOnClickListener((v) -> {
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                    .addOnSuccessListener(authResult -> loadInUserAndLogin())
                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        });

    }

    private void loadInUserAndLogin() {
        loadUserProfile(unused -> {
            if (!CurrentUser.getInstance().getUserProfile().getRegistered())
                goToProfileActivity();
            else {
                goToMainActivity();
            }
        }, e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

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