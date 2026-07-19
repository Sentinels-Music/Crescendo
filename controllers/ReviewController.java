package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReviewController {
    public boolean insertReview(int userId, int itemId, int stars, String comment) {
        String sql = "INSERT INTO Reviews (userId, itemId, starRating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, stars);
            pstmt.setString(4, comment);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}   
