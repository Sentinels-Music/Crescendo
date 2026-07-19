package model;

import java.util.ArrayList;
import java.util.List;

import controllers.ArtistController;

/**
 * A user who has published at least 10 reviews and has automatically
 * earned Verified status (see Requirements Report, "Verified Users").
 *
 * Verified users keep every standard-user ability and gain the privilege
 * of adding new music to the catalogue. This subclass keeps those
 * privileges isolated from the base {@link User}, exactly as described in
 * the Detailed Design Report.
 */
public class VerifiedUser extends User {

    public VerifiedUser(int userId, String username, String passwordHash) {
        super(userId, username, passwordHash);
    }

    /** @return true — this account is verified */
    @Override
    public boolean isVerified() {
        return true;
    }

    /**
     * The two main perks unlocked by verification, plus the badge.
     * Kept as text so the UI can list them on the profile / settings.
     */
    public List<String> getVerifiedPerks() {
        List<String> perks = new ArrayList<>();
        perks.add("Add new artists, albums, and songs to the catalog");
        perks.add("Your reviews are prioritized at the top of comment sections");
        perks.add("A Verified badge is shown on your profile");
        return perks;
    }

    /**
     * Verified privilege: add a new artist to the catalogue.
     *
     * Delegates to {@link ArtistController} (Emir's area) so the actual
     * database insert stays in one place. addAlbum() / addSong() would
     * follow the same delegation pattern once the Music Items controller
     * (Mustafa's area) exposes its insert methods.
     *
     * @param name        artist name
     * @param description short description (may be empty)
     * @return the saved Artist, or null if the insert failed
     */
    public model.Artist addNewArtist(String name, String description) {
        return new ArtistController().addNewArtist(name, description);
    }
}
