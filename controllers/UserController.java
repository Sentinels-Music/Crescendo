package controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Profile;
import model.User;
import model.VerifiedUser;

/**
 * Controller for User & Authentication (Person 1).
 *
 * Handles registration, login, the follow / unfollow relationship, the
 * profile data shown on the Profile page, and taste similarity. Uses the
 * shared {@link DatabaseManager} connection and prepared statements, the
 * same way the other controllers on the team do.
 *
 * Verified status is derived, not stored: a user with 10+ reviews is
 * loaded as a {@link VerifiedUser} (Requirements Report, "Verified Users").
 */
public class UserController {

    private static final int VERIFIED_REVIEW_THRESHOLD = 10;

    // =====================================================================
    //  Authentication
    // =====================================================================

    /**
     * Registers a new account. Creates the Users row and an empty Profiles
     * row. Fails (returns false) if the username is already taken.
     *
     * @return true on success, false on failure / duplicate username
     */
    public boolean register(String username, String nickname, String password) {
        String insertUser = "INSERT INTO Users (username, nickname, passwordHash) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt =
                     conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, nickname);
            pstmt.setString(3, hashPassword(password));

            if (pstmt.executeUpdate() == 0) {
                return false;
            }

            int newUserId = -1;
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    newUserId = keys.getInt(1);
                }
            }

            if (newUserId > 0) {
                try (PreparedStatement profileStmt = conn.prepareStatement(
                        "INSERT INTO Profiles (userId, bio) VALUES (?, '')")) {
                    profileStmt.setInt(1, newUserId);
                    profileStmt.executeUpdate();
                }
            }
            return true;

        } catch (SQLException e) {
            // Duplicate username throws an integrity-constraint violation here.
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies credentials and, on success, returns the fully loaded user
     * ({@link User} or {@link VerifiedUser}).
     *
     * @return the user, or null if the username/password do not match
     */
    public User login(String username, String password) {
        String sql = "SELECT userId, passwordHash FROM Users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("passwordHash");
                    if (storedHash != null && storedHash.equals(hashPassword(password))) {
                        return loadUser(rs.getInt("userId"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================================
    //  Loading a user + profile
    // =====================================================================

    /**
     * Loads a user by id together with a fully populated {@link Profile}
     * (bio + live follower / following / review counts). Returns a
     * {@link VerifiedUser} when the review count reaches the threshold.
     */
    public User loadUser(int userId) {
        String sql = "SELECT username, passwordHash FROM Users WHERE userId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String username = rs.getString("username");
                String hash     = rs.getString("passwordHash");

                int reviewCount    = countReviews(conn, userId);
                int followerCount  = countFollowers(conn, userId);
                int followingCount = countFollowing(conn, userId);
                String bio         = loadBio(conn, userId);

                boolean verified = reviewCount >= VERIFIED_REVIEW_THRESHOLD;
                User user = verified
                        ? new VerifiedUser(userId, username, hash)
                        : new User(userId, username, hash);

                Profile profile = new Profile(bio, followerCount, followingCount, reviewCount);
                user.setProfile(profile);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================================
    //  Follow / unfollow
    // =====================================================================

    /** Makes {@code followerId} follow {@code followedId}. Idempotent. */
    public boolean followUser(int followerId, int followedId) {
        if (followerId == followedId) {
            return false;
        }
        String sql = "INSERT IGNORE INTO Follows (followerId, followedId) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, followerId);
            pstmt.setInt(2, followedId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Makes {@code followerId} stop following {@code followedId}. */
    public boolean unfollowUser(int followerId, int followedId) {
        String sql = "DELETE FROM Follows WHERE followerId = ? AND followedId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, followerId);
            pstmt.setInt(2, followedId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** @return true if {@code followerId} already follows {@code followedId}. */
    public boolean isFollowing(int followerId, int followedId) {
        String sql = "SELECT 1 FROM Follows WHERE followerId = ? AND followedId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, followerId);
            pstmt.setInt(2, followedId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return the list of users that {@code userId} follows (id + username
     *         loaded; enough for the Following list on the Profile page).
     */
    public List<User> getFollowedUsers(int userId) {
        List<User> result = new ArrayList<>();
        String sql = "SELECT u.userId, u.username " +
                     "FROM Follows f JOIN Users u ON f.followedId = u.userId " +
                     "WHERE f.followerId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new User(rs.getInt("userId"), rs.getString("username"), null));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // =====================================================================
    //  Taste similarity
    // =====================================================================

    /**
     * Database-backed taste similarity between two users, as a percentage.
     * Reuses {@link TasteMatchController} (Ege's implementation) so the app
     * has one single Taste Match formula.
     */
    public double calculateTasteSimilarity(int currentUserId, int targetUserId) {
        return new TasteMatchController()
                .getTasteMatchPercentage(currentUserId, targetUserId);
    }

    // =====================================================================
    //  Profile page data
    // =====================================================================

    /** Updates the bio in the Profiles table. */
    public boolean updateBio(int userId, String bio) {
        String sql = "UPDATE Profiles SET bio = ? WHERE userId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bio);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return the reviews written by this user, for the "My Reviews"
     *         section of the Profile page.
     */
    public List<ReviewSummary> getReviewsByUser(int userId) {
        List<ReviewSummary> reviews = new ArrayList<>();
        // Add "ORDER BY r.createdAt DESC" if that column exists in Reviews.
        String sql = "SELECT m.title AS title, r.starRating AS stars, r.comment AS comment " +
                     "FROM Reviews r JOIN MusicItems m ON r.itemId = m.itemId " +
                     "WHERE r.userId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new ReviewSummary(
                            rs.getString("title"),
                            rs.getInt("stars"),
                            rs.getString("comment")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // =====================================================================
    //  Private helpers (share the caller's Connection)
    // =====================================================================

    private int countReviews(Connection conn, int userId) throws SQLException {
        return countWhere(conn, "SELECT COUNT(*) FROM Reviews WHERE userId = ?", userId);
    }

    private int countFollowers(Connection conn, int userId) throws SQLException {
        return countWhere(conn, "SELECT COUNT(*) FROM Follows WHERE followedId = ?", userId);
    }

    private int countFollowing(Connection conn, int userId) throws SQLException {
        return countWhere(conn, "SELECT COUNT(*) FROM Follows WHERE followerId = ?", userId);
    }

    private int countWhere(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String loadBio(Connection conn, int userId) throws SQLException {
        try (PreparedStatement pstmt =
                     conn.prepareStatement("SELECT bio FROM Profiles WHERE userId = ?")) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String bio = rs.getString("bio");
                    return bio != null ? bio : "";
                }
            }
        }
        return "";
    }

    /**
     * Hashes a password with SHA-256 and returns it as hex.
     * (For a real product you would also add a per-user salt / bcrypt;
     * SHA-256 keeps plaintext out of the DB and matches the passwordHash
     * field in the UML.)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
