package com.crescendo.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a review written by a user for an artist, album, or song.
 * Reviews are ordered by priority score in descending order. If two reviews
 * have the same priority score, the newer review comes first.
 */
public class Review implements Comparable<Review> {

    private static final int VERIFIED_BASE_SCORE = 55;
    private static final int REGULAR_BASE_SCORE = 5;
    private static final int MAX_RECENCY_BONUS = 45;
    private static final int RECENCY_INTERVAL_DAYS = 2;

    private final int reviewId;
    private final String reviewerUsername;
    private final int starRating;
    private final String comment;
    private final Date createdAt;
    private int priorityScore;
    private final boolean writtenByVerifiedUser;

    public Review(int reviewId, String reviewerUsername, int starRating, String comment,
                  boolean writtenByVerifiedUser) {
        this(reviewId, reviewerUsername, starRating, comment, new Date(), writtenByVerifiedUser);
    }

    public Review(int reviewId, String reviewerUsername, int starRating, String comment,
                  Date createdAt, boolean writtenByVerifiedUser) {
        this.reviewId = reviewId;
        this.reviewerUsername = reviewerUsername;
        this.starRating = starRating;
        this.comment = comment;

        if (createdAt == null) {
            this.createdAt = new Date();
        } else {
            this.createdAt = new Date(createdAt.getTime());
        }

        this.writtenByVerifiedUser = writtenByVerifiedUser;
        updatePriorityScore();
    }

    private void updatePriorityScore() {
        int baseScore;
        if (writtenByVerifiedUser) {
            baseScore = VERIFIED_BASE_SCORE;
        } else {
            baseScore = REGULAR_BASE_SCORE;
        }

        long ageInMilliseconds = System.currentTimeMillis() - createdAt.getTime();
        if (ageInMilliseconds < 0) {
            ageInMilliseconds = 0;
        }

        long reviewAgeInDays = TimeUnit.MILLISECONDS.toDays(ageInMilliseconds);
        int recencyBonus = MAX_RECENCY_BONUS
                - (int) (reviewAgeInDays / RECENCY_INTERVAL_DAYS);

        if (recencyBonus < 0) {
            recencyBonus = 0;
        }

        priorityScore = baseScore + recencyBonus;
    }

    @Override
    public int compareTo(Review other) {
        if (other == null) {
            return -1;
        }

        int thisPriorityScore = getPriorityScore();
        int otherPriorityScore = other.getPriorityScore();

        if (thisPriorityScore > otherPriorityScore) {
            return -1;
        }

        if (thisPriorityScore < otherPriorityScore) {
            return 1;
        }

        if (createdAt.after(other.createdAt)) {
            return -1;
        }

        if (createdAt.before(other.createdAt)) {
            return 1;
        }

        return 0;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public int getReviewId() {
        return reviewId;
    }

    public int getStarRating() {
        return starRating;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return new Date(createdAt.getTime());
    }

    public int getPriorityScore() {
        updatePriorityScore();
        return priorityScore;
    }

    public boolean isWrittenByVerifiedUser() {
        return writtenByVerifiedUser;
    }
}
