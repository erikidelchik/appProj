package com.example.myapplication;

public class RegisterValidator {
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
