package com.crescendo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Owned by Emir Selim Kayhan (Artist, Tag & Streaming).
 * Stores artist information, its releases and tags, follower count, and average rating,
 * per the Detailed Design Report's UML class diagram.
 */
public class Artist {

    private int artistId;
    private final String name;
    private final String description;
    private int followerCount;
    private double averageRating;

    private final List<MusicItem> releases = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();

    public Artist(int artistId, String name, String description) {
        this.artistId = artistId;
        this.name = name;
        this.description = description;
        this.followerCount = 0;
        this.averageRating = 0.0;
    }

    public void addMusicItem(MusicItem item) {
        if (item != null && !releases.contains(item)) {
            releases.add(item);
        }
    }

    public void addTag(Tag tag) {
        if (tag != null && !tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public List<Album> getAlbums() {
        List<Album> albums = new ArrayList<>();
        for (MusicItem item : releases) {
            if (item instanceof Album album) {
                albums.add(album);
            }
        }
        return albums;
    }

    public List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        for (MusicItem item : releases) {
            if (item instanceof Song song) {
                songs.add(song);
            }
        }
        return songs;
    }

    public double calculateAverageRating() {
        double total = 0;
        int ratedItems = 0;
        for (MusicItem item : releases) {
            double rating = item.calculateAverageRating();
            if (rating > 0) {
                total += rating;
                ratedItems++;
            }
        }
        // Direct ratings of the artist itself (from the Rate button) count too, not just
        // the rolled-up average of its albums/songs - otherwise rating the artist directly
        // would never move this number.
        for (Review review : reviews) {
            total += review.getStarRating();
            ratedItems++;
        }
        if (ratedItems == 0) {
            averageRating = 0.0;
        } else {
            averageRating = total / ratedItems;
        }
        return averageRating;
    }

    /** A direct rating/review of the artist as a whole (as opposed to one of its releases). */
    public void addReview(Review review) {
        if (review != null) {
            reviews.add(review);
            Collections.sort(reviews);
        }
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public double calculateReviewAverage() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (Review review : reviews) {
            sum += review.getStarRating();
        }
        return (double) sum / reviews.size();
    }

    // Called by User.followArtist()/unfollowArtist() (Metehan's part) to keep followerCount in sync.
    public void incrementFollowerCount() {
        followerCount++;
    }

    public void decrementFollowerCount() {
        if (followerCount > 0) {
            followerCount--;
        }
    }

    public int getArtistId() {
        return artistId;
    }

    // Assigned by the persistence layer once the artist is inserted into the database.
    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist other)) return false;
        return artistId == other.artistId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(artistId);
    }

    @Override
    public String toString() {
        return name;
    }
}
