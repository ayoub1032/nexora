package tn.esprit.utils;

import tn.esprit.entities.User;

/**
 * Singleton class for managing user session
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Login a user and set the current session
     * 
     * @param user The user to login
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /**
     * Logout the current user
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Get the current logged in user
     * 
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * 
     * @return true if user is authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
