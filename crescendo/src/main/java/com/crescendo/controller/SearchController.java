package com.crescendo.controller;

import com.crescendo.db.Database;

import java.sql.SQLException;
import java.util.List;

/**
 * Owned by Ege Yiğit Yıldırım (Search, Database & Integration).
 * Mediates between the Search Page and Database.
 */
public class SearchController {

    public List<Database.SearchResult> searchMusicItem(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            return Database.searchMusicItem(query.strip());
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
