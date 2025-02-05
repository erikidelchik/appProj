package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RegisterValidatorTest {

    @Test
    public void testAllValid() {
        RegisterValidator validator = new RegisterValidator();
        String result = validator.checkIfAllFieldsValid("user@email.com",
                "bob", "secret", "secret");
        assertEquals("all valid", result);
    }

    @Test
    public void testFieldsMissing() {
        RegisterValidator validator = new RegisterValidator();
        // Empty username
        String result = validator.checkIfAllFieldsValid("user@email.com",
                "", "secret", "secret");
        assertEquals("all fields must be filled", result);
    }

    @Test
    public void testPasswordsNotMatching() {
        RegisterValidator validator = new RegisterValidator();
        String result = validator.checkIfAllFieldsValid("john@doe.com",
                "john", "secret", "secret2");
        assertEquals("passwords are not matching", result);
    }
}
