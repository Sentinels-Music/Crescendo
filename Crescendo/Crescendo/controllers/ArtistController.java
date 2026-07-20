package Crescendo.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Crescendo.model.Album;
import Crescendo.model.Artist;
import Crescendo.model.Song;
import Crescendo.model.StreamingLink;
import Crescendo.model.Tag;

//database access for Artist, backed by the Artists table (plus MusicItems, Albums, Songs for the discography, and Tags through TagController)
public class ArtistController {

    private final TagController tagController = new TagController();
    private final StreamingLinkController streamingLinkController = new StreamingLinkController();

    // Returns every artist in the catalog, with their tags loaded 
    public List<Artist> getAllArtists() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT artistId, name, description, followerCount, averageRating FROM Artists";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                artists.add(readArtistRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Artist artist : artists) {
            for (Tag tag : tagController.getTagsForArtist(artist.getArtistId())) {
                artist.addTag(tag);
            }
        }
        return artists;
    }

    // Returns one artist fully loaded: tags, albums and popular songs with streaming links 
    public Artist getArtistById(int artistId) {
        Artist artist = null;
        String sql = "SELECT artistId, name, description, followerCount, averageRating "
                + "FROM Artists WHERE artistId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    artist = readArtistRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (artist == null) {
            return null;
        }

        for (Tag tag : tagController.getTagsForArtist(artistId)) {
            artist.addTag(tag);
        }
        loadAlbums(artist);
        loadSongs(artist);
        return artist;
    }

    //Inserts a new artist into the database and returns it with its generated artistId set. Only a VerifiedUser is meant to call this 
    public Artist addNewArtist(String name, String description) {
        String sql = "INSERT INTO Artists (name, description, followerCount, averageRating) "
                + "VALUES (?, ?, 0, 0.00)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                int newId = keys.next() ? keys.getInt(1) : 0;
                return new Artist(newId, name, description);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Increments the artist's stored follower count. 
    public boolean followArtist(int artistId) {
        return updateFollowerCount(artistId, "followerCount + 1");
    }

    // Decrements the artist's stored follower count --> never below 0
    public boolean unfollowArtist(int artistId) {
        return updateFollowerCount(artistId, "GREATEST(followerCount - 1, 0)");
    }

    private boolean updateFollowerCount(int artistId, String expression) {
        String sql = "UPDATE Artists SET followerCount = " + expression + " WHERE artistId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artistId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    //  helpers
    private Artist readArtistRow(ResultSet rs) throws Exception {
        Artist artist = new Artist(rs.getInt("artistId"), rs.getString("name"), rs.getString("description"));
        artist.setFollowerCount(rs.getInt("followerCount"));
        artist.setAverageRating(rs.getDouble("averageRating"));
        return artist;
    }

    private void loadAlbums(Artist artist) {
        String sql = "SELECT m.itemId, m.title, m.averageRating, a.releaseYear "
                + "FROM Albums a JOIN MusicItems m ON a.itemId = m.itemId "
                + "WHERE m.artistId = ? ORDER BY a.releaseYear DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artist.getArtistId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    artist.addAlbum(new Album(
                            rs.getInt("itemId"),
                            rs.getString("title"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSongs(Artist artist) {
        String sql = "SELECT m.itemId, m.title, m.averageRating, s.durationInSeconds "
                + "FROM Songs s JOIN MusicItems m ON s.itemId = m.itemId "
                + "WHERE m.artistId = ? ORDER BY m.averageRating DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artist.getArtistId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Song song = new Song(
                            rs.getInt("itemId"),
                            rs.getString("title"),
                            rs.getInt("durationInSeconds"),
                            rs.getDouble("averageRating"));
                    for (StreamingLink link : streamingLinkController.getLinksForSong(song.getItemId())) {
                        song.addStreamingLink(link);
                    }
                    artist.addSong(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
