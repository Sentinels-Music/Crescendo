package com.crescendo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stub owned by Metehan Karadeniz (User & Authentication).
 */
public class User {

    protected final int userId;
    protected final String username;
    protected final String passwordHash;
    protected String bio = "";
    protected int reviewCount = 0;

    protected final List<User> followedUsers = new ArrayList<>();
    protected final List<Artist> followedArtists = new ArrayList<>();
    protected final List<MusicItem> listenLaterItems = new ArrayList<>();

    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public void followArtist(Artist artist) {
        if (artist != null && !followedArtists.contains(artist)) {
            followedArtists.add(artist);
            artist.incrementFollowerCount();
        }
    }

    public void unfollowArtist(Artist artist) {
        if (artist != null && followedArtists.remove(artist)) {
            artist.decrementFollowerCount();
        }
    }

    public void followUser(User other) {
        if (other != null && other.userId != this.userId && !followedUsers.contains(other)) {
            followedUsers.add(other);
        }
    }

    public void unfollowUser(User other) {
        followedUsers.remove(other);
    }

    public List<User> getFollowedUsers() {
        return followedUsers;
    }

    public void addToListenLater(MusicItem item) {
        if (item != null && !listenLaterItems.contains(item)) {
            listenLaterItems.add(item);
        }
    }

    public void removeFromListenLater(MusicItem item) {
        listenLaterItems.remove(item);
    }

    public List<MusicItem> getListenLaterItems() {
        return listenLaterItems;
    }

    public List<Artist> getFollowedArtists() {
        return followedArtists;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
