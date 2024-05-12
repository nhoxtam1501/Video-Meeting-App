package com.ducku.conferenceapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ducku.conferenceapp.R;
import com.ducku.conferenceapp.adapters.UserAdapter;
import com.ducku.conferenceapp.databinding.ActivityMainBinding;
import com.ducku.conferenceapp.listeners.UserListener;
import com.ducku.conferenceapp.models.User;
import com.ducku.conferenceapp.utils.Constants;
import com.ducku.conferenceapp.utils.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListener {
    private ActivityMainBinding activityMainBinding;
    private PreferenceManager preferenceManager;

    private List<User> users;
    private UserAdapter userAdapter;

    private RecyclerView userRecyclerView;
    private TextView textErrorMessage;

    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        TextView textTitle = activityMainBinding.textTitle;
        userRecyclerView = activityMainBinding.userRecyclerView;
        textErrorMessage = activityMainBinding.textErrorMessage;
        swipeRefreshLayout = activityMainBinding.swipeRefreshLayout;

        users = new ArrayList<>();
        userAdapter = new UserAdapter(users, this);
        userRecyclerView.setAdapter(userAdapter);
        preferenceManager = new PreferenceManager(getApplicationContext());

        textTitle.setText(String.format("%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)));

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sendFCMTokenToDatabase(task.getResult());
            }
        });

        activityMainBinding.textSignOut.setOnClickListener(v -> {
            signOut();
        });
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);
        getUsers();
    }

    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.getId().equals(currentUserId)) {
                                continue;
                            }
                            User user = new User();
                            user.setFirstName(documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                            user.setLastName(documentSnapshot.getString(Constants.KEY_LAST_NAME));
                            user.setEmail(documentSnapshot.getString(Constants.KEY_EMAIL));
                            user.setToken(documentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            users.add(user);
                        }
                        if (!users.isEmpty()) {
                            userAdapter.notifyDataSetChanged();
                        } else {
                            textErrorMessage.setText(String.format("%s", "No users available"));
                            textErrorMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        textErrorMessage.setText(String.format("%s", "No users available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokenToDatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        reference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("TokenException", task.getException().toString());
                        Toast.makeText(this, "Unable to send token: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        Toast.makeText(this, "Signing Out....", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                preferenceManager.clearPreferences();
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            } else
                Toast.makeText(this, "Unable to sign out", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void initVideoMeeting(User user) {
        if (user.getToken() == null || user.getToken().trim().isEmpty()) {
            Toast.makeText(this, user.getFirstName() + " " + user.getLastName() + " is not available for video meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutGoingInvitationActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initAudioMeeting(User user) {
        if (user.getToken() == null || user.getToken().trim().isEmpty()) {
            Toast.makeText(this, user.getFirstName() + " " + user.getLastName() + " is not available for audio meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutGoingInvitationActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }
}