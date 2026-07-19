package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Core User domain class (Model layer).
 *
 * Holds account data and in-memory social state (the users this account
 * follows) plus the user's Profile. Persistence is handled by
 * {@code controllers.UserController}; this class only keeps state and
 * simple object-oriented behaviour, matching the UML class diagram.
 *
 * A user with 10 or more reviews is represented at runtime by the
 * {@link VerifiedUser} subclass (see UserController.loadUser).
 */
public class User {

    protected int userId;
    protected String username;
    protected String passwordHash;

    /** Users this account follows (populated by the controller when needed). */
    protected List<User> followedUsers = new ArrayList<>();

    /** The profile attached to this account. */
    protected Profile profile;

    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // ---- getters ----
    public int getUserId()          { return userId; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Profile getProfile()     { return profile; }

    public void setProfile(Profile profile) { this.profile = profile; }

    /**
     * Base users are not verified. {@link VerifiedUser} overrides this.
     * @return false for a standard user
     */
    public boolean isVerified() {
        return false;
    }

    // ---- following behaviour (in-memory; DB is done by UserController) ----

    /** Adds a user to this account's followed list (no duplicates). */
    public void followUser(User user) {
        if (user != null && user.getUserId() != this.userId
                && !followedUsers.contains(user)) {
            followedUsers.add(user);
        }
    }

    /** Removes a user from this account's followed list. */
    public void unfollowUser(User user) {
        followedUsers.remove(user);
    }

    /** @return the users this account currently follows */
    public List<User> getFollowedUsers() {
        return followedUsers;
    }

    public void setFollowedUsers(List<User> followedUsers) {
        this.followedUsers = (followedUsers != null) ? followedUsers : new ArrayList<>();
    }

    /**
     * Pure object-oriented taste-similarity calculation, using the same
     * formula the app uses for Taste Match:
     *
     *   (shared taste tags) / (this user's taste tags) * 100, rounded.
     *
     * This works on the in-memory taste tags of both profiles. The
     * database-backed version lives in
     * {@code controllers.UserController.calculateTasteSimilarity(int,int)},
     * which delegates to {@code TasteMatchController}.
     *
     * @param other the user to compare against
     * @return match percentage 0..100 (0 if either profile has no tags)
     */
    public double calculateTasteSimilarity(User other) {
        if (this.profile == null || other == null || other.getProfile() == null) {
            return 0.0;
        }
        List<Tag> mine  = this.profile.getTasteTags();
        List<Tag> yours = other.getProfile().getTasteTags();
        if (mine == null || mine.isEmpty() || yours == null || yours.isEmpty()) {
            return 0.0;
        }
        int shared = 0;
        for (Tag tag : mine) {
            if (yours.contains(tag)) {
                shared++;
            }
        }
        return Math.round((shared * 100.0) / mine.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        return this.userId == ((User) obj).userId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }

    @Override
    public String toString() {
        return username + " (#" + userId + ")";
    }
}
