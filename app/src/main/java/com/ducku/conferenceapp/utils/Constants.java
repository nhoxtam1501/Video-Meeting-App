package com.ducku.conferenceapp.utils;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    public static final String KEY_USER_ID = "user_id";

    public static final String KEY_PREFERENCE_NAME = "videoMeetingReference";

    public static final String KEY_IS_SIGNED_IN = "isSignedIn";

    public static final String KEY_FCM_TOKEN = "fcm_token";

    public static final String REMOTE_MESSAGE_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MESSAGE_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MESSAGE_TYPE = "type";
    public static final String REMOTE_MESSAGE_INVITATION = "invitation";
    public static final String REMOTE_MESSAGE_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MESSAGE_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MESSAGE_DATA = "data";
    public static final String REMOTE_MESSAGE_REGISTRATION_IDS = "registration_ids";
    public static final String REMOTE_MESSAGE_INVITATION_RESPONSE = "invitationResponse";

    public static final String REMOTE_MESSAGE_INVITATION_ACCEPTED = "accepted";

    public static final String REMOTE_MESSAGE_INVITATION_REJECTED = "rejected";

    public static final String REMOTE_MESSAGE_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MESSAGE_MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constants.REMOTE_MESSAGE_AUTHORIZATION
                , "key=AAAAuAzR2wM:APA91bEJX6Dl6B6DonCySUqWuF9L6zjVt__iVRZMnE_la6sz4IOCEF-QZc9ojcjHLljFd51ui542B5oiJNI_-09ZN-ogkqj39CJCqy6VyZmT-JdPemZkKnc8u23Cz7AU8JFPE2m-dSDl");
        headers.put(Constants.REMOTE_MESSAGE_CONTENT_TYPE, "application/json");
        return headers;
    }
}
