package com.crescendo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Stub owned by Mustafa Ziya Akyol (Music Items & Listen Later).
 * Abstract base for Album and Song: shared title/rating/review state.
 */
public abstract class MusicItem {

    protected int itemId;
    protected String title;
    protected double averageRating;
    protected final Artist artist;
    protected final List<Review> reviews = new ArrayList<>();

    protected MusicItem(int itemId, String title, Artist artist) {
        this.itemId = itemId;
        this.title = title;
        this.artist = artist;
        this.averageRating = 0.0;
    }

    public void addReview(Review review) {
        if (review != null) {
            reviews.add(review);
            Collections.sort(reviews);
            calculateAverageRating();
        }
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        calculateAverageRating();
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public double calculateAverageRating() {
        if (reviews.isEmpty()) {
            averageRating = 0.0;
            return averageRating;
        }
        int sum = 0;
        for (Review review : reviews) {
            sum += review.getStarRating();
        }
        averageRating = (double) sum / reviews.size();
        return averageRating;
    }

    public abstract String getItemType();

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public Artist getArtist() {
        return artist;
    }

    public double getAverageRating() {
        return averageRating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicItem other)) return false;
        return itemId == other.itemId && getItemType().equals(other.getItemType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, getItemType());
    }
}
