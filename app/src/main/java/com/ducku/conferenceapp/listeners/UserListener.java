package com.ducku.conferenceapp.listeners;

import com.ducku.conferenceapp.models.User;

public interface UserListener {
    void initVideoMeeting(User user);

    void initAudioMeeting(User user);
}
