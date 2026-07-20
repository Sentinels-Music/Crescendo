package Crescendo.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TasteMatchController {

    public int getTasteMatchPercentage(int currentUserId, int targetUserId) {
        int matchPercentage = 0;
        
        String sql = "SELECT ROUND((COUNT(Me.tagId) * 100.0 / (SELECT COUNT(tagId) FROM ProfileTags WHERE userId = ?)), 0) AS tasteMatchPercentage " +
                     "FROM ProfileTags AS Me " +
                     "JOIN ProfileTags AS Them ON Me.tagId = Them.tagId " +
                     "WHERE Me.userId = ? AND Them.userId = ? " +
                     "GROUP BY Them.userId";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, currentUserId);
            pstmt.setInt(3, targetUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    matchPercentage = rs.getInt("tasteMatchPercentage");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return matchPercentage;
    }
}