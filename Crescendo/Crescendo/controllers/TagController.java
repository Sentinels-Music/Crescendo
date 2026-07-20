package Crescendo.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import Crescendo.model.Tag;

public class TagController {

    // Returns every tag currently attached to the given artist. 
    public List<Tag> getTagsForArtist(int artistId) {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT t.tagId, t.name FROM Tags t "
                + "JOIN ArtistTags at ON t.tagId = at.tagId "
                + "WHERE at.artistId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getInt("tagId"), rs.getString("name")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tags;
    }

    // Returns every distinct tag that is attached to at least one artist 
    public List<Tag> getAllTagsInUse() {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT DISTINCT t.tagId, t.name FROM Tags t "
                + "JOIN ArtistTags at ON t.tagId = at.tagId "
                + "ORDER BY t.name";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                tags.add(new Tag(rs.getInt("tagId"), rs.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tags;
    }

    // Attaches a tag (genre) to an artist. Creates the Tag row first if a tag with this name does not exist yet, then links it in ArtistTags.
    public Tag addTag(int artistId, String tagName) {
        try (Connection conn = DatabaseManager.getConnection()) {

            // 1) check the tag row exists
            try (PreparedStatement insertTag = conn.prepareStatement(
                    "INSERT IGNORE INTO Tags (name) VALUES (?)")) {
                insertTag.setString(1, tagName);
                insertTag.executeUpdate();
            }

            // 2) find its tagId
            int tagId = -1;
            try (PreparedStatement findTag = conn.prepareStatement(
                    "SELECT tagId FROM Tags WHERE name = ?")) {
                findTag.setString(1, tagName);
                try (ResultSet rs = findTag.executeQuery()) {
                    if (rs.next()) {
                        tagId = rs.getInt("tagId");
                    }
                }
            }
            if (tagId == -1) {
                return null;
            }

            // 3) link the artist to the tag
            try (PreparedStatement link = conn.prepareStatement(
                    "INSERT IGNORE INTO ArtistTags (artistId, tagId) VALUES (?, ?)")) {
                link.setInt(1, artistId);
                link.setInt(2, tagId);
                link.executeUpdate();
            }

            return new Tag(tagId, tagName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Removes a tag from an artist (does not delete the Tag row itself). 
    public boolean removeTag(int artistId, int tagId) {
        String sql = "DELETE FROM ArtistTags WHERE artistId = ? AND tagId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artistId);
            pstmt.setInt(2, tagId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
