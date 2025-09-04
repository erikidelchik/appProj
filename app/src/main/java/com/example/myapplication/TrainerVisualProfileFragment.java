package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerVisualProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "ARG_USER_ID";
    private static final String ARG_USERNAME = "ARG_USERNAME";
    private static final String ARG_PROFILE_PIC = "ARG_PROFILE_PIC";

    private String trainerId;
    private String username;
    private String profilePictureUrl;

    ShapeableImageView profilePic;
    TextView trainerNameView;

    private RecyclerView trainerPostsRecyclerView;
    private PostAdapter postAdapter;
    private List<TrainerProfileFragment.PostModel> postList = new ArrayList<>();
    private Button followButton;
    private FirebaseAuth auth;

    private RatingBar ratingBar;
    private TextView ratingSummary;
    private ListenerRegistration postsReg;

    public static TrainerVisualProfileFragment newInstance(String trainerId, String username, String profilePictureUrl) {
        TrainerVisualProfileFragment fragment = new TrainerVisualProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, trainerId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_PROFILE_PIC, profilePictureUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            trainerId = getArguments().getString(ARG_USER_ID);
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
        // Initialize RecyclerView
        trainerPostsRecyclerView = view.findViewById(R.id.trainerPostsRecyclerView);
        followButton = view.findViewById(R.id.buttonFollow);

        trainerNameView.setText(username);

        // Initialize rating UI components
        ratingBar = view.findViewById(R.id.ratingBar);
        ratingSummary = view.findViewById(R.id.ratingSummary);

        // Load the profile pic via Glide
        Glide.with(requireContext())
                .load(profilePictureUrl)
                .placeholder(R.drawable.blank_profile_picture)
                .into(profilePic);


        // Create adapter and set to RecyclerView
        postAdapter = new PostAdapter(postList,trainerId,false);

        trainerPostsRecyclerView.setAdapter(postAdapter);
        // Optionally set a LayoutManager
        trainerPostsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadPosts();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(trainerId)) {
            // Trainer viewing their own profile:
            followButton.setVisibility(View.GONE);
            // Hide the RatingBar input since a trainer shouldn’t rate themselves.
            ratingBar.setVisibility(View.GONE);
            ratingSummary.setVisibility(View.GONE);
        }

        else {
            // For non-trainer users, check following status and allow rating.
            checkIfAlreadyFollowing(trainerId);
            // Listen for rating changes and update the rating in Firestore.
            ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    updateUserRating(rating);
                }
            });
            // Check if the current user already rated this trainer.
            checkIfAlreadyRated();
        }

        // Listen for rating changes (by any user) to update the average rating and count.
        loadRatings();

        // Follow/Unfollow button functionality
        followButton.setOnClickListener(v -> {
            if (followButton.getText().toString().equals("Follow")) {
                followTrainer();
            }
            else {
                unfollowTrainer();
            }
        });
    }

    private void checkIfAlreadyFollowing(String trainerId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = currentUser.getUid();

        // Check if doc with currentUserId exists in trainer's followers subcollection
        db.collection("users")
                .document(trainerId)
                .collection("followers")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Already following
                        followButton.setText("Unfollow");
                    } else {
                        // Not following
                        followButton.setText("Follow");
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback
                    followButton.setText("Follow");
                });
    }

    private void followTrainer() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();

        Map<String, Object> followData = new HashMap<>();
        followData.put("followerEmail", currentUser.getEmail());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add a doc in the trainer's "followers" subcollection
        db.collection("users")
                .document(trainerId)
                .collection("followers")
                .document(currentUserId)
                .set(followData) // can be empty or store some data (timestamp, etc.)
                .addOnSuccessListener(aVoid -> {
                    followButton.setText("Unfollow");
                    Toast.makeText(requireContext(), "You're now following this trainer!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to follow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowTrainer() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(trainerId)
                .collection("followers")
                .document(currentUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    followButton.setText("Follow");
                    Toast.makeText(requireContext(), "Unfollowed!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPosts() {
        if (trainerId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listen for real-time updates from Firestore
        db.collection("users")
                .document(trainerId)
                .collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING) // optional, if you have a timestamp field
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        // If logged out, ignore the error (so i don't get the toast message on sign-out)
                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            postAdapter.setData(Collections.emptyList());
                            return;
                        }

                        Toast.makeText(requireContext(),
                                "Error loading posts: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        // Convert QuerySnapshot to a list of PostModel
                        List<TrainerProfileFragment.PostModel> updatedPostList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            TrainerProfileFragment.PostModel post = doc.toObject(TrainerProfileFragment.PostModel.class);
                            if (post != null) {
                                post.setDocId(doc.getId());
                                updatedPostList.add(post);
                            }
                        }
                        // Update adapter data
                        postAdapter.setData(updatedPostList);
                    }
                });

    }

    // ------------------ Rating Methods ------------------

    // Checks if the current user already rated this trainer and sets the RatingBar accordingly.
    private void checkIfAlreadyRated() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(trainerId)
                .collection("ratings")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double ratingVal = documentSnapshot.getDouble("rating");
                        if (ratingVal != null) {
                            ratingBar.setRating(ratingVal.floatValue());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Optionally handle the error.
                });
    }

    // Updates (or creates) the user's rating for the trainer.
    private void updateUserRating(float rating) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(trainerId)
                .collection("ratings")
                .document(currentUserId)
                .set(ratingData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Listens for changes in the ratings subcollection and updates the average rating and count.
    private void loadRatings() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(trainerId)
                .collection("ratings")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        // If logged out, ignore the error (so i don't get the toast message on sign-out)
                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            postAdapter.setData(Collections.emptyList());
                            return;
                        }

                        Toast.makeText(requireContext(),
                                "Error loading ratings: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        int ratingCount = value.size();
                        float totalRating = 0;
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Double ratingVal = doc.getDouble("rating");
                            if (ratingVal != null) {
                                totalRating += ratingVal.floatValue();
                            }
                        }
                        float averageRating = ratingCount > 0 ? totalRating / ratingCount : 0;
                        ratingSummary.setText("Average Rating: " + averageRating + " (" + ratingCount + " ratings)");
                    }
                });
    }


}
