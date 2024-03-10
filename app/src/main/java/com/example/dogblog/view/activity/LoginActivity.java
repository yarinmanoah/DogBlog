package com.example.dogblog.view.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.dogblog.R;
import com.example.dogblog.dal.DataCrud;
import com.example.dogblog.dal.FirebaseDB;
import com.example.dogblog.current_state.singletons.CurrentUser;
import com.example.dogblog.model.UserProfile;
import com.example.dogblog.utils.Constants;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            login();
        } else {
            loadUserProfile();
        }
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

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
    }

    private void login() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.ic_icon_background)
                .setTheme(R.style.LoginTheme)
                .build();
        signInLauncher.launch(signInIntent);

    }

    private void loadUserProfile() {
        DataCrud.getInstance().getUserReference(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    if (task.getResult().getValue() != null)
                        CurrentUser.getInstance().setUserProfile(task.getResult().getValue(UserProfile.class));
                    else
                        createUser();
                    if (!CurrentUser.getInstance().getUserProfile().getRegistered())
                        goToProfileActivity();
                    else {
                        goToMainActivity();
                    }
                }
            }
        });
    }

    private void createUser() {
        UserProfile userProfile = new UserProfile(firebaseUser.getDisplayName(), firebaseUser.getUid(), firebaseUser.getEmail());
        CurrentUser.getInstance().setUserProfile(userProfile);
        DatabaseReference usersReference = FirebaseDB.getInstance().getUsersReference();
        usersReference.child(firebaseUser.getUid()).setValue(userProfile);
    }

}