package Crescendo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the display information for a user's profile: biography,
 * follower / following / review counts, and taste tags.
 *
 * The counts are filled in by {@code controllers.UserController} from
 * live COUNT queries (they are not stored columns, so they never go
 * stale). Taste tags are used by {@link User#calculateTasteSimilarity}.
 */
public class Profile {

    private String bio;
    private int followerCount;
    private int followingCount;
    private int reviewCount;
    private List<Tag> tasteTags = new ArrayList<>();

    public Profile() {
        this.bio = "";
    }

    public Profile(String bio, int followerCount, int followingCount, int reviewCount) {
        this.bio = (bio != null) ? bio : "";
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.reviewCount = reviewCount;
    }

    // ---- bio ----
    public String getBio() { return bio; }

    /** Updates the biography shown on the profile page. */
    public void updateBio(String bio) {
        this.bio = (bio != null) ? bio : "";
    }

    // ---- counts ----
    public int getFollowerCount()  { return followerCount; }
    public int getFollowingCount() { return followingCount; }
    public int getReviewCount()    { return reviewCount; }

    public void setFollowerCount(int followerCount)   { this.followerCount = followerCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
    public void setReviewCount(int reviewCount)       { this.reviewCount = reviewCount; }

    // ---- taste tags ----
    public List<Tag> getTasteTags() {
        return tasteTags;
    }

    public void setTasteTags(List<Tag> tasteTags) {
        this.tasteTags = (tasteTags != null) ? tasteTags : new ArrayList<>();
    }

    public void addTasteTag(Tag tag) {
        if (tag != null && !tasteTags.contains(tag)) {
            tasteTags.add(tag);
        }
    }

    /**
     * Rebuilds the taste-tag list from the genres of the artists this user
     * follows.
     *
     * NOTE: this needs the "followed artists" source, which lives in a
     * teammate's schema (Emir / Ege). Until that table is wired in, call
     * {@link #setTasteTags(List)} with the tags the controller has loaded
     * from ProfileTags. This method is the agreed extension point for that
     * feature.
     *
     * @param tagsFromFollowedArtists tags collected from followed artists
     */
    public void refreshTasteTagsFromFollowedArtist(List<Tag> tagsFromFollowedArtists) {
        this.tasteTags = new ArrayList<>();
        if (tagsFromFollowedArtists != null) {
            for (Tag tag : tagsFromFollowedArtists) {
                addTasteTag(tag);
            }
        }
    }
}
