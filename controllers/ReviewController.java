package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import model.Album;
import model.MusicItem;
import model.Review;
import model.Song;
import model.User;
import model.VerifiedUser;

public class ReviewController {

    private static final int MIN_STAR_RATING = 1;
    private static final int MAX_STAR_RATING = 5;
    private static final int VERIFIED_REVIEW_THRESHOLD = 10;

    public boolean rateItem(int userId, int itemId, int stars, String text) {
        if(userId <= 0 || itemId <= 0 || !isValidStarRating(stars)) {
            return false;
        }

        String sql = "INSERT INTO Reviews (userId, itemId, starRating, comment, createdAt, priorityScore) VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                User author = loadUser(conn, userId);
                MusicItem targetItem = loadMusicItem(conn, itemId);

                if(author == null || targetItem == null) {
                    conn.rollback();
                    return false;
                }

                Review review = new Review(author, targetItem, stars, text);

                try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, itemId);
                    pstmt.setInt(3, stars);

                    if(text == null || text.trim().isEmpty()) {
                        pstmt.setNull(4, Types.LONGVARCHAR);
                    }
                    else {
                        pstmt.setString(4, text.trim());
                    }

                    pstmt.setTimestamp(5, new Timestamp(review.getCreatedAt().getTime()));
                    pstmt.setInt(6, review.getPriorityScore());

