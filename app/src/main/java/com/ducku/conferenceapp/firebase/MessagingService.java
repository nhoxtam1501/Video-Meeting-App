package com.ducku.conferenceapp.firebase;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ducku.conferenceapp.activities.IncomingInvitationActivity;
import com.ducku.conferenceapp.utils.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String type = message.getData().get(Constants.REMOTE_MESSAGE_TYPE);


        if (type != null) {
            if (type.equals(Constants.REMOTE_MESSAGE_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intent.putExtra(Constants.REMOTE_MESSAGE_MEETING_TYPE,
                        message.getData().get(Constants.REMOTE_MESSAGE_MEETING_TYPE));
                intent.putExtra(Constants.KEY_FIRST_NAME,
                        message.getData().get(Constants.KEY_FIRST_NAME));
                intent.putExtra(Constants.KEY_LAST_NAME,
                        message.getData().get(Constants.KEY_LAST_NAME));
                intent.putExtra(Constants.KEY_EMAIL,
                        message.getData().get(Constants.KEY_EMAIL));
                intent.putExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN,
                        message.getData().get(Constants.REMOTE_MESSAGE_INVITER_TOKEN));
                intent.putExtra(Constants.REMOTE_MESSAGE_MEETING_ROOM,
                        message.getData().get(Constants.REMOTE_MESSAGE_MEETING_ROOM));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)) {
                Intent intent = new Intent(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE);
                intent.putExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE,
                        message.getData().get(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        }
    }
}
