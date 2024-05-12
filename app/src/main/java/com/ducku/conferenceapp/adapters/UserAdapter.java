package com.ducku.conferenceapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ducku.conferenceapp.R;
import com.ducku.conferenceapp.listeners.UserListener;
import com.ducku.conferenceapp.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    List<User> users;
    UserListener userListener;

    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_user,
                        parent,
                        false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : -1;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textFirstChar, textUsername, textEmail;
        ImageView imageAudioMeeting, imageVideoMeeting;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textFirstChar = itemView.findViewById(R.id.textFirstChar);
            textUsername = itemView.findViewById(R.id.textUsername);
            textEmail = itemView.findViewById(R.id.textEmail);
            imageAudioMeeting = itemView.findViewById(R.id.imageAudioMeeting);
            imageVideoMeeting = itemView.findViewById(R.id.imageVideoMeeting);
        }

        void setUserData(User user) {
            textFirstChar.setText(user.getFirstName().substring(0, 1));
            textUsername.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            textEmail.setText(user.getEmail());
            imageVideoMeeting.setOnClickListener(v -> {
                userListener.initVideoMeeting(user);
            });

            imageAudioMeeting.setOnClickListener(v -> {
                userListener.initAudioMeeting(user);
            });
        }


    }
}
