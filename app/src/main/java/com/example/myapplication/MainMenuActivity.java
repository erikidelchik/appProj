package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;

    private View headerView;
    private FirebaseUser currentUser;

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

        //update profile pic
        setProfilePictureInNavBar();

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
                            boolean isTrainer = Boolean.TRUE.equals(documentSnapshot.getBoolean("isTrainer"));

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
        if(item.getItemId()==R.id.nav_profile){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
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
        ImageView profPic = headerView.findViewById(R.id.profilePicNavBar);
        if (profilePictureUrl != null) {
            Glide.with(this)
                    .load(profilePictureUrl)
                    .into(profPic);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
            super.onBackPressed();
    }
}