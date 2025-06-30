package com.epam.gym_crm.cucumber.steps;

import com.epam.gym_crm.entity.User;

import java.util.HashMap;
import java.util.Map;

/**
 * A shared context to pass data between Cucumber step definitions.
 * This is effectively a singleton for the duration of a test run.
 */

public class TestContext {

    public static final TestContext CONTEXT = new TestContext();

    private String authToken;
    private Map<String, String> userPasswords = new HashMap<>(); // username -> plain_password
    private Map<String, User> createdUsers = new HashMap<>(); // username -> User entity

    private TestContext() {
        // Private constructor to enforce singleton
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Map<String, String> getUserPasswords() {
        return userPasswords;
    }

    public void setUserPasswords(Map<String, String> userPasswords) {
        this.userPasswords = userPasswords;
    }

    public Map<String, User> getCreatedUsers() {
        return createdUsers;
    }

    public void setCreatedUsers(Map<String, User> createdUsers) {
        this.createdUsers = createdUsers;
    }

    public void reset() {
        authToken = null;
        userPasswords.clear();
        createdUsers.clear();
    }

    public void addUserData(String username, String plainPassword, User user) {
        userPasswords.put(username, plainPassword);
        createdUsers.put(username, user);
    }

    public String getPlainPassword(String username) {
        return userPasswords.get(username);
    }
}