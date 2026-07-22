package com.crescendo.controller;

import com.crescendo.db.Database;
import com.crescendo.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Owned by Metehan Karadeniz (User & Authentication).
 * Mediates between the Login/Register/Profile screens and Database.
 */
public class AuthController {

    private static RuntimeException wrap(SQLException e) {
        return new RuntimeException("Database error: " + e.getMessage(), e);
    }

    public User register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }
        try {
            return Database.register(username.strip(), password);
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("That username is already taken.");
            }
            throw wrap(e);
        }
    }

    public User login(String username, String password) {
        try {
            User user = Database.login(username, password);
            if (user == null) {
                throw new IllegalArgumentException("Incorrect username or password.");
            }
            return user;
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public User reload(User user) {
        try {
            return Database.reloadUser(user.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void updateBio(User user, String bio) {
        try {
            Database.updateBio(user.getUserId(), bio);
            user.setBio(bio);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public List<User> getFollowedUsers(User user) {
        try {
            return Database.getFollowedUsers(user.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public List<User> getAllOtherUsers(User user) {
        try {
            return Database.getAllUsersExcept(user.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void followUser(User user, User other) {
        Database.followUser(user.getUserId(), other.getUserId());
        user.followUser(other);
    }

    /** Users who follow this account - shown in the Followers popup on the Profile page. */
    public List<User> getFollowers(User user) {
        try {
            return Database.getFollowers(user.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void unfollowUser(User user, User other) {
        try {
            Database.unfollowUser(user.getUserId(), other.getUserId());
            user.unfollowUser(other);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public boolean isFollowingUser(User user, User other) {
        try {
            return Database.isFollowingUser(user.getUserId(), other.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public int getFollowerCount(User user) {
        try {
            return Database.getFollowerCountForUser(user.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public int computeTasteMatch(User currentUser, User otherUser) {
        try {
            return Database.computeTasteMatch(currentUser.getUserId(), otherUser.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /** Returns the shared tags behind the taste-match percentage. */
    public List<String> getSharedTags(User currentUser, User otherUser) {
        try {
            return Database.getSharedTagNames(currentUser.getUserId(), otherUser.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }
}
