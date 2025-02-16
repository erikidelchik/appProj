package com.example.myapplication;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class RegisterUITest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule = new ActivityScenarioRule<>(RegisterActivity.class);

    @Test
    public void testSuccessfulRegistration() {
        // Enter email
        onView(withId(R.id.input_email))
                .perform(typeText("testuser@example.com"), closeSoftKeyboard());

        // Enter username
        onView(withId(R.id.input_username))
                .perform(typeText("testuser"), closeSoftKeyboard());

        // Enter password
        onView(withId(R.id.input_pass))
                .perform(typeText("TestPassword123"), closeSoftKeyboard());

        // Enter confirm password
        onView(withId(R.id.input_passConfrm))
                .perform(typeText("TestPassword123"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.register_button))
                .perform(click());

        // Check for success message (assuming Toast appears)
        onView(withText("Account created successfully!"))
                .inRoot(new ToastMatcher()) // Custom matcher for Toast messages
                .check(matches(isDisplayed()));

        // Check if login screen is displayed (if Login screen opens)
        onView(withId(R.id.login_button)) // Make sure this ID exists in Login Activity
                .check(matches(isDisplayed()));
    }
}
