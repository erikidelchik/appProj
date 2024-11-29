package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.recaptcha.internal.zzaq;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class Register extends AppCompatActivity {

    private Button register_button;
    private TextView email, username, password, passwordConform;

    private String email_p, username_p, password_p, passwordConform_p;

    FirebaseAuth auth;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        register_button = findViewById(R.id.register_button);

        email = findViewById(R.id.input_email);
        username = findViewById(R.id.input_username);
        password = findViewById(R.id.input_pass);
        passwordConform = findViewById(R.id.input_passConfrm);


        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email_p = email.getText().toString();
                username_p = username.getText().toString();
                password_p = password.getText().toString();
                passwordConform_p = passwordConform.getText().toString();

                //if all fields are valid
                String fieldsResult = checkIfAllFieldsValid(email_p, username_p, password_p, passwordConform_p);

                if (!fieldsResult.equals("all valid")) {
                    Toast.makeText(Register.this, fieldsResult, Toast.LENGTH_SHORT).show();
                    return;
                }
                register_button.setEnabled(false);
                //check if username exist in the database
                database.collection("users")
                        .whereEqualTo("username", username_p)
                        .get() //execute the query
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                register_button.setEnabled(true);

                                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                    Toast.makeText(Register.this, "username already exist", Toast.LENGTH_SHORT).show();
                                } else {
                                    registerUser(email_p, username_p, password_p);
                                }
                            }
                        });

            }
        });

    }

    private void registerUser(String email, String username, String password) {

        //firebase func to register new user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                // Add user to Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("email", email);

                                database.collection("users")
                                        .add(userData)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(Register.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                // Navigate to Login
                                                Intent intent = new Intent(Register.this, Login.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public String checkIfAllFieldsValid(String email, String username, String password, String confPass) {
        if (!email.isEmpty() && !username.isEmpty() && !password.isEmpty() && !confPass.isEmpty()) {
            if (password.equals(confPass)) {
                return "all valid";
            } else {
                return "passwords are not matching";
            }
        } else {
            return "all fields must be filled";
        }

    }

}