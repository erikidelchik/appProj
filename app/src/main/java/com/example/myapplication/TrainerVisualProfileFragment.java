package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class TrainerVisualProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "ARG_USER_ID";
    private static final String ARG_USERNAME = "ARG_USERNAME";
    private static final String ARG_PROFILE_PIC = "ARG_PROFILE_PIC";

    private String userId;
    private String username;
    private String profilePictureUrl;

    ShapeableImageView profilePic;
    TextView trainerNameView;

    public static TrainerVisualProfileFragment newInstance(String userId, String username, String profilePictureUrl) {
        TrainerVisualProfileFragment fragment = new TrainerVisualProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_PROFILE_PIC, profilePictureUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            username = getArguments().getString(ARG_USERNAME);
            profilePictureUrl = getArguments().getString(ARG_PROFILE_PIC);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_visual_trainer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        profilePic = view.findViewById(R.id.profilePic);
        trainerNameView = view.findViewById(R.id.trainer_name);
        
        trainerNameView.setText(username);

        // Load the profile pic via Glide
        Glide.with(requireContext())
                .load(profilePictureUrl)
                .placeholder(R.drawable.blank_profile_picture)
                .into(profilePic);
    }
}
