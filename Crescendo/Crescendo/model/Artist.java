package Crescendo.model;

import java.util.ArrayList;


public class Artist {

    private int artistId; // 0 until it has been saved to / loaded from the database
    private String name;
    private String description;
    private boolean verified; 
    private int followerCount;
    private double averageRating;
    private boolean following; 

    private ArrayList<Album> albums;
    private ArrayList<Song> songs;
    private ArrayList<Tag> tags;

    public Artist(int artistId, String name, String description) {
        this.artistId = artistId;
        this.name = name;
        this.description = description;
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.verified = false;
        this.followerCount = 0;
        this.averageRating = 0.0;
        this.following = false;
    }

   
    public static Artist addNewArtist(String name, String description) {
        return new Artist(0, name, description);
    }

    // albums & songs 
    public void addAlbum(Album album) {
        if(album != null && !albums.contains(album)) {
            albums.add(album);
        }
    }

    public void addSong(Song song) {
        if(song != null && !songs.contains(song)) {
            songs.add(song);
        }
    }

    public int getAlbumCount() {
        return albums.size();
    }

    public int getSongCount() {
        return songs.size();
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    // Returns the most popular songs highest average rating first
    public ArrayList<Song> getPopularSongs(int howMany) {
        ArrayList<Song> sorted = new ArrayList<>(songs);
        sorted.sort((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()));
        if (sorted.size() > howMany) {
            return new ArrayList<>(sorted.subList(0, howMany));
        }
        return sorted;
    }

    // tags 
    public void addTag(Tag tag) {
        if (tag != null && !tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    // The main genre shown in the header subtitle, or "Unknown" if none
    public String getMainGenre() {
        if (tags.isEmpty()) {
            return "Unknown";
        }
        return tags.get(0).getName();
    }

    // following 
    public void follow() {
        if (!following) {
            following = true;
            followerCount++;
        }
    }

    public void unfollow() {
        if (following) {
            following = false;
            if (followerCount > 0) {
                followerCount--;
            }
        }
    }

    public boolean isFollowing() {
        return following;
    }

    // ratings 

    // Recalculates the average rating over the albums and songs currently loaded. 
    public double calculateAverageRating() {
        double total = 0;
        int count = 0;
        for (Album album : albums) {
            total += album.getAverageRating();
            count++;
        }
        for (Song song : songs) {
            total += song.getAverageRating();
            count++;
        }
        return count == 0 ? 0.0 : total / count;
    }

    // getters / setters 

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    @Override
    public String toString() {
        return name + (verified ? " (Verified)" : "");
    }
}
