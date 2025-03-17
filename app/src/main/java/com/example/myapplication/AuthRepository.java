package com.example.myapplication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * This class contains the pure logic for authenticating users
 * using FirebaseAuth.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public FirebaseAuth getFirebaseAuth(){
        return firebaseAuth;
    }


    /**
     * Authenticate a user with email and password, returning the Task<AuthResult>.
     *
     * (In production code, you might want a callback interface instead of returning Task<>.)
     */
    public Task<AuthResult> authenticateUser(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }
}