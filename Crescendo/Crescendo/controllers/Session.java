package Crescendo.controllers;

import Crescendo.model.User;

/**
 * Holds the currently logged-in user for the running session.
 *
 * Login sets it; the Profile page and any "current user" logic read it.
 * Simple static holder so the whole app can reach the active account
 * without threading it through every screen.
 */
public final class Session {

    private static User currentUser;

    private Session() { }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}
