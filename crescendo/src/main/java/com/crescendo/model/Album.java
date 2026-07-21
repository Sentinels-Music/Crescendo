package com.crescendo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub owned by Mustafa Ziya Akyol (Music Items & Listen Later).
 */
public class Album extends MusicItem implements ListenLaterItem {

    private final int releaseYear;
    private final List<Song> trackList = new ArrayList<>();

    public Album(int itemId, String title, Artist artist, int releaseYear) {
        super(itemId, title, artist);
        this.releaseYear = releaseYear;
    }

    public void addSong(Song song) {
        trackList.add(song);
    }

    public void removeSong(Song song) {
        trackList.remove(song);
    }

    public List<Song> getTrackList() {
        return trackList;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    @Override
    public String getItemType() {
        return "Album";
    }

    @Override
    public void addToListenLater(User user) {
        user.addToListenLater(this);
    }

    @Override
    public void removeFromListenLater(User user) {
        user.removeFromListenLater(this);
    }

    @Override
    public boolean isInListenLater(User user) {
        return user.getListenLaterItems().contains(this);
    }
}
