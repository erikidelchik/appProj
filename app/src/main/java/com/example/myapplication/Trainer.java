package com.example.myapplication;

public class Trainer {
    private String username;
    private String profilePicture;
    private boolean isTrainer;

    public Trainer() {
        // Required empty constructor for Firestore
    }

    public Trainer(String username, String profilePicture, boolean isTrainer) {
        this.username = username;
        this.profilePicture = profilePicture;
        this.isTrainer = isTrainer;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public boolean isTrainer() {
        return isTrainer;
    }
}

