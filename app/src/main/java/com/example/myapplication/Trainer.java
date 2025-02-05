package com.example.myapplication;

public class Trainer {
    private String userId; // Firestore doc ID or Auth UID
    private String username;
    private String profilePicture;
    private boolean isTrainer;

    public Trainer() {} // needed for Firestore

    public Trainer(String userId, String username, String profilePicture, boolean isTrainer) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
        this.isTrainer = isTrainer;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getProfilePicture() { return profilePicture; }
    public boolean isTrainer() { return isTrainer; }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

