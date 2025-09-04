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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ProfileFragment extends Fragment {

    ImageView profPic;

    FirebaseAuth auth;
    FirebaseUser currentUser;

    FloatingActionButton change_pic_button;

    SharedPreferences prefs;
    RelativeLayout loadingOverlay;

    EditText full_name_field;
    Button save_button;
    TextView name_text;


    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        //start loading screen
                        loadingOverlay.setVisibility(View.VISIBLE);
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

        //initialize firebase auth and current user
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        //initialize views
        profPic = view.findViewById(R.id.profilePic);
        change_pic_button = view.findViewById(R.id.changeProfilePicButton);
        loadingOverlay = requireActivity().findViewById(R.id.loadingOverlay);
        save_button = view.findViewById(R.id.saveButton);
        full_name_field = view.findViewById(R.id.full_name);
        name_text = view.findViewById(R.id.nameTextView);

        prefs = requireContext().getSharedPreferences("profilePictures", Context.MODE_PRIVATE);

        //set user's name
        String name = prefs.getString(currentUser.getUid() + "fullname",null);
        if(name!=null){
            name_text.setText(name);
        }
        
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = full_name_field.getText().toString();
                //check if field is empty
                if(!newName.isEmpty()) {
                    name_text.setText(newName);
                    prefs.edit().putString(currentUser.getUid() + "fullname", newName).apply();
                }
                else{
                    Toast.makeText(requireContext(),"cant save an empty text",Toast.LENGTH_SHORT).show();
                }
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



        change_pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUser!=null) {
                    ImagePicker.with(requireActivity())
                            .crop(1f,1f)
                            .compress(512)
                            .maxResultSize(256, 256)
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
        StorageReference imageRef = storageRef.child("images/" + userId);

        // Upload the image to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveImageUrlToFirestore(userId, downloadUrl); // Save the URL to Firestore
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
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //start loading screen


        // Save the image URL in the user's document
        db.collection("users").document(userId)
                .update("profilePicture", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    // Update the cached URL
                    prefs.edit().putString(currentUser.getUid() + "profilePictureUrl", downloadUrl).apply();
                    // Load the new profile picture
                    Glide.with(requireContext())
                            .load(downloadUrl)
                            .into(profPic);


                    ((MainMenuActivity) requireActivity()).setProfilePictureInNavBar();

                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}