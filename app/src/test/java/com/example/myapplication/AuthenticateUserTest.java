package com.example.myapplication;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateUserTest {

    @Mock
    private FirebaseAuth mockFirebaseAuth;

    @Mock
    private Task<AuthResult> mockAuthResultTask;

    private AuthRepository authRepository;

    @Before
    public void setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this);

        // Create the repository with the mocked FirebaseAuth
        authRepository = new AuthRepository(mockFirebaseAuth);
    }

    @Test
    public void testAuthenticateUser_Success() {
        // 1) Stub signInWithEmailAndPassword(...) to return a mocked Task
        when(mockFirebaseAuth.signInWithEmailAndPassword("some@test.com", "password123"))
                .thenReturn(mockAuthResultTask);

        // 2) Stub the Task to trigger onComplete() with success
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            // Stub isSuccessful() to return true
            when(mockAuthResultTask.isSuccessful()).thenReturn(true);
            listener.onComplete(mockAuthResultTask);
            return null;
        }).when(mockAuthResultTask).addOnCompleteListener(any());

        // 3) Call the repository method
        Task<AuthResult> resultTask = authRepository.authenticateUser("some@test.com", "password123");
        resultTask.addOnCompleteListener(
                task -> assertTrue(task.isSuccessful()));

    }

    @Test
    public void testAuthenticateUser_Failure() {
        when(mockFirebaseAuth.signInWithEmailAndPassword("wrong@test.com", "wrongpassword"))
                .thenReturn(mockAuthResultTask);

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            // Stub isSuccessful() to return false
            when(mockAuthResultTask.isSuccessful()).thenReturn(false);
            listener.onComplete(mockAuthResultTask);
            return null;
        }).when(mockAuthResultTask).addOnCompleteListener(any());

        Task<AuthResult> resultTask = authRepository.authenticateUser("wrong@test.com", "wrongpassword");


        assertFalse(resultTask.isSuccessful());
    }
}
