package model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a review written by a user for a music item.
 * A review contains a star rating, an optional comment,
 * a creation date, and a priority score.
 *
 * @author Efe Anık
 */

public class Review implements Comparable<Review> {

    private int reviewId;
    private User author;
    private MusicItem targetItem;
    private int starRating;
    private String comment;
    private Date createdAt;
    private int priorityScore;

    // Creates a new review.
    public Review(User author, MusicItem targetItem, int starRating, String comment) {
        this(author, targetItem, starRating, comment, new Date());
    }

    private Review(User author, MusicItem targetItem, int starRating, String comment, Date createdAt) {
        this(0, author, targetItem, starRating, comment, createdAt, calculatePriorityScore(author, createdAt));
    }

    // Creates a review loaded from the database.
    public Review(int reviewId, User author, MusicItem targetItem, int starRating, String comment, Date createdAt, int priorityScore) {
        if(author == null) {
            throw new IllegalArgumentException("Review author cannot be null.");
        }

        if(targetItem == null) {
            throw new IllegalArgumentException("Review target cannot be null.");
        }

        validateStarRating(starRating);
        validatePriorityScore(priorityScore);

        this.reviewId = reviewId;
        this.author = author;
        this.targetItem = targetItem;
        this.starRating = starRating;
        if(comment == null) {
            this.comment = "";
        }
        else {
            this.comment = comment;
        }

        if(createdAt == null) {
            this.createdAt = new Date();
        }
        else {
            this.createdAt = createdAt;
        }
        this.priorityScore = priorityScore;
    }

    // Calculates priority using verification and recency.
    private static int calculatePriorityScore(User author, Date createdAt) {
        int baseScore;

        if(author != null && author.isVerified()) {
            baseScore = 55;
        }
        else {
            baseScore = 5;
        }

        if(createdAt == null) {
            createdAt = new Date();
        }

        // Calculates how old the review is.
        long currentTime = System.currentTimeMillis();
        long createdTime = createdAt.getTime();
        long ageInMilliseconds = currentTime - createdTime;

        if(ageInMilliseconds < 0) {
            ageInMilliseconds = 0;
        }

        // Converts the review age to days.
        long ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMilliseconds);

        // Decreases by 1 point every 2 days.
        int recencyBonus = 45 - (int) (ageInDays / 2);

        if(recencyBonus < 0) {
            recencyBonus = 0;
        }

        return baseScore + recencyBonus;
    }

    // Checks that the rating is between 1 and 5.
    private static void validateStarRating(int starRating) {
        if(starRating < 1 || starRating > 5) {
            throw new IllegalArgumentException("Star rating must be between 1 and 5. Received: " + starRating);
        }
    }

    // Checks that the priority score is between 1 and 100.
    private static void validatePriorityScore(int priorityScore) {
        if(priorityScore < 1 || priorityScore > 100) {
            throw new IllegalArgumentException("Priority score must be between 1 and 100. Received: " + priorityScore);
        }
    }

    // Removes the review from its music item.
    public void deleteReview() {
        targetItem.removeReview(this);
    }

    // Checks whether the author is verified.
    public boolean isWrittenByVerifiedUser() {
        return author.isVerified();
    }

    public int getReviewId() {
        return reviewId;
    }

    public User getAuthor() {
        return author;
    }

    public MusicItem getTargetItem() {
        return targetItem;
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

    // Returns the current priority score.
    public int getPriorityScore() {
        priorityScore = calculatePriorityScore(author, createdAt);
        return priorityScore;
    }

    // Compares reviews by priority score and creation date in descending order.
    @Override
    public int compareTo(Review other) {
        int result = other.getPriorityScore() - getPriorityScore();

        if(result != 0) {
            return result;
        }

        return other.getCreatedAt().compareTo(getCreatedAt());
    }

    @Override
    public String toString() {
        String str = author.getUsername() + " rated \"" + targetItem.getTitle() + "\" " + starRating + "/5";

        if(!comment.trim().isEmpty()) {
            str += " - " + comment.trim();
        }

        return str;
    }
}
