package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private AuthRepository authRepository; //for testing mock
    private TextView username;
    private TextView password;
    private TextView registerText;
    TextView error_text;
    private Button loginButton;

//    FirebaseAuth auth;
    private FirebaseFirestore database;
    RelativeLayout loadingOverlay;

    // Google Sign-In variables
    private SignInButton googleSignInButton;
    private static final int RC_SIGN_IN = 123;


    @Override
    public void onStart() {
        super.onStart();
        loadingOverlay.setVisibility(View.VISIBLE);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = authRepository.getFirebaseAuth().getCurrentUser();
        if(currentUser!=null){
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        }
        loadingOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.input_username);
        password = findViewById(R.id.input_pass);
        loginButton = findViewById(R.id.login_button);
        registerText = findViewById(R.id.register_text);
        error_text = findViewById(R.id.error_text_view);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        googleSignInButton = findViewById(R.id.google_sign_in_button);

        authRepository = new AuthRepository(FirebaseAuth.getInstance());

//        auth = FirebaseAuth.getInstance();

        database = FirebaseFirestore.getInstance();

        googleSignInButton.setOnClickListener(view -> startFirebaseUIAuth());

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username_p = username.getText().toString().trim();
                String password_p = password.getText().toString().trim();

                if (username_p.isEmpty() || password_p.isEmpty()) {
                    error_text.setText("Please fill in all fields");
                    return;
                }

                loadingOverlay.setVisibility(View.VISIBLE);
                // Get the email associated with the username
                database.collection("users")
                        .whereEqualTo("username", username_p)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                loadingOverlay.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Username found
                                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                        String email = document.getString("email");

                                        // Login with email and password
                                        authenticateUser(email, password_p);
                                    } else {
                                        // Username not found
                                        error_text.setText("wrong username or password");
                                    }
                                }
                                else {
                                    error_text.setText("Error: " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });


    }

    /**
     * Starts the FirebaseUI sign-in flow.
     */
    private void startFirebaseUIAuth() {
        // Choose authentication providers. In this case, only Google.
        List<AuthUI.IdpConfig> providers = List.of(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                // Optionally, customize the sign-in screen:
                //.setLogo(R.drawable.my_logo) // Set logo drawable
                //.setTheme(R.style.MyCustomTheme) // Set theme
                .build();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle the result from the FirebaseUI sign-in intent.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadingOverlay.setVisibility(View.GONE);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Get Firestore instance
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String uid = user.getUid();

                    // Check if a user document exists for this uid
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    // User document doesn't exist, create one with default values
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("email", user.getEmail());
                                    // Use displayName as username if available, or a default value
                                    userData.put("username", user.getDisplayName() != null ? user.getDisplayName() : "Anonymous");
                                    // Set additional fields as needed, for example:
                                    userData.put("isTrainer", false);

                                    db.collection("users").document(uid).set(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(LoginActivity.this, "Error adding user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                                else {
                                    // User document already exists, proceed to main activity
                                    Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(LoginActivity.this, "Error checking user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
                error_text.setText("Google sign in failed");
            }
        }
    }

    void authenticateUser(String email, String password) {
        authRepository.authenticateUser(email,password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        error_text.setText("");
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    else {
                        // Login failed
                        error_text.setText("wrong username or password");
                    }
                });
    }
}