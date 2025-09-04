package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;

    private View headerView;
    private FirebaseUser currentUser;
    private boolean isTrainer;
    private ImageView profPic;
    private RelativeLayout loadingOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();


        NavigationView navigationView = findViewById(R.id.nav_view);

        // Access the header view
        headerView = navigationView.getHeaderView(0); // Get the first header view
        TextView uname = headerView.findViewById(R.id.nameOfUser);

        profPic = headerView.findViewById(R.id.profilePicNavBar);

        loadingOverlay = findViewById(R.id.loadingOverlay);

        //assign isTrainer to true or false
        getTrainerStatus();

        // Check if the user is not null and set the username
        setUsernameInNavBar(db,currentUser,uname);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        getUserToken();
        
        loadingOverlay.setVisibility(View.VISIBLE);
        //update profile pic
        setProfilePictureInNavBar();
        loadingOverlay.setVisibility(View.GONE);



    }

    //assign isTrainer to true or false
    private void getTrainerStatus(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        isTrainer = Boolean.TRUE.equals(documentSnapshot.getBoolean("isTrainer"));
                    }
                });
    }

    public void setUsernameInNavBar(FirebaseFirestore db,FirebaseUser currentUser,TextView uname){
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the authenticated user's UID
            Log.d("DEBUG", "User ID: " + userId);


            // Get the user's document from Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the username from Firestore
                            String username = documentSnapshot.getString("username");

                            // Set the username to the TextView
                            if(isTrainer)
                                uname.setText(username + " (Trainer)");
                            else
                                uname.setText(username + " (Training)");
                        }
                        else{
                            Log.d("DEBUG", "Document does not exist for UID: " + userId);
                            uname.setText("not logged in");
                        }
                    });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_profile && !isTrainer){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        }

        else if(item.getItemId()==R.id.nav_profile && isTrainer){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TrainerProfileFragment()).commit();
        }

        else if(item.getItemId()==R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
        else if(item.getItemId()==R.id.nav_settings){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        }
        else if(item.getItemId()==R.id.nav_about){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        }
        else if(item.getItemId()==R.id.nav_logout){
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            //clear activity stack
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        //GravityCompat.START defines which drawer to close (start- most left drawer)
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setProfilePictureInNavBar() {
        String userID = currentUser.getUid();
        SharedPreferences prefs = getSharedPreferences("profilePictures", Context.MODE_PRIVATE);
        String profilePictureUrl = prefs.getString(userID + "profilePictureUrl", null);
        if (profilePictureUrl != null) {
            Glide.with(this)
                    .load(profilePictureUrl)
                    .into(profPic);
        }
        else{
            loadProfilePicture(userID);
        }
    }

    private void loadProfilePicture(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profilePictureUrl = documentSnapshot.getString("profilePicture");
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            // Use Glide to load and cache the image
                            Glide.with(MainMenuActivity.this)
                                    .load(profilePictureUrl)
                                    .into(profPic);

                            // Save the URL in SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("profilePictures", Context.MODE_PRIVATE);
                            prefs.edit().putString(currentUser.getUid() + "profilePictureUrl", profilePictureUrl).apply();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainMenuActivity.this, "Failed to load profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getUserToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("a", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();

                    // Save token in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        String userId = auth.getCurrentUser().getUid();
                        db.collection("users")
                                .document(userId)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("a", "FCM token updated successfully"))
                                .addOnFailureListener(e -> Log.e("a", "Failed to update FCM token", e));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // If the drawer is open, close it first
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Check if the current fragment is HomeFragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof HomeFragment)) {
                // If we are NOT in HomeFragment, go to HomeFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            } else {
                // If we're already in HomeFragment, do the default back action
                super.onBackPressed();
            }
        }
    }

}
