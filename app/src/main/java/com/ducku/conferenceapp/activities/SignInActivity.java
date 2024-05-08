package com.ducku.conferenceapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ducku.conferenceapp.databinding.ActivitySignInBinding;
import com.ducku.conferenceapp.utils.Constants;
import com.ducku.conferenceapp.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding activitySignInBinding;

    private EditText inputEmail, inputPassword;
    private MaterialButton buttonSignIn;

    private ProgressBar signInProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(activitySignInBinding.getRoot());

        inputEmail = activitySignInBinding.inputEmail;
        inputPassword = activitySignInBinding.inputPassword;
        buttonSignIn = activitySignInBinding.buttonSignIn;
        signInProgressBar = activitySignInBinding.signInProgressBar;
        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }


        activitySignInBinding.textSignUp.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });

        buttonSignIn.setOnClickListener(v -> {
            validateData();
            signIn();
        });

    }

    private void validateData() {
        if (inputEmail.getText().toString().trim().isEmpty()) {
            inputEmail.setError("Please enter your email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
            inputEmail.setError("Please enter valid email");
            return;
        }
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputPassword.setError("Please enter your password");
            return;
        }
    }

    private void signIn() {
        buttonSignIn.setVisibility(View.INVISIBLE);
        signInProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);

                        preferenceManager.putString(Constants.KEY_USER_ID, snapshot.getId());
                        Log.d("SignInId", snapshot.getId());
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, snapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME, snapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL, snapshot.getString(Constants.KEY_EMAIL));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        buttonSignIn.setVisibility(View.VISIBLE);
                        signInProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(this, "Unable to sign in. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}