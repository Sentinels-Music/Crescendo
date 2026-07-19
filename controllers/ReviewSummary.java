package controllers;

/**
 * Small read-only object used to show one of the user's reviews in the
 * "My Reviews" section of the Profile page. Mirrors the style of
 * {@link SearchResult}.
 */
public class ReviewSummary {

    private final String itemTitle;
    private final int stars;
    private final String comment;

    public ReviewSummary(String itemTitle, int stars, String comment) {
        this.itemTitle = itemTitle;
        this.stars = stars;
        this.comment = comment;
    }

    public String getItemTitle() { return itemTitle; }
    public int getStars()        { return stars; }
    public String getComment()   { return comment; }

    @Override
    public String toString() {
        return itemTitle + " — " + stars + "★";
    }
}
