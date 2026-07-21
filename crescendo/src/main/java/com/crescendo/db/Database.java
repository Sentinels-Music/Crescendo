package com.crescendo.db;

import com.crescendo.model.Album;
import com.crescendo.model.Artist;
import com.crescendo.model.Review;
import com.crescendo.model.Song;
import com.crescendo.model.Tag;
import com.crescendo.model.User;
import com.crescendo.model.VerifiedUser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owned by Ege Yiğit Yıldırım (Search, Database & Integration), used by every
 * controller. Plain JDBC against a local MySQL instance: schema creation, demo seed
 * data (10 artists, 10 songs, 3 accounts), and all persistence queries the app needs.
 * Each method opens and closes its own Connection - simple and safe at this app's scale.
 */
public final class Database {

    private static final String SERVER_URL = "jdbc:mysql://127.0.0.1:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/crescendo?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Ege754";

    private Database() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // ---------------------------------------------------------------------
    // Schema + seed
    // ---------------------------------------------------------------------
    private static final String[] SCHEMA = {
            "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "username VARCHAR(50) UNIQUE NOT NULL,"
                    + "password_hash VARCHAR(128) NOT NULL,"
                    + "bio VARCHAR(500) DEFAULT '',"
                    + "verified BOOLEAN DEFAULT FALSE,"
                    + "review_count INT DEFAULT 0,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS user_follows ("
                    + "follower_id INT NOT NULL,"
                    + "followee_id INT NOT NULL,"
                    + "PRIMARY KEY (follower_id, followee_id),"
                    + "FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS artists ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(150) NOT NULL,"
                    + "description VARCHAR(1000) DEFAULT '',"
                    + "follower_count INT DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS artist_follows ("
                    + "user_id INT NOT NULL,"
                    + "artist_id INT NOT NULL,"
                    + "PRIMARY KEY (user_id, artist_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS tags ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(60) UNIQUE NOT NULL)",
            "CREATE TABLE IF NOT EXISTS artist_tags ("
                    + "artist_id INT NOT NULL,"
                    + "tag_id INT NOT NULL,"
                    + "PRIMARY KEY (artist_id, tag_id),"
                    + "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS albums ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "artist_id INT NOT NULL,"
                    + "title VARCHAR(200) NOT NULL,"
                    + "release_year INT,"
                    + "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS songs ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "artist_id INT NOT NULL,"
                    + "album_id INT NULL,"
                    + "title VARCHAR(200) NOT NULL,"
                    + "duration_seconds INT,"
                    + "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE SET NULL)",
            "CREATE TABLE IF NOT EXISTS streaming_links ("
                    + "song_id INT NOT NULL,"
                    + "platform VARCHAR(40) NOT NULL,"
                    + "url VARCHAR(500) NOT NULL,"
                    + "PRIMARY KEY (song_id, platform),"
                    + "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS reviews ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_id INT NOT NULL,"
                    + "target_type VARCHAR(10) NOT NULL,"
                    + "target_id INT NOT NULL,"
                    + "star_rating INT NOT NULL,"
                    + "comment VARCHAR(1000) DEFAULT '',"
                    + "priority_score INT DEFAULT 1,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS listen_later ("
                    + "user_id INT NOT NULL,"
                    + "item_type VARCHAR(10) NOT NULL,"
                    + "item_id INT NOT NULL,"
                    + "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (user_id, item_type, item_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)"
    };

    public static void initialize() {
        try (Connection serverConn = DriverManager.getConnection(SERVER_URL, USER, PASS);
             Statement stmt = serverConn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS crescendo;");
        } catch (SQLException ignored) {
        }

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0;");
            for (String ddl : SCHEMA) {
                statement.execute(ddl);
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize the Crescendo database. Is MySQL running on port 3306?", e);
        }
        seedIfEmpty();
    }

