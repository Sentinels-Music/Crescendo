package Crescendo.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SearchController {

    public List<SearchResult> searchMusicItem(String query, String filterType) {
        List<SearchResult> results = new ArrayList<>();
        String safeQuery = "%" + query.trim() + "%";
        
        // Use the centralized DatabaseManager for the connection
        try (Connection conn = DatabaseManager.getConnection()) {
            
            String sql = "";
            PreparedStatement pstmt = null;
            
            switch (filterType.toLowerCase()) {
                case "artists":
                    sql = "SELECT name AS title, 'Artist' AS type, '' AS additionalInfo, averageRating FROM Artists WHERE name LIKE ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, safeQuery);
                    break;
                    
                case "albums":
                    sql = "SELECT m.title AS title, 'Album' AS type, CONCAT('Released: ', a.releaseYear) AS additionalInfo, m.averageRating AS averageRating FROM Albums a JOIN MusicItems m ON a.itemId = m.itemId WHERE m.title LIKE ? AND m.itemType = 'ALBUM'";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, safeQuery);
                    break;
                    
                case "songs":
                    sql = "SELECT m.title AS title, 'Song' AS type, CONCAT(s.durationInSeconds, ' seconds') AS additionalInfo, m.averageRating AS averageRating FROM Songs s JOIN MusicItems m ON s.itemId = m.itemId WHERE m.title LIKE ? AND m.itemType = 'SONG'";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, safeQuery);
                    break;
                    
                case "people":
                    sql = "SELECT username AS title, 'User' AS type, '' AS additionalInfo, 0.0 AS averageRating FROM Users WHERE username LIKE ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, safeQuery);
                    break;

                case "all":
                default:
                    sql = "SELECT name AS title, 'Artist' AS type, '' AS additionalInfo, averageRating FROM Artists WHERE name LIKE ? " +
                          "UNION ALL " +
                          "SELECT m.title AS title, 'Album' AS type, CONCAT('Released: ', a.releaseYear) AS additionalInfo, m.averageRating AS averageRating FROM Albums a JOIN MusicItems m ON a.itemId = m.itemId WHERE m.title LIKE ? AND m.itemType = 'ALBUM' " +
                          "UNION ALL " +
                          "SELECT m.title AS title, 'Song' AS type, CONCAT(s.durationInSeconds, ' seconds') AS additionalInfo, m.averageRating AS averageRating FROM Songs s JOIN MusicItems m ON s.itemId = m.itemId WHERE m.title LIKE ? AND m.itemType = 'SONG' " +
                          "UNION ALL " +
                          "SELECT username AS title, 'User' AS type, '' AS additionalInfo, 0.0 AS averageRating FROM Users WHERE username LIKE ?";
                    
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, safeQuery);
                    pstmt.setString(2, safeQuery);
                    pstmt.setString(3, safeQuery);
                    pstmt.setString(4, safeQuery);
                    break;
            }

            if (pstmt != null) {
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    results.add(new SearchResult(
                        rs.getString("title"), 
                        rs.getString("type"), 
                        rs.getString("additionalInfo"),
                        rs.getDouble("averageRating")
                    ));
                }
                rs.close(); 
                pstmt.close(); 
            }
            
        } catch (Exception e) { 
            e.printStackTrace();
        }
        
        return results;
    }
}