package com.crescendo.model;

import java.util.List;

/**
 * Stub owned by Metehan Karadeniz (User & Authentication).
 */
public class VerifiedUser extends User {

    public VerifiedUser(int userId, String username, String passwordHash) {
        super(userId, username, passwordHash);
    }

    public Artist addNewArtist(String name, String description) {
        return new Artist(0, name, description);
    }

    public Album addAlbum(Artist artist, Album album) {
        artist.addMusicItem(album);
        return album;
    }

    public Song addSong(Artist artist, Song song) {
        artist.addMusicItem(song);
        return song;
    }

    public List<String> getVerifiedPerks() {
        return List.of("Add new artists", "Add albums and songs", "Prioritized reviews");
    }
}