    private static void seedIfEmpty() {
        try (Connection connection = connect()) {
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM artists")) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return;
                }
            }
            seedAccounts(connection);
            seedCatalog(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Could not seed the Crescendo database.", e);
        }
    }

    private static void seedAccounts(Connection connection) throws SQLException {
        int mustafa = insertUserRaw(connection, "mustafa", "password123", true, 12,
                "Yacht-rock to post-punk. Always one track away from a new favorite.");
        int elifK = insertUserRaw(connection, "elif_k", "password123", true, 15, "92% taste match energy.");
        int canPolat = insertUserRaw(connection, "can.polat", "password123", false, 3, "Still building my taste profile.");

        followUser(mustafa, elifK);
        followUser(mustafa, canPolat);
        followUser(elifK, canPolat);
    }

    private static int insertUserRaw(Connection connection, String username, String password, boolean verified,
                                      int reviewCount, String bio) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, bio, verified, review_count) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            ps.setString(3, bio);
            ps.setBoolean(4, verified);
            ps.setInt(5, reviewCount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static void seedCatalog(Connection connection) throws SQLException {
        int tameImpala = insertArtistRaw(connection, "Tame Impala",
                "Australian psychedelic music project led by Kevin Parker.");
        int radiohead = insertArtistRaw(connection, "Radiohead", "English rock band formed in Abingdon in 1985.");
        int massiveAttack = insertArtistRaw(connection, "Massive Attack", "Pioneers of the trip-hop sound from Bristol.");
        int frankOcean = insertArtistRaw(connection, "Frank Ocean", "American singer-songwriter known for Channel Orange.");
        int sza = insertArtistRaw(connection, "SZA", "American R&B singer-songwriter.");
        int sufjanStevens = insertArtistRaw(connection, "Sufjan Stevens", "American singer-songwriter and multi-instrumentalist.");
        int bonIver = insertArtistRaw(connection, "Bon Iver", "Indie folk project led by Justin Vernon.");
        int tyler = insertArtistRaw(connection, "Tyler, The Creator", "American rapper and producer.");
        int khruangbin = insertArtistRaw(connection, "Khruangbin", "Texan trio blending psychedelic and funk influences.");
        int fleetFoxes = insertArtistRaw(connection, "Fleet Foxes", "American indie folk band from Seattle.");

        tagArtist(connection, tameImpala, "Psychedelic Rock");
        tagArtist(connection, radiohead, "Alternative Rock");
        tagArtist(connection, radiohead, "Art Rock");
        tagArtist(connection, massiveAttack, "Trip Hop");
        tagArtist(connection, frankOcean, "R&B");
        tagArtist(connection, sza, "R&B");
        tagArtist(connection, sufjanStevens, "Indie Folk");
        tagArtist(connection, bonIver, "Indie Folk");
        tagArtist(connection, tyler, "Hip Hop");
        tagArtist(connection, khruangbin, "Psychedelic Rock");
        tagArtist(connection, fleetFoxes, "Indie Folk");

        int currents = insertAlbumRaw(connection, tameImpala, "Currents", 2015);
        int inRainbows = insertAlbumRaw(connection, radiohead, "In Rainbows", 2007);
        int mezzanine = insertAlbumRaw(connection, massiveAttack, "Mezzanine", 1998);
        int channelOrange = insertAlbumRaw(connection, frankOcean, "Channel Orange", 2012);
        int callMeByYourName = insertAlbumRaw(connection, sufjanStevens, "Call Me by Your Name", 2017);
        int bonIverBonIver = insertAlbumRaw(connection, bonIver, "Bon Iver, Bon Iver", 2011);

        int letItHappen = insertSongRaw(connection, tameImpala, currents, "Let It Happen", 467);
        int lessIKnow = insertSongRaw(connection, tameImpala, currents, "The Less I Know The Better", 216);
        int weirdFishes = insertSongRaw(connection, radiohead, inRainbows, "Weird Fishes/Arpeggi", 318);
        int videotape = insertSongRaw(connection, radiohead, inRainbows, "Videotape", 279);
        int teardrop = insertSongRaw(connection, massiveAttack, mezzanine, "Teardrop", 330);
        int pyramids = insertSongRaw(connection, frankOcean, channelOrange, "Pyramids", 593);
        int thinkinBoutYou = insertSongRaw(connection, frankOcean, channelOrange, "Thinkin Bout You", 200);
        int goodDays = insertSongRaw(connection, sza, null, "Good Days", 278);
        int mysteryOfLove = insertSongRaw(connection, sufjanStevens, callMeByYourName, "Mystery of Love", 290);
        int holocene = insertSongRaw(connection, bonIver, bonIverBonIver, "Holocene", 336);

        addStreamingLinkRaw(connection, letItHappen, "Spotify", "https://open.spotify.com/track/letithappen");
        addStreamingLinkRaw(connection, letItHappen, "YouTube", "https://www.youtube.com/watch?v=letithappen");
        addStreamingLinkRaw(connection, lessIKnow, "Spotify", "https://open.spotify.com/track/lessiknow");
        addStreamingLinkRaw(connection, weirdFishes, "Spotify", "https://open.spotify.com/track/weirdfishes");
        addStreamingLinkRaw(connection, weirdFishes, "YouTube", "https://www.youtube.com/watch?v=weirdfishes");
        addStreamingLinkRaw(connection, videotape, "Spotify", "https://open.spotify.com/track/videotape");
        addStreamingLinkRaw(connection, teardrop, "Spotify", "https://open.spotify.com/track/teardrop");
        addStreamingLinkRaw(connection, pyramids, "Spotify", "https://open.spotify.com/track/pyramids");
        addStreamingLinkRaw(connection, thinkinBoutYou, "Spotify", "https://open.spotify.com/track/thinkinboutyou");
        addStreamingLinkRaw(connection, goodDays, "Spotify", "https://open.spotify.com/track/gooddays");
        addStreamingLinkRaw(connection, mysteryOfLove, "Spotify", "https://open.spotify.com/track/mysteryoflove");
        addStreamingLinkRaw(connection, holocene, "Spotify", "https://open.spotify.com/track/holocene");
        addStreamingLinkRaw(connection, holocene, "YouTube", "https://www.youtube.com/watch?v=holocene");

        int mustafaId = findUserIdRaw(connection, "mustafa");
        int elifId = findUserIdRaw(connection, "elif_k");
        int canId = findUserIdRaw(connection, "can.polat");

        insertReviewRaw(connection, elifId, true, "SONG", letItHappen, 5,
                "The drums alone are worth the 7 minutes. Best album opener of the decade.");
        insertReviewRaw(connection, canId, false, "SONG", letItHappen, 4,
                "Takes a couple listens to click, then it doesn't leave your head.");
        insertReviewRaw(connection, elifId, true, "ALBUM", currents, 4,
                "A hazy, synth-soaked headphone record.");
        insertReviewRaw(connection, canId, false, "SONG", teardrop, 5,
                "Trip-hop at its absolute peak. Timeless.");
        insertReviewRaw(connection, mustafaId, true, "ALBUM", inRainbows, 5,
                "Still flawless after all these years.");
        insertReviewRaw(connection, mustafaId, true, "SONG", pyramids, 5,
                "A late-night record. Endlessly replayable.");

        followArtistRaw(connection, mustafaId, tameImpala);
        followArtistRaw(connection, mustafaId, radiohead);
        followArtistRaw(connection, elifId, tameImpala);

        addListenLaterRaw(connection, mustafaId, "SONG", pyramids);
        addListenLaterRaw(connection, mustafaId, "ALBUM", bonIverBonIver);
        addListenLaterRaw(connection, elifId, "SONG", holocene);
    }

    private static int insertArtistRaw(Connection connection, String name, String description) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO artists (name, description) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static void tagArtist(Connection connection, int artistId, String tagName) throws SQLException {
        int tagId = findOrCreateTagRaw(connection, tagName);
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT IGNORE INTO artist_tags (artist_id, tag_id) VALUES (?,?)")) {
            ps.setInt(1, artistId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }

    private static int findOrCreateTagRaw(Connection connection, String name) throws SQLException {
        try (PreparedStatement find = connection.prepareStatement("SELECT id FROM tags WHERE name = ?")) {
            find.setString(1, name);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO tags (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, name);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static int insertAlbumRaw(Connection connection, int artistId, String title, int year) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO albums (artist_id, title, release_year) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, artistId);
            ps.setString(2, title);
            ps.setInt(3, year);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static int insertSongRaw(Connection connection, int artistId, Integer albumId, String title, int durationSeconds)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO songs (artist_id, album_id, title, duration_seconds) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, artistId);
            if (albumId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, albumId);
            }
            ps.setString(3, title);
            ps.setInt(4, durationSeconds);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static void addStreamingLinkRaw(Connection connection, int songId, String platform, String url) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO streaming_links (song_id, platform, url) VALUES (?,?,?)")) {
            ps.setInt(1, songId);
            ps.setString(2, platform);
            ps.setString(3, url);
            ps.executeUpdate();
        }
    }

    private static int findUserIdRaw(Connection connection, String username) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void insertReviewRaw(Connection connection, int userId, boolean verified, String targetType,
                                         int targetId, int stars, String comment) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO reviews (user_id, target_type, target_id, star_rating, comment, priority_score) "
                        + "VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, userId);
            ps.setString(2, targetType);
            ps.setInt(3, targetId);
            ps.setInt(4, stars);
            ps.setString(5, comment);
            ps.setInt(6, verified ? 100 : 1);
            ps.executeUpdate();
        }
    }

    private static void followArtistRaw(Connection connection, int userId, int artistId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT IGNORE INTO artist_follows (user_id, artist_id) VALUES (?,?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, artistId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE artists SET follower_count = follower_count + 1 WHERE id = ?")) {
            ps.setInt(1, artistId);
            ps.executeUpdate();
        }
    }

    private static void addListenLaterRaw(Connection connection, int userId, String itemType, int itemId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT IGNORE INTO listen_later (user_id, item_type, item_id) VALUES (?,?,?)")) {
            ps.setInt(1, userId);
            ps.setString(2, itemType);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        }
    }

    private static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------------
    // Auth - owned by Metehan Karadeniz (User & Authentication)
    // ---------------------------------------------------------------------
    public static User register(String username, String password) throws SQLException {
        try (Connection connection = connect()) {
            int id = insertUserRaw(connection, username, password, false, 0, "");
            return new User(id, username, hash(password));
        }
    }

    public static User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password_hash, bio, verified, review_count FROM users WHERE username = ?";
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!rs.getString("password_hash").equals(hash(password))) {
                    return null;
                }
                return userFromRow(rs);
            }
        }
    }

    /** Reloads a user's current state - call after any action that might change verified status. */
    public static User reloadUser(int userId) throws SQLException {
        String sql = "SELECT id, username, password_hash, bio, verified, review_count FROM users WHERE id = ?";
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return userFromRow(rs);
            }
        }
    }

    private static User userFromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        User user = rs.getBoolean("verified")
                ? new VerifiedUser(id, username, passwordHash)
                : new User(id, username, passwordHash);
        user.setBio(rs.getString("bio"));
        user.setReviewCount(rs.getInt("review_count"));
        return user;
    }

    public static void updateBio(int userId, String bio) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement("UPDATE users SET bio = ? WHERE id = ?")) {
            ps.setString(1, bio);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Increments the reviewer's review count and auto-upgrades to Verified at 10 reviews. */
    private static void incrementReviewCountAndMaybeVerify(Connection connection, int userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET review_count = review_count + 1 WHERE id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET verified = TRUE WHERE id = ? AND verified = FALSE AND review_count >= 10")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public static void followUser(int followerId, int followeeId) {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT IGNORE INTO user_follows (follower_id, followee_id) VALUES (?,?)")) {
            ps.setInt(1, followerId);
            ps.setInt(2, followeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unfollowUser(int followerId, int followeeId) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM user_follows WHERE follower_id = ? AND followee_id = ?")) {
            ps.setInt(1, followerId);
            ps.setInt(2, followeeId);
            ps.executeUpdate();
        }
    }

    public static List<User> getFollowedUsers(int userId) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password_hash, u.bio, u.verified, u.review_count FROM users u "
                + "JOIN user_follows f ON f.followee_id = u.id WHERE f.follower_id = ?";
        List<User> result = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(userFromRow(rs));
                }
            }
        }
        return result;
    }

    /** Users who follow this account (the reverse of getFollowedUsers). */
    public static List<User> getFollowers(int userId) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password_hash, u.bio, u.verified, u.review_count FROM users u "
                + "JOIN user_follows f ON f.follower_id = u.id WHERE f.followee_id = ?";
        List<User> result = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(userFromRow(rs));
                }
            }
        }
        return result;
    }

    public static boolean isFollowingUser(int followerId, int followeeId) throws SQLException {
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM user_follows WHERE follower_id = ? AND followee_id = ?")) {
            ps.setInt(1, followerId);
            ps.setInt(2, followeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int getFollowerCountForUser(int userId) throws SQLException {
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM user_follows WHERE followee_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Simple taste-match heuristic: percentage overlap between the two users' followed
     * artists (shared followed artists / the larger of the two follow-lists). A stand-in
     * for the full taste-matching algorithm described as Ege Yiğit Yıldırım's JOIN/aggregation work.
     */
    public static int computeTasteMatch(int userIdA, int userIdB) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM artist_follows af1 JOIN artist_follows af2 "
                + "   ON af1.artist_id = af2.artist_id WHERE af1.user_id = ? AND af2.user_id = ?) AS shared, "
                + "(SELECT COUNT(*) FROM artist_follows WHERE user_id = ?) AS countA, "
                + "(SELECT COUNT(*) FROM artist_follows WHERE user_id = ?) AS countB";
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userIdA);
            ps.setInt(2, userIdB);
            ps.setInt(3, userIdA);
            ps.setInt(4, userIdB);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int shared = rs.getInt("shared");
                int denominator = Math.max(rs.getInt("countA"), rs.getInt("countB"));
                return denominator == 0 ? 0 : (int) Math.round(100.0 * shared / denominator);
            }
        }
    }

    /** Names of artists both users follow - the "why" behind a taste-match percentage. */
    public static List<String> getSharedArtistNames(int userIdA, int userIdB) throws SQLException {
        String sql = "SELECT a.name FROM artist_follows af1 JOIN artist_follows af2 ON af1.artist_id = af2.artist_id "
                + "JOIN artists a ON a.id = af1.artist_id WHERE af1.user_id = ? AND af2.user_id = ? ORDER BY a.name";
        List<String> names = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userIdA);
            ps.setInt(2, userIdB);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }
        return names;
    }

    public static List<User> getAllUsersExcept(int userId) throws SQLException {
        String sql = "SELECT id, username, password_hash, bio, verified, review_count FROM users WHERE id != ?";
        List<User> result = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(userFromRow(rs));
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------------------------
    // Artists / Tags - owned by Emir Selim Kayhan (Artist, Tag & Streaming)
    // ---------------------------------------------------------------------
    public static Artist insertArtist(String name, String description) throws SQLException {
        try (Connection connection = connect()) {
            int id = insertArtistRaw(connection, name, description);
            return new Artist(id, name, description);
        }
    }

    public static List<Artist> getAllArtists() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id FROM artists ORDER BY name")) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        List<Artist> artists = new ArrayList<>();
        for (int id : ids) {
            artists.add(loadArtist(id));
        }
        return artists;
    }

    public static Artist loadArtist(int artistId) throws SQLException {
        try (Connection connection = connect()) {
            Artist artist;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT name, description, follower_count FROM artists WHERE id = ?")) {
                ps.setInt(1, artistId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    artist = new Artist(artistId, rs.getString("name"), rs.getString("description"));
                    int followers = rs.getInt("follower_count");
                    for (int i = 0; i < followers; i++) {
                        artist.incrementFollowerCount();
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT t.id, t.name FROM tags t JOIN artist_tags at ON at.tag_id = t.id WHERE at.artist_id = ?")) {
                ps.setInt(1, artistId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        artist.addTag(new Tag(rs.getInt("id"), rs.getString("name")));
                    }
                }
            }

            Map<Integer, Album> albumsById = new LinkedHashMap<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, title, release_year FROM albums WHERE artist_id = ?")) {
                ps.setInt(1, artistId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Album album = new Album(rs.getInt("id"), rs.getString("title"), artist, rs.getInt("release_year"));
                        albumsById.put(album.getItemId(), album);
                        artist.addMusicItem(album);
                        loadReviewsInto(connection, "ALBUM", album.getItemId(), album::addReview);
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, album_id, title, duration_seconds FROM songs WHERE artist_id = ?")) {
                ps.setInt(1, artistId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Song song = new Song(rs.getInt("id"), rs.getString("title"), artist, rs.getInt("duration_seconds"));
                        loadStreamingLinksInto(connection, song);
                        loadReviewsInto(connection, "SONG", song.getItemId(), song::addReview);
                        artist.addMusicItem(song);
                        int albumId = rs.getInt("album_id");
                        if (!rs.wasNull() && albumsById.containsKey(albumId)) {
                            albumsById.get(albumId).addSong(song);
                        }
                    }
                }
            }

            loadReviewsInto(connection, "ARTIST", artistId, artist::addReview);
            return artist;
        }
    }

    private static void loadStreamingLinksInto(Connection connection, Song song) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT platform, url FROM streaming_links WHERE song_id = ?")) {
            ps.setInt(1, song.getItemId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    song.addStreamingLink(rs.getString("platform"), rs.getString("url"));
                }
            }
        }
    }

    private static void loadReviewsInto(Connection connection, String targetType, int targetId,
                                         java.util.function.Consumer<Review> sink) throws SQLException {
        String sql = "SELECT r.id, r.star_rating, r.comment, r.priority_score, u.username, u.verified "
                + "FROM reviews r JOIN users u ON u.id = r.user_id "
                + "WHERE r.target_type = ? AND r.target_id = ? ORDER BY r.created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, targetType);
            ps.setInt(2, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sink.accept(new Review(rs.getInt("id"), rs.getString("username"), rs.getInt("star_rating"),
                            rs.getString("comment"), rs.getBoolean("verified")));
                }
            }
        }
    }

    public static Tag addTag(int artistId, String tagName) throws SQLException {
        try (Connection connection = connect()) {
            int tagId = findOrCreateTagRaw(connection, tagName);
            tagArtist(connection, artistId, tagName);
            return new Tag(tagId, tagName);
        }
    }

    public static void removeTag(int artistId, int tagId) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM artist_tags WHERE artist_id = ? AND tag_id = ?")) {
            ps.setInt(1, artistId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }

    public static List<Tag> getAllTags() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name FROM tags ORDER BY name")) {
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        }
        return tags;
    }

    public static void followArtist(int userId, int artistId) throws SQLException {
        try (Connection connection = connect()) {
            followArtistRaw(connection, userId, artistId);
        }
    }

    public static void unfollowArtist(int userId, int artistId) throws SQLException {
        try (Connection connection = connect()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM artist_follows WHERE user_id = ? AND artist_id = ?")) {
                ps.setInt(1, userId);
                ps.setInt(2, artistId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE artists SET follower_count = GREATEST(follower_count - 1, 0) WHERE id = ?")) {
                ps.setInt(1, artistId);
                ps.executeUpdate();
            }
        }
    }

    public static boolean isFollowingArtist(int userId, int artistId) throws SQLException {
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM artist_follows WHERE user_id = ? AND artist_id = ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, artistId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void rateArtist(int artistId, int userId, boolean verified, int stars, String comment) throws SQLException {
        try (Connection connection = connect()) {
            insertReviewRaw(connection, userId, verified, "ARTIST", artistId, stars, comment == null ? "" : comment);
            incrementReviewCountAndMaybeVerify(connection, userId);
        }
    }

    // ---------------------------------------------------------------------
    // Music items - owned by Mustafa Ziya Akyol (Music Items & Listen Later)
    // ---------------------------------------------------------------------

    /** Adds a new album to an artist's discography. Only a VerifiedUser may call this. */
    public static int insertAlbum(int artistId, String title, int releaseYear) throws SQLException {
        try (Connection connection = connect()) {
            return insertAlbumRaw(connection, artistId, title, releaseYear);
        }
    }

    /** Adds a new song to an artist's catalog, optionally attached to one of their albums. */
    public static int insertSong(int artistId, Integer albumId, String title, int durationSeconds) throws SQLException {
        try (Connection connection = connect()) {
            return insertSongRaw(connection, artistId, albumId, title, durationSeconds);
        }
    }

    public static void rateItem(String itemType, int itemId, int userId, boolean verified, int stars, String comment)
            throws SQLException {
        try (Connection connection = connect()) {
            insertReviewRaw(connection, userId, verified, itemType, itemId, stars, comment == null ? "" : comment);
            incrementReviewCountAndMaybeVerify(connection, userId);
        }
    }

    public static void addToListenLater(int userId, String itemType, int itemId) throws SQLException {
        try (Connection connection = connect()) {
            addListenLaterRaw(connection, userId, itemType, itemId);
        }
    }

    public static void removeFromListenLater(int userId, String itemType, int itemId) throws SQLException {
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM listen_later WHERE user_id = ? AND item_type = ? AND item_id = ?")) {
            ps.setInt(1, userId);
            ps.setString(2, itemType);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        }
    }

    public static boolean isInListenLater(int userId, String itemType, int itemId) throws SQLException {
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM listen_later WHERE user_id = ? AND item_type = ? AND item_id = ?")) {
            ps.setInt(1, userId);
            ps.setString(2, itemType);
            ps.setInt(3, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Lightweight rows for the Listen Later page: type/id/title/subtitle/when-saved. */
    public record ListenLaterEntry(String itemType, int itemId, String title, String subtitle, Timestamp savedAt) {
    }

    public static List<ListenLaterEntry> getListenLater(int userId) throws SQLException {
        List<ListenLaterEntry> entries = new ArrayList<>();
        String songSql = "SELECT s.id, s.title, s.duration_seconds, a.name AS artist_name, ll.saved_at "
                + "FROM listen_later ll JOIN songs s ON s.id = ll.item_id AND ll.item_type = 'SONG' "
                + "JOIN artists a ON a.id = s.artist_id WHERE ll.user_id = ?";
        String albumSql = "SELECT al.id, al.title, al.release_year, a.name AS artist_name, ll.saved_at "
                + "FROM listen_later ll JOIN albums al ON al.id = ll.item_id AND ll.item_type = 'ALBUM' "
                + "JOIN artists a ON a.id = al.artist_id WHERE ll.user_id = ?";
        try (Connection connection = connect()) {
            try (PreparedStatement ps = connection.prepareStatement(songSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String subtitle = rs.getString("artist_name") + " · " + formatDuration(rs.getInt("duration_seconds"));
                        entries.add(new ListenLaterEntry("SONG", rs.getInt("id"), rs.getString("title"), subtitle,
                                rs.getTimestamp("saved_at")));
                    }
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(albumSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String subtitle = rs.getString("artist_name") + " · " + rs.getInt("release_year");
                        entries.add(new ListenLaterEntry("ALBUM", rs.getInt("id"), rs.getString("title"), subtitle,
                                rs.getTimestamp("saved_at")));
                    }
                }
            }
        }
        entries.sort((a, b) -> b.savedAt().compareTo(a.savedAt()));
        return entries;
    }

    public static Song loadSong(int songId) throws SQLException {
        try (Connection connection = connect()) {
            Song song;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT s.title, s.duration_seconds, s.artist_id, a.name AS artist_name, a.description "
                            + "FROM songs s JOIN artists a ON a.id = s.artist_id WHERE s.id = ?")) {
                ps.setInt(1, songId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Artist artist = new Artist(rs.getInt("artist_id"), rs.getString("artist_name"), rs.getString("description"));
                    song = new Song(songId, rs.getString("title"), artist, rs.getInt("duration_seconds"));
                }
            }
            loadStreamingLinksInto(connection, song);
            loadReviewsInto(connection, "SONG", songId, song::addReview);
            return song;
        }
    }

    public static Album loadAlbum(int albumId) throws SQLException {
        try (Connection connection = connect()) {
            Album album;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT al.title, al.release_year, al.artist_id, a.name AS artist_name, a.description "
                            + "FROM albums al JOIN artists a ON a.id = al.artist_id WHERE al.id = ?")) {
                ps.setInt(1, albumId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Artist artist = new Artist(rs.getInt("artist_id"), rs.getString("artist_name"), rs.getString("description"));
                    album = new Album(albumId, rs.getString("title"), artist, rs.getInt("release_year"));
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, title, duration_seconds FROM songs WHERE album_id = ?")) {
                ps.setInt(1, albumId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Song track = new Song(rs.getInt("id"), rs.getString("title"), album.getArtist(), rs.getInt("duration_seconds"));
                        loadReviewsInto(connection, "SONG", track.getItemId(), track::addReview);
                        album.addSong(track);
                    }
                }
            }
            loadReviewsInto(connection, "ALBUM", albumId, album::addReview);
            return album;
        }
    }

    /** The album a song appears on, or null if it's a standalone single. Used by the Song page's "Appears On" link. */
    public static Album findContainingAlbum(int songId) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement("SELECT album_id FROM songs WHERE id = ?")) {
            ps.setInt(1, songId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getObject("album_id") == null) {
                    return null;
                }
                return loadAlbum(rs.getInt("album_id"));
            }
        }
    }

    private static String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    // ---------------------------------------------------------------------
    // Search - owned by Ege Yiğit Yıldırım (Search, Database & Integration)
    // ---------------------------------------------------------------------
    /** Lightweight rows for search results: type/id/title/subtitle. */
    public record SearchResult(String type, int id, String title, String subtitle) {
    }

    public static List<SearchResult> searchMusicItem(String query) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        String needle = "%" + query + "%";
        try (Connection connection = connect()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, name, description FROM artists WHERE name LIKE ?")) {
                ps.setString(1, needle);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult("ARTIST", rs.getInt("id"), rs.getString("name"), "Artist"));
                    }
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT al.id, al.title, al.release_year, a.name AS artist_name "
                            + "FROM albums al JOIN artists a ON a.id = al.artist_id WHERE al.title LIKE ?")) {
                ps.setString(1, needle);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult("ALBUM", rs.getInt("id"), rs.getString("title"),
                                rs.getString("artist_name") + " · " + rs.getInt("release_year")));
                    }
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT s.id, s.title, a.name AS artist_name FROM songs s "
                            + "JOIN artists a ON a.id = s.artist_id WHERE s.title LIKE ?")) {
                ps.setString(1, needle);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult("SONG", rs.getInt("id"), rs.getString("title"), rs.getString("artist_name")));
                    }
                }
            }
        }
        return results;
    }

    // ---------------------------------------------------------------------
    // Feed - owned by Abdullah Efe Anık (Review & Social Feed)
    // ---------------------------------------------------------------------
    /** A row in the home feed: who did what, to what, with what rating. */
    public record FeedEntry(String reviewerUsername, boolean verified, String targetType, String targetLabel,
                             int targetId, int starRating, String comment, Timestamp createdAt) {
    }

    public static List<FeedEntry> getFeed(int currentUserId, int limit) throws SQLException {
        List<Integer> followedIds = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(
                "SELECT followee_id FROM user_follows WHERE follower_id = ?")) {
            ps.setInt(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    followedIds.add(rs.getInt(1));
                }
            }
        }

        boolean onlyFollowed = !followedIds.isEmpty();
        StringBuilder sql = new StringBuilder(
                "SELECT r.star_rating, r.comment, r.priority_score, r.created_at, r.target_type, r.target_id, "
                        + "u.username, u.verified, "
                        + "CASE r.target_type "
                        + "  WHEN 'ARTIST' THEN (SELECT name FROM artists WHERE id = r.target_id) "
                        + "  WHEN 'ALBUM' THEN (SELECT CONCAT(title, ' — ', (SELECT name FROM artists a2 WHERE a2.id = al.artist_id)) FROM albums al WHERE al.id = r.target_id) "
                        + "  WHEN 'SONG' THEN (SELECT CONCAT(title, ' — ', (SELECT name FROM artists a3 WHERE a3.id = s.artist_id)) FROM songs s WHERE s.id = r.target_id) "
                        + "END AS target_label "
                        + "FROM reviews r JOIN users u ON u.id = r.user_id ");
        if (onlyFollowed) {
            sql.append("WHERE r.user_id IN (")
                    .append(String.join(",", followedIds.stream().map(String::valueOf).toList()))
                    .append(") ");
        }
        sql.append("ORDER BY r.priority_score DESC, r.created_at DESC LIMIT ?");

        List<FeedEntry> feed = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feed.add(new FeedEntry(rs.getString("username"), rs.getBoolean("verified"), rs.getString("target_type"),
                            rs.getString("target_label"), rs.getInt("target_id"), rs.getInt("star_rating"),
                            rs.getString("comment"), rs.getTimestamp("created_at")));
                }
            }
        }
        return feed;
    }

    /** All reviews written by one user, newest first - used by the Profile page's "My Reviews" list. */
    public static List<FeedEntry> getReviewsByUser(int userId) throws SQLException {
        String sql = "SELECT r.star_rating, r.comment, r.priority_score, r.created_at, r.target_type, r.target_id, "
                + "u.username, u.verified, "
                + "CASE r.target_type "
                + "  WHEN 'ARTIST' THEN (SELECT name FROM artists WHERE id = r.target_id) "
                + "  WHEN 'ALBUM' THEN (SELECT CONCAT(title, ' — ', (SELECT name FROM artists a2 WHERE a2.id = al.artist_id)) FROM albums al WHERE al.id = r.target_id) "
                + "  WHEN 'SONG' THEN (SELECT CONCAT(title, ' — ', (SELECT name FROM artists a3 WHERE a3.id = s.artist_id)) FROM songs s WHERE s.id = r.target_id) "
                + "END AS target_label "
                + "FROM reviews r JOIN users u ON u.id = r.user_id WHERE r.user_id = ? ORDER BY r.created_at DESC";
        List<FeedEntry> reviews = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new FeedEntry(rs.getString("username"), rs.getBoolean("verified"), rs.getString("target_type"),
                            rs.getString("target_label"), rs.getInt("target_id"), rs.getInt("star_rating"),
                            rs.getString("comment"), rs.getTimestamp("created_at")));
                }
            }
        }
        return reviews;
    }
}
