package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Album;
import model.MusicItem;
import model.Song;

public class ListenLaterController {

    public void addToListenLater(int userId, int itemId) {
        String sql = "INSERT IGNORE INTO ListenLater (userId, itemId) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFromListenLater(int userId, int itemId) {
        String sql = "DELETE FROM ListenLater WHERE userId = ? AND itemId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isInListenLater(int userId, int itemId) {
        String sql = "SELECT 1 FROM ListenLater WHERE userId = ? AND itemId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<MusicItem> getListenLater(int userId) {
        List<MusicItem> items = new ArrayList<>();
        String sql = "SELECT m.itemId, m.title, m.averageRating, m.itemType, "
                + "a.releaseYear, s.durationInSeconds "
                + "FROM ListenLater ll "
                + "JOIN MusicItems m ON ll.itemId = m.itemId "
                + "LEFT JOIN Albums a ON m.itemId = a.itemId "
                + "LEFT JOIN Songs s ON m.itemId = s.itemId "
                + "WHERE ll.userId = ? "
                + "ORDER BY ll.addedAt";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("itemId");
                    String title = rs.getString("title");
                    double averageRating = rs.getDouble("averageRating");
                    String itemType = rs.getString("itemType");
                    if ("ALBUM".equalsIgnoreCase(itemType)) {
                        items.add(new Album(itemId, title, rs.getInt("releaseYear"), averageRating));
                    } else {
                        items.add(new Song(itemId, title, rs.getInt("durationInSeconds"), averageRating));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public void addToListenLater(int itemId) {
        if (Session.isLoggedIn()) {
            addToListenLater(Session.getCurrentUser().getUserId(), itemId);
        }
    }

    public void removeFromListenLater(int itemId) {
        if (Session.isLoggedIn()) {
            removeFromListenLater(Session.getCurrentUser().getUserId(), itemId);
        }
    }

    public boolean isInListenLater(int itemId) {
        return Session.isLoggedIn()
                && isInListenLater(Session.getCurrentUser().getUserId(), itemId);
    }

    public List<MusicItem> getListenLater() {
        if (Session.isLoggedIn()) {
            return getListenLater(Session.getCurrentUser().getUserId());
        }
        return new ArrayList<>();
    }
}
