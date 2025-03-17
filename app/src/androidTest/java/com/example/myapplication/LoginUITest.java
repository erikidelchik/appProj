//package com.example.myapplication;
//
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.*;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.*;
//
//@RunWith(AndroidJUnit4.class)
//public class LoginUITest {
//
//    @Rule
//    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);
//
//    @Test
//    public void testSuccessfulLogin() {
//        // Enter email
//        onView(withId(R.id.input_email))
//                .perform(typeText("testuser@example.com"), closeSoftKeyboard());
//
//        // Enter password
//        onView(withId(R.id.input_pass))
//                .perform(typeText("TestPassword123"), closeSoftKeyboard());
//
//        // Click login button
//        onView(withId(R.id.login_button))
//                .perform(click());
//
//        // Check for success message (assuming Toast appears)
//        onView(withText("Login successful!"))
//                .inRoot(new ToastMatcher()) // Custom matcher for Toast messages
//                .check(matches(isDisplayed()));
//
//        // Check if the home screen or dashboard is displayed
//        onView(withId(R.id.dashboard_container)) // Change to actual ID in your home screen
//                .check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void testLoginWithInvalidCredentials() {
//        // Enter email
//        onView(withId(R.id.input_email))
//                .perform(typeText("wronguser@example.com"), closeSoftKeyboard());
//
//        // Enter incorrect password
//        onView(withId(R.id.input_pass))
//                .perform(typeText("WrongPassword"), closeSoftKeyboard());
//
//        // Click login button
//        onView(withId(R.id.login_button))
//                .perform(click());
//
//        // Check for error message
//        onView(withText("Invalid email or password"))
//                .inRoot(new ToastMatcher()) // Custom matcher for Toast messages
//                .check(matches(isDisplayed()));
//    }
//}
