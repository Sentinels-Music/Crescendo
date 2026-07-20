package model;

import java.util.List;
import java.util.ArrayList;

public class Album extends MusicItem {
    private int releaseYear;
    private List<Song> trackList;

    public Album(int itemId, String title, int releaseYear, double averageRating) {
        super(itemId, title, averageRating);
        this.releaseYear = releaseYear;
        this.trackList = new ArrayList<>();
    }

    public int getReleaseYear() {
        return releaseYear;
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

    public Album getItemType() {
        return this;
    }
}
