package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.StreamingLink;


public class StreamingLinkController {

    /** Returns every streaming link stored for the given song (itemId). */
    public List<StreamingLink> getLinksForSong(int itemId) {
        List<StreamingLink> links = new ArrayList<>();
        String sql = "SELECT linkId, itemId, platform, url FROM StreamingLinks WHERE itemId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    links.add(new StreamingLink(
                            rs.getInt("linkId"),
                            rs.getInt("itemId"),
                            rs.getString("platform"),
                            rs.getString("url")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return links;
    }

    // Adds a new streaming link for a song and returns it (with its linkId set). 
    public StreamingLink addLink(int itemId, String platform, String url) {
        String sql = "INSERT INTO StreamingLinks (itemId, platform, url) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, itemId);
            pstmt.setString(2, platform);
            pstmt.setString(3, url);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                int linkId = keys.next() ? keys.getInt(1) : 0;
                return new StreamingLink(linkId, itemId, platform, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
