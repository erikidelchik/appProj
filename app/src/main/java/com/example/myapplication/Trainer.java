package com.example.myapplication;

// data class for storing trainer information
public class Trainer {
    private String name;
    private String profilePictureUrl;
    private String userId;

    public Trainer() {
        // Required empty constructor for Firestore
    }

    public Trainer(String name, String profilePictureUrl, String userId) {
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getUserId() {
        return userId;
    }
}
