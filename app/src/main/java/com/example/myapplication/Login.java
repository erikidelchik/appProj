package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class Login extends AppCompatActivity {

    private TextView username, password, registerText;
    private Button loginButton;

    private FirebaseAuth auth;
    private FirebaseFirestore database;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null){
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.input_username);
        password = findViewById(R.id.input_pass);
        loginButton = findViewById(R.id.login_button);
        registerText = findViewById(R.id.register_text);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,Register.class);
                startActivity(intent);
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username_p = username.getText().toString().trim();
                String password_p = password.getText().toString().trim();

                if (username_p.isEmpty() || password_p.isEmpty()) {
                    Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginButton.setEnabled(false);
                // Get the email associated with the username
                database.collection("users")
                        .whereEqualTo("username", username_p)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                loginButton.setEnabled(true);
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Username found
                                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                        String email = document.getString("email");

                                        // Login with email and password
                                        authenticateUser(email, password_p);
                                    } else {
                                        // Username not found
                                        Toast.makeText(Login.this, "Username not found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    // Firestore query failed
                                    Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void authenticateUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MainMenu.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(Login.this, "wrong username or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}