package com.crescendo.controller;

import com.crescendo.db.Database;
import com.crescendo.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Owned by Abdullah Efe Anık (Review & Social Feed).
 * Mediates between the Home Page feed and Database.
 */
public class FeedController {

    public List<Database.FeedEntry> getFeed(User user) {
        try {
            return Database.getFeed(user.getUserId(), 50);
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public List<Database.FeedEntry> getReviewsByUser(User user) {
        try {
            return Database.getReviewsByUser(user.getUserId());
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
