package com.crescendo.controller;

import com.crescendo.db.Database;
import com.crescendo.model.Artist;
import com.crescendo.model.Song;
import com.crescendo.model.Tag;
import com.crescendo.model.User;
import com.crescendo.model.VerifiedUser;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Owned by Emir Selim Kayhan (Artist, Tag & Streaming).
 * Mediates between the Artist Page / Discover Page UI and the Artist/Tag domain classes.
 */
public class ArtistController {

    private static RuntimeException wrap(SQLException e) {
        return new RuntimeException("Database error: " + e.getMessage(), e);
    }

    public Artist addNewArtist(VerifiedUser requester, String name, String description) {
        if (requester == null) {
            throw new SecurityException("Only verified users can add new artists.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Artist name cannot be empty.");
        }
        try {
            return Database.insertArtist(name.strip(), description == null ? "" : description.strip());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public Tag addTag(Artist artist, String tagName) {
        if (artist == null) {
            throw new IllegalArgumentException("Artist is required.");
        }
        if (tagName == null || tagName.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty.");
        }
        try {
            Tag tag = Database.addTag(artist.getArtistId(), tagName.strip());
            artist.addTag(tag);
            return tag;
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void removeTag(Artist artist, Tag tag) {
        if (artist == null || tag == null) {
            return;
        }
        try {
            Database.removeTag(artist.getArtistId(), tag.getTagId());
            artist.removeTag(tag);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public String getStreamingLink(Song song, String platform) {
        return song == null || platform == null ? null : song.getStreamingLink(platform);
    }

    /** Opens a streaming link in the user's default browser; returns false instead of throwing. */
    public boolean open(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public Artist getArtistById(int artistId) {
        try {
            return Database.loadArtist(artistId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public List<Artist> getAllArtists() {
        try {
            return Database.getAllArtists();
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public List<Tag> getAllTags() {
        try {
            return Database.getAllTags();
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public List<Artist> getArtistsByTag(Tag tag) {
        List<Artist> matches = new ArrayList<>();
        for (Artist artist : getAllArtists()) {
            if (artist.getTags().contains(tag)) {
                matches.add(artist);
            }
        }
        return matches;
    }

    public void rateArtist(Artist artist, User reviewer, int starRating, String comment) {
        if (artist == null || reviewer == null) {
            throw new IllegalArgumentException("Artist and reviewer are required.");
        }
        if (starRating < 1 || starRating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }
        try {
            Database.rateArtist(artist.getArtistId(), reviewer.getUserId(), reviewer instanceof VerifiedUser,
                    starRating, comment);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void followArtist(User user, Artist artist) {
        try {
            Database.followArtist(user.getUserId(), artist.getArtistId());
            user.followArtist(artist);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void unfollowArtist(User user, Artist artist) {
        try {
            Database.unfollowArtist(user.getUserId(), artist.getArtistId());
            user.unfollowArtist(artist);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public boolean isFollowingArtist(User user, Artist artist) {
        if (user == null) {
            return false;
        }
        try {
            return Database.isFollowingArtist(user.getUserId(), artist.getArtistId());
        } catch (SQLException e) {
            throw wrap(e);
        }
    }
}