                    if(pstmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                updateAverageRating(conn, itemId);
                conn.commit();
                return true;
            }
            catch(Exception e) {
                rollbackQuietly(conn);
                e.printStackTrace();
                return false;
            }
            finally {
                restoreAutoCommit(conn);
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertReview(int userId, int itemId, int stars, String comment) {
        return rateItem(userId, itemId, stars, comment);
    }

    public boolean deleteReview(int reviewId, int userId) {
        if(reviewId <= 0 || userId <= 0) {
            return false;
        }

        String findSql = "SELECT itemId FROM Reviews WHERE reviewId = ? AND userId = ?";
        String deleteSql = "DELETE FROM Reviews WHERE reviewId = ? AND userId = ?";

        try(Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int itemId;

                try(PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                    findStmt.setInt(1, reviewId);
                    findStmt.setInt(2, userId);

                    try(ResultSet rs = findStmt.executeQuery()) {
                        if(!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        itemId = rs.getInt("itemId");
                    }
                }

                try(PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, reviewId);
                    deleteStmt.setInt(2, userId);

                    if(deleteStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                updateAverageRating(conn, itemId);
                conn.commit();
                return true;
            }
            catch(Exception e) {
                rollbackQuietly(conn);
                e.printStackTrace();
                return false;
            }
            finally {
                restoreAutoCommit(conn);
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Review> getReviewsForItem(int itemId) {
        if(itemId <= 0) {
            return new ArrayList<>();
        }

        String sql = baseReviewSelect() + " WHERE r.itemId = ?";

        return executeReviewQuery(sql, itemId);
    }

    public List<Review> generateFeed(int currentUserId) {
        if(currentUserId <= 0) {
            return new ArrayList<>();
        }

        String sql = baseReviewSelect() + " JOIN Follows f ON f.followedId = r.userId WHERE f.followerId = ?";

        return executeReviewQuery(sql, currentUserId);
    }

    private List<Review> executeReviewQuery(String sql, int parameter) {
        List<Review> reviews = new ArrayList<>();

        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parameter);

            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    Review review = readReview(rs);

                    if(review != null) {
                        reviews.add(review);
                    }
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        Collections.sort(reviews);

        return reviews;
    }

    private String baseReviewSelect() {
        return "SELECT r.reviewId, r.userId, r.itemId, r.starRating, r.comment, r.createdAt, r.priorityScore, u.username, u.passwordHash, (SELECT COUNT(*) FROM Reviews rc WHERE rc.userId = r.userId) AS reviewCount, m.title, m.averageRating, m.itemType, a.releaseYear, s.durationInSeconds FROM Reviews r JOIN Users u ON u.userId = r.userId JOIN MusicItems m ON m.itemId = r.itemId LEFT JOIN Albums a ON a.itemId = m.itemId LEFT JOIN Songs s ON s.itemId = m.itemId";
    }

    private Review readReview(ResultSet rs) throws SQLException {
        int userId = rs.getInt("userId");
        int reviewCount = rs.getInt("reviewCount");

        User author = createUser(userId, rs.getString("username"), rs.getString("passwordHash"), reviewCount);

        MusicItem targetItem = createMusicItem(rs.getInt("itemId"), rs.getString("title"), rs.getDouble("averageRating"), rs.getString("itemType"), rs.getInt("releaseYear"), rs.getInt("durationInSeconds"));

        if(targetItem == null) {
            return null;
        }

        Timestamp timestamp = rs.getTimestamp("createdAt");
        Date createdAt;

        if(timestamp == null) {
            createdAt = new Date();
        }
        else {
            createdAt = new Date(timestamp.getTime());
        }

        int storedPriorityScore = rs.getInt("priorityScore");

        if(rs.wasNull() || storedPriorityScore < 1 || storedPriorityScore > 100) {
            storedPriorityScore = 1;
        }

        return new Review(rs.getInt("reviewId"), author, targetItem, rs.getInt("starRating"), rs.getString("comment"), createdAt, storedPriorityScore);
    }

    private User loadUser(Connection conn, int userId) throws SQLException {
        String sql = "SELECT u.username, u.passwordHash, (SELECT COUNT(*) FROM Reviews r WHERE r.userId = u.userId) AS reviewCount FROM Users u WHERE u.userId = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try(ResultSet rs = pstmt.executeQuery()) {
                if(!rs.next()) {
                    return null;
                }

                int reviewCount = rs.getInt("reviewCount");

                return createUser(userId, rs.getString("username"), rs.getString("passwordHash"), reviewCount);
            }
        }
    }

    private User createUser(int userId, String username, String passwordHash, int reviewCount) {
        if(reviewCount >= VERIFIED_REVIEW_THRESHOLD) {
            return new VerifiedUser(userId, username, passwordHash);
        }
        else {
            return new User(userId, username, passwordHash);
        }
    }

    private MusicItem loadMusicItem(Connection conn, int itemId) throws SQLException {
        String sql = "SELECT m.itemId, m.title, m.averageRating, m.itemType, a.releaseYear, s.durationInSeconds FROM MusicItems m LEFT JOIN Albums a ON a.itemId = m.itemId LEFT JOIN Songs s ON s.itemId = m.itemId WHERE m.itemId = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);

            try(ResultSet rs = pstmt.executeQuery()) {
                if(!rs.next()) {
                    return null;
                }

                return createMusicItem(rs.getInt("itemId"), rs.getString("title"), rs.getDouble("averageRating"), rs.getString("itemType"), rs.getInt("releaseYear"), rs.getInt("durationInSeconds"));
            }
        }
    }

    private MusicItem createMusicItem(int itemId, String title, double averageRating, String itemType, int releaseYear, int durationInSeconds) {
        if("ALBUM".equalsIgnoreCase(itemType)) {
            return new Album(itemId, title, releaseYear, averageRating);
        }
        else if("SONG".equalsIgnoreCase(itemType)) {
            return new Song(itemId, title, durationInSeconds, averageRating);
        }
        else {
            return null;
        }
    }

    private void updateAverageRating(Connection conn, int itemId) throws SQLException {
        String sql = "UPDATE MusicItems SET averageRating = COALESCE((SELECT AVG(starRating) FROM Reviews WHERE itemId = ?), 0.00) WHERE itemId = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        }
    }

    private boolean isValidStarRating(int stars) {
        return stars >= MIN_STAR_RATING && stars <= MAX_STAR_RATING;
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        }
        catch(SQLException rollbackError) {
            rollbackError.printStackTrace();
        }
    }

    private void restoreAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        }
        catch(SQLException autoCommitError) {
            autoCommitError.printStackTrace();
        }
    }
}
