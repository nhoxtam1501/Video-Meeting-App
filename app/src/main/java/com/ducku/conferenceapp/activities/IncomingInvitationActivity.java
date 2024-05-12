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
import com.ducku.conferenceapp.databinding.ActivityIncomingInvitationBinding;
import com.ducku.conferenceapp.network.ApiClient;
import com.ducku.conferenceapp.network.ApiService;
import com.ducku.conferenceapp.utils.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {
    ActivityIncomingInvitationBinding activityIncomingInvitationBinding;
    private String meetingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityIncomingInvitationBinding = ActivityIncomingInvitationBinding.inflate(getLayoutInflater());
        setContentView(activityIncomingInvitationBinding.getRoot());

        ImageView imageMeetingType = activityIncomingInvitationBinding.imageMeetingType;
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MESSAGE_MEETING_TYPE);

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else if (meetingType.equals("audio")) {
                imageMeetingType.setImageResource(R.drawable.ic_phone_call);
            }
        }

        TextView textFirstChar = activityIncomingInvitationBinding.textFirstChar;
        textFirstChar.setText(getIntent().getStringExtra(Constants.KEY_FIRST_NAME).substring(0, 1));

        TextView textUsername = activityIncomingInvitationBinding.textUsername;
        textUsername.setText(getIntent().getStringExtra(String.format("%s %s", Constants.KEY_FIRST_NAME, Constants.KEY_LAST_NAME)));

        TextView textEmail = activityIncomingInvitationBinding.textEmail;
        textEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        ImageView imageAcceptInvitation = activityIncomingInvitationBinding.imageAcceptInvitation;
        imageAcceptInvitation.setOnClickListener(v -> {
            sendInvitationResponse(Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED,
                    getIntent().getStringExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN));
        });

        ImageView imageRejectInvitation = activityIncomingInvitationBinding.imageRejectInvitation;
        imageRejectInvitation.setOnClickListener(v -> {
            sendInvitationResponse(Constants.REMOTE_MESSAGE_INVITATION_REJECTED,
                    getIntent().getStringExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN));
        });
    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MESSAGE_TYPE, Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MESSAGE_DATA, data);
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED)) {
                        try {
                            //URL serverURL = new URL("https://meet.jit.si");
                            URL serverURL = new URL("http://10.0.2.2:8000/");
                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            //builder.setFeatureFlag("welcomepage.enabled", false);
                            //builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MESSAGE_MEETING_ROOM));
                            Log.d("IncomingMeetingRoom", Constants.REMOTE_MESSAGE_MEETING_ROOM);
                            if (meetingType.equals("audio")) {
                                builder.setVideoMuted(true);
                            }
                            JitsiMeetActivity.launch(IncomingInvitationActivity.this, builder.build());
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(IncomingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MESSAGE_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
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