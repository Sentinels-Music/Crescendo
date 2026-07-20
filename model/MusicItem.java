package model;

import java.util.List;
import java.util.ArrayList;

public abstract class MusicItem implements ListenLaterItem {
    protected int itemId;
    protected String title;
    protected double averageRating;
    protected List<Review> reviews;
    protected List<User> listenLaterUsers;

    public MusicItem(int itemId, String title) {
        this.itemId = itemId;
        this.title = title;
        this.averageRating = 0.0;
        this.reviews = new ArrayList<>();
        this.listenLaterUsers = new ArrayList<>();
    }

    public void addReview(Review review) {
        reviews.add(review);
        calculateAverageRating();
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

    public void addToListenLater(User user) {
        if (!listenLaterUsers.contains(user)) {
            listenLaterUsers.add(user);
        }
    }

    public void removeFromListenLater(User user) {
        listenLaterUsers.remove(user);
    }

    public boolean isInListenLater(User user) {
        return listenLaterUsers.contains(user);
    }

    protected abstract MusicItem getItemType();
}
