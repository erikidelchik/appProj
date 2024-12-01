package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ProfileFragment extends Fragment {

    ImageView profPic;

    FirebaseAuth auth;
    FirebaseUser currentUser;

    FloatingActionButton change_pic_button;

    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        profPic.setImageURI(uri);
                        uploadImageToFirebase(uri);
                    }
                }
                else {
                    Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
    );



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            loadProfilePicture(currentUser.getUid());
        }

        profPic = view.findViewById(R.id.profilePic);
        change_pic_button = view.findViewById(R.id.changeProfilePicButton);

        change_pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUser!=null) {
                    ImagePicker.with(requireActivity())
                            .crop()
                            .compress(1024)
                            .maxResultSize(1080, 1080)
                            .createIntent(intent -> {
                                imagePickerLauncher.launch(intent); // Launch the picker and wait for the result
                                return null;
                            });
                }
                else{
                    Toast.makeText(requireContext(),"not signed in",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }


        // Reference to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Define the path where the image will be stored
        String userId = currentUser.getUid();
        StorageReference imageRef = storageRef.child("images/" + userId + ".jpg");

        // Upload the image to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveImageUrlToFirestore(userId, downloadUrl); // Save the URL to Firestore
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveImageUrlToFirestore(String userId, String downloadUrl) {
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Save the image URL in the user's document
        db.collection("users").document(userId)
                .update("profilePicture", downloadUrl) // Update or create the profilePicture field
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfilePicture(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profilePictureUrl = documentSnapshot.getString("profilePicture");
                        if (profilePictureUrl != null) {
                            // Load the image using Glide
                            Glide.with(requireContext())
                                    .load(profilePictureUrl)
                                    .into(profPic);
                        }
                    }
                });
//                .addOnFailureListener(e -> {
//                    Toast.makeText(requireContext(), "Failed to load profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
    }




}