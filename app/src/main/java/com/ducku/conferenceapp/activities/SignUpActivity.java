package com.ducku.conferenceapp.activities;

import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ducku.conferenceapp.databinding.ActivitySignUpBinding;
import com.ducku.conferenceapp.utils.Constants;
import com.ducku.conferenceapp.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding activitySignUpBinding;
    private EditText inputFirstName, inputLastName, inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(activitySignUpBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        inputFirstName = activitySignUpBinding.inputFirstname;
        inputLastName = activitySignUpBinding.inputLastname;
        inputEmail = activitySignUpBinding.inputEmail;
        inputPassword = activitySignUpBinding.inputPassword;
        inputConfirmPassword = activitySignUpBinding.inputConfirmPassword;
        buttonSignUp = activitySignUpBinding.buttonSignUp;
        progressBar = activitySignUpBinding.signUpProgressBar;

        activitySignUpBinding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });
        activitySignUpBinding.textSignIn.setOnClickListener(v -> {
            onBackPressed();
        });

        buttonSignUp.setOnClickListener(v -> {
            validateData();
        });
    }

    private void validateData() {
        if (inputFirstName.getText().toString().trim().isEmpty()) {
            inputFirstName.setError("Please enter first name");
            return;
        }
        if (inputLastName.getText().toString().trim().isEmpty()) {
            inputLastName.setError("Please enter last name");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString().trim()).matches()) {
            inputEmail.setError("Please enter email");
            return;
        }
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputPassword.setError("Please enter password");
            return;
        }
        if (inputConfirmPassword.getText().toString().trim().isEmpty()) {
            inputConfirmPassword.setError("Please enter confirm password");
            return;
        }
        if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())) {
            inputPassword.setText("");
            inputConfirmPassword.setError("Password not match");
            return;
        }

        signUp();
    }

    private void signUp() {
        buttonSignUp.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                preferenceManager.putString(Constants.KEY_USER_ID, task.getResult().getId());
                Log.d("SignUpId", task.getResult().getId());
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
                preferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
                preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                preferenceManager.putString(Constants.KEY_PASSWORD, inputPassword.getText().toString());

                buttonSignUp.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}