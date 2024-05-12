package com.ducku.conferenceapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ducku.conferenceapp.R;
import com.ducku.conferenceapp.databinding.ActivityOutGoingInvitationBinding;
import com.ducku.conferenceapp.models.User;
import com.ducku.conferenceapp.network.ApiClient;
import com.ducku.conferenceapp.network.ApiService;
import com.ducku.conferenceapp.utils.Constants;
import com.ducku.conferenceapp.utils.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutGoingInvitationActivity extends AppCompatActivity {
    ActivityOutGoingInvitationBinding activityOutGoingInvitationBinding;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;

    private String meetingRoom = null;

    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityOutGoingInvitationBinding = ActivityOutGoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(activityOutGoingInvitationBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        ImageView imageMeetingType = activityOutGoingInvitationBinding.imageMeetingType;
        TextView textFirstChar = activityOutGoingInvitationBinding.textFirstChar;
        TextView textViewUsername = activityOutGoingInvitationBinding.textUsername;
        TextView textEmail = activityOutGoingInvitationBinding.textEmail;
        meetingType = getIntent().getStringExtra("type");


        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else if (meetingType.equals("audio")) {
                imageMeetingType.setImageResource(R.drawable.ic_phone_call);
            }
        }

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            textFirstChar.setText(user.getFirstName().substring(0, 1));
            textViewUsername.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            textEmail.setText(user.getEmail());
        }

        ImageView imageStopInvitation = activityOutGoingInvitationBinding.imageStopInvitation;
        imageStopInvitation.setOnClickListener(v -> {
            if (user != null) {
                cancelInvitation(user.getToken());
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult();
                if (user != null && meetingType != null) {
                    initMeeting(meetingType, user.getToken());
                }
            }
        });


    }

    private void initMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MESSAGE_TYPE, Constants.REMOTE_MESSAGE_INVITATION);
            data.put(Constants.REMOTE_MESSAGE_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MESSAGE_INVITER_TOKEN, inviterToken);

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" + UUID.randomUUID()
                    .toString().substring(0, 5);
            data.put(Constants.REMOTE_MESSAGE_MEETING_ROOM, meetingRoom);


            body.put(Constants.REMOTE_MESSAGE_DATA, data);
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MESSAGE_INVITATION);


        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MESSAGE_INVITATION))
                        Toast.makeText(OutGoingInvitationActivity.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    else if (type.equals(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)) {
                        Toast.makeText(OutGoingInvitationActivity.this, "Invitation cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OutGoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutGoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MESSAGE_TYPE, Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE, Constants.REMOTE_MESSAGE_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MESSAGE_DATA, data);
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED)) {
                    try {
                        //URL serverUrl = new URL("https://meet.jit.si");
                        URL serverUrl = new URL("http://10.0.2.2:8000/");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverUrl);
                        //builder.setFeatureFlag("welcomepage.enabled", false);
                        //builder.setWelcomePageEnabled(false);
                        //String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6InZwYWFzLW1hZ2ljLWNvb2tpZS0xZmM1NDJhM2U0NDE0YTQ0YjI2MTE2NjgxOTVlMmJmZS80ZjQ5MTAiLCJ0eXAiOiJKV1QifQ.eyJhdWQiOiJqaXRzaSIsImNvbnRleHQiOnsidXNlciI6eyJpZCI6IjBmOGI3NzYwLWMxN2YtNGExMi1iMTM0LWM2YWMzNzE2NzE0NCIsIm5hbWUiOiJKb2huIERvZSIsImF2YXRhciI6Imh0dHBzOi8vbGluay50by91c2VyL2F2YXRhci9waWN0dXJlIiwiZW1haWwiOiJqb2huLmRvZUBjb21wYW55LmNvbSIsIm1vZGVyYXRvciI6InRydWUifSwiZmVhdHVyZXMiOnsibGl2ZXN0cmVhbWluZyI6ImZhbHNlIiwib3V0Ym91bmQtY2FsbCI6ImZhbHNlIiwidHJhbnNjcmlwdGlvbiI6ImZhbHNlIiwicmVjb3JkaW5nIjoiZmFsc2UifSwicm9vbSI6eyJyZWdleCI6ZmFsc2V9fSwiZXhwIjoxNzc4MzIzMDAxLCJpc3MiOiJjaGF0IiwibmJmIjoxNTk2MTk3NjUyLCJyb29tIjoiKiIsInN1YiI6InZwYWFzLW1hZ2ljLWNvb2tpZS0xZmM1NDJhM2U0NDE0YTQ0YjI2MTE2NjgxOTVlMmJmZSJ9.TBqclFTmhepUAHCAXyjaqLAp3LSN8eK6QBZETczZOZ8WvhM9LX_x7WnpYvh5CVX19WG9UCvDH_m1gEIXxwE6lARZJaXun3keTsznUw7HztZJJrgd0oCYrZPGCDyJrth046gkA-OFjuUiLH0wDSe0xNBjkl7Ytn3BWR_t90MTOllcc1G2cEyadntJ2HAYlegpRALXxgWwN827g8W8t39ITQ7l-KymeYqwzEiLaKpF__v8q7gjnB6NdiHWvTxFIiaukuxRV6T9zq4PN99DdUN2zqVwLByMYuPCw3GdGFcwdcOOtjJvlw5kk2yySJmlkrD5G5F2smGBbm2KC9Zmu7EK4ODeAQK6ni4vo4d4n6VDTZiIGRev3oQAdaBn9ZBBU3uxBjjLp6ybpOJ8jsM7cNDoxSKg9Psy9O5v59wo0NwhUf8VzyPCPmu8fobmUkiykb2L--2GNlZm2u8tLBc_vw7BFgbKIjZQeXx4hnZtsLCCgoNRMbVRgMWkG9IUinJCyYWz4IY0RNOyX6fJZf-4CSvLWh8kcpR5QA487igVHOOV3RKjoUujQJqg0wTmWiY1uRcOkg-GvjjZkIkTuVdOIQz6_CtJL7WK3H1O9SZ7st9pXgUVLh30UgOHFYkp5rTKQEn12OyMNozbbgN9m1gYmAy5wDoTfGq0B_Ow-IX8SWUIAeY";
                        //builder.setToken(token);
                        builder.setRoom(meetingRoom);
                        Log.d("OutGoingMeetingRoom", meetingRoom);
                        if (meetingType.equals("audio")) {
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutGoingInvitationActivity.this, builder.build());
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (type.equals(Constants.REMOTE_MESSAGE_INVITATION_REJECTED)) {
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}