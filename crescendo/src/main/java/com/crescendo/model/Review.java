package com.crescendo.model;

import java.util.Date;

/**
 * Stub owned by Abdullah Efe Anık (Review & Social Feed).
 */
public class Review {

    private final int reviewId;
    private final String reviewerUsername;
    private final int starRating;
    private final String comment;
    private final Date createdAt;
    private final int priorityScore;
    private final boolean writtenByVerifiedUser;

    public Review(int reviewId, String reviewerUsername, int starRating, String comment,
                   boolean writtenByVerifiedUser) {
        this.reviewId = reviewId;
        this.reviewerUsername = reviewerUsername;
        this.starRating = starRating;
        this.comment = comment;
        this.createdAt = new Date();
        this.writtenByVerifiedUser = writtenByVerifiedUser;
        this.priorityScore = writtenByVerifiedUser ? 100 : 1;
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
        return createdAt;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public boolean isWrittenByVerifiedUser() {
        return writtenByVerifiedUser;
    }
}
