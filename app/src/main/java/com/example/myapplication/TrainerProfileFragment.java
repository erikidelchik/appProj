package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainerProfileFragment extends Fragment {

    ImageView profPic;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FloatingActionButton change_pic_button;

    SharedPreferences prefs;
    RelativeLayout loadingOverlay;

    EditText phone_number_field;
    Button save_button;
    TextView name_text;

    // For adding posts
    EditText postContentEditText;
    Button addPostButton;

    private TextView ratingSummary;

    private RecyclerView trainerPostsRecyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> postList = new ArrayList<>();

    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        loadingOverlay.setVisibility(View.VISIBLE);
                        profPic.setImageURI(uri);
                        uploadImageToFirebase(uri);
                    }
                } else {
                    Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views (similar to your ProfileFragment)
        profPic = view.findViewById(R.id.profilePic);
        change_pic_button = view.findViewById(R.id.changeProfilePicButton);
        loadingOverlay = requireActivity().findViewById(R.id.loadingOverlay);
        save_button = view.findViewById(R.id.saveButton);
        phone_number_field = view.findViewById(R.id.phone_number);
        name_text = view.findViewById(R.id.nameTextView);

        // Trainer-specific views for creating posts
        postContentEditText = view.findViewById(R.id.postContentEditText);
        addPostButton = view.findViewById(R.id.addPostButton);

        ratingSummary = view.findViewById(R.id.ratingSummary);

        prefs = requireContext().getSharedPreferences("profilePictures", Context.MODE_PRIVATE);

        SharedPreferences prefs2 = requireContext().getSharedPreferences("phoneNumbers", Context.MODE_PRIVATE);

        // Set user's name from SharedPreferences
        if (currentUser != null) {
            String name = prefs.getString(currentUser.getUid() + "fullname", null);
            if (name != null) {
                name_text.setText(name);
            }
        }

        // Initialize RecyclerView
        trainerPostsRecyclerView = view.findViewById(R.id.trainerPostsRecyclerView);

        // Create adapter and set to RecyclerView
        postAdapter = new PostAdapter(postList,currentUser.getUid(),true);
        trainerPostsRecyclerView.setAdapter(postAdapter);

        // Optionally set a LayoutManager
        trainerPostsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Now load the posts (or listen for them)
        loadPosts();

        loadRatings();

        // Save button logic
        save_button.setOnClickListener(v -> {
            String phoneNum = phone_number_field.getText().toString();
            if (!phoneNum.isEmpty()) {
                prefs2.edit().putString(currentUser.getUid() + "phoneNum", phoneNum).apply();
            }
            else {
                Toast.makeText(requireContext(), "Cannot save an empty name!", Toast.LENGTH_SHORT).show();
            }
        });

        // Load profile picture from SharedPreferences or Firestore
        if (currentUser != null) {
            String profilePictureUrl = prefs.getString(currentUser.getUid() + "profilePictureUrl", null);
            if (profilePictureUrl != null) {
                Glide.with(requireContext())
                        .load(profilePictureUrl)
                        .into(profPic);
            }
        }

        // Change profile pic button
        change_pic_button.setOnClickListener(v -> {
            if (currentUser != null) {
                ImagePicker.with(requireActivity())
                        .crop(1f, 1f)
                        .compress(512)
                        .maxResultSize(256, 256)
                        .createIntent(intent -> {
                            imagePickerLauncher.launch(intent);
                            return null;
                        });
            }
            else {
                Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Post button logic (Trainer-only functionality)
        addPostButton.setOnClickListener(v -> {
            if (currentUser != null) {
                String postContent = postContentEditText.getText().toString().trim();
                if (!postContent.isEmpty()) {
                    addNewPost(currentUser.getUid(), postContent);
                } else {
                    Toast.makeText(requireContext(), "Post content cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String userId = currentUser.getUid();
        StorageReference imageRef = storageRef.child("images/" + userId);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveImageUrlToFirestore(userId, downloadUrl);
                    }).addOnFailureListener(e -> {
                        loadingOverlay.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveImageUrlToFirestore(String userId, String downloadUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .update("profilePicture", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    prefs.edit().putString(currentUser.getUid() + "profilePictureUrl", downloadUrl).apply();
                    Glide.with(requireContext())
                            .load(downloadUrl)
                            .into(profPic);

                    // update nav bar pic
                    ((MainMenuActivity) requireActivity()).setProfilePictureInNavBar();

                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Trainer-specific method to add a new post to Firestore.
     */
    private void addNewPost(String trainerId, String postContent) {
        // Show a simple loading overlay if you want
        loadingOverlay.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Build a post object (you can customize the fields)
        PostModel newPost = new PostModel(trainerId, postContent, System.currentTimeMillis());

        // Save to a subcollection "posts" under this trainer's document:
        db.collection("users")
                .document(trainerId)
                .collection("posts")
                .add(newPost)
                .addOnSuccessListener(documentReference -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Post added!", Toast.LENGTH_SHORT).show();
                    // Clear the input
                    postContentEditText.setText("");
                    // You might refresh a RecyclerView here to show the new post
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPosts() {
        if (currentUser == null) return;

        String trainerId = currentUser.getUid();
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
                        List<PostModel> updatedPostList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            PostModel post = doc.toObject(PostModel.class);
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

    /**
     * Simple model class for a Post
     */
    public static class PostModel {
        private String trainerId;
        private String content;
        private long timestamp;
        private String docId;

        // Empty constructor needed for Firestore’s automatic data mapping
        public PostModel() {}

        public PostModel(String trainerId, String content, long timestamp) {
            this.trainerId = trainerId;
            this.content = content;
            this.timestamp = timestamp;
        }

        // Getters and setters (FireStore uses them to map data)
        public String getTrainerId() { return trainerId; }
        public void setTrainerId(String trainerId) { this.trainerId = trainerId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public String getDocId() { return docId; }
        public void setDocId(String docId) { this.docId = docId; }
    }

    private void loadRatings() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(currentUser.getUid())
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
