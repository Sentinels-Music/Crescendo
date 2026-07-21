package com.crescendo.controller;

import com.crescendo.db.Database;
import com.crescendo.model.Album;
import com.crescendo.model.Artist;
import com.crescendo.model.Song;
import com.crescendo.model.User;
import com.crescendo.model.VerifiedUser;

import java.sql.SQLException;
import java.util.List;

/**
 * Owned by Mustafa Ziya Akyol (Music Items & Listen Later).
 * Mediates between the Song/Album/Listen Later screens and the Music Item domain classes.
 */
public class MusicController {

    private static RuntimeException wrap(SQLException e) {
        return new RuntimeException("Database error: " + e.getMessage(), e);
    }

    public Song getSong(int songId) {
        try {
            return Database.loadSong(songId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public Album getAlbum(int albumId) {
        try {
            return Database.loadAlbum(albumId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /** The album a song appears on, or null for a standalone single. */
    public Album getContainingAlbum(int songId) {
        try {
            return Database.findContainingAlbum(songId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /** Adds a new album to an artist's discography. Only a VerifiedUser may call this. */
    public void addAlbum(VerifiedUser requester, Artist artist, String title, int releaseYear) {
        if (requester == null) {
            throw new SecurityException("Only verified users can add albums.");
        }
        if (artist == null) {
            throw new IllegalArgumentException("Artist is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Album title cannot be empty.");
        }
        if (releaseYear < 1900 || releaseYear > 2100) {
            throw new IllegalArgumentException("Enter a valid release year.");
        }
        try {
            Database.insertAlbum(artist.getArtistId(), title.strip(), releaseYear);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /** Adds a new song to an artist's catalog, optionally attached to one of their albums. */
    public void addSong(VerifiedUser requester, Artist artist, Album album, String title, int durationSeconds) {
        if (requester == null) {
            throw new SecurityException("Only verified users can add songs.");
        }
        if (artist == null) {
            throw new IllegalArgumentException("Artist is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Song title cannot be empty.");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Enter a valid duration.");
        }
        try {
            Database.insertSong(artist.getArtistId(), album == null ? null : album.getItemId(), title.strip(), durationSeconds);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void rateItem(String itemType, int itemId, User reviewer, int starRating, String comment) {
        if (starRating < 1 || starRating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }
        try {
            Database.rateItem(itemType, itemId, reviewer.getUserId(), reviewer instanceof VerifiedUser,
                    starRating, comment);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void addToListenLater(User user, String itemType, int itemId) {
        try {
            Database.addToListenLater(user.getUserId(), itemType, itemId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void removeFromListenLater(User user, String itemType, int itemId) {
        try {
            Database.removeFromListenLater(user.getUserId(), itemType, itemId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public boolean isInListenLater(User user, String itemType, int itemId) {
        try {
            return Database.isInListenLater(user.getUserId(), itemType, itemId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public List<Database.ListenLaterEntry> getListenLater(User user) {
        try {
            return Database.getListenLater(user.getUserId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }
}
