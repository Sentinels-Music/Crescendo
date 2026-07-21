package com.crescendo;

import com.crescendo.controller.ArtistController;
import com.crescendo.controller.AuthController;
import com.crescendo.controller.FeedController;
import com.crescendo.controller.MusicController;
import com.crescendo.controller.SearchController;
import com.crescendo.db.Database;
import com.crescendo.model.Artist;
import com.crescendo.model.User;
import com.crescendo.panels.AlbumView;
import com.crescendo.panels.ArtistPageView;
import com.crescendo.panels.DiscoverPageView;
import com.crescendo.panels.HomeFeedView;
import com.crescendo.panels.ListenLaterView;
import com.crescendo.panels.LoginView;
import com.crescendo.panels.Nav;
import com.crescendo.panels.ProfileView;
import com.crescendo.panels.RegisterView;
import com.crescendo.panels.SearchView;
import com.crescendo.panels.SongView;
import com.crescendo.panels.TasteMatchView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class CrescendoApp extends Application {

    private AuthController authController;
    private ArtistController artistController;
    private MusicController musicController;
    private FeedController feedController;
    private SearchController searchController;
    private final Stack<Runnable> history = new Stack<>();

    @Override
    public void start(Stage stage) {
        try {
            Database.initialize();
        } catch (Exception e) {
            System.err.println("Database initialization warning/error: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Database Warning");
            alert.setHeaderText("Database Initialization Notice");
            alert.setContentText("Could not connect to or initialize MySQL database on port 3306. Please ensure MySQL is running.\n\nDetails: " + e.getMessage());
            alert.showAndWait();
        }

        authController = new AuthController();
        artistController = new ArtistController();
        musicController = new MusicController();
        feedController = new FeedController();
        searchController = new SearchController();

        showLogin(stage);
    }

    private void showLogin(Stage stage) {
        history.clear();
        Parent root = LoginView.build(stage, authController, user -> showScreen(stage, user, "Home", null), () -> showRegister(stage));
        setScene(stage, root, "Crescendo - Login");
    }

    private void showRegister(Stage stage) {
        history.clear();
        Parent root = RegisterView.build(stage, authController, user -> showScreen(stage, user, "Home", null), () -> showLogin(stage));
        setScene(stage, root, "Crescendo - Register");
    }

    private void showScreen(Stage stage, User user, String screenName, Object param) {
        boolean canGoBack = !history.isEmpty();
        Runnable toBack = () -> {
            if (!history.isEmpty()) {
                history.pop().run();
            }
        };

        Runnable toHome = () -> {
            history.clear();
            showScreen(stage, user, "Home", null);
        };
        Runnable toDiscover = () -> {
            history.clear();
            showScreen(stage, user, "Discover", null);
        };
        Runnable toListenLater = () -> {
            history.clear();
            showScreen(stage, user, "ListenLater", null);
        };
        Runnable toTasteMatch = () -> {
            history.clear();
            showScreen(stage, user, "TasteMatch", null);
        };
        Runnable toProfile = () -> {
            history.clear();
            showScreen(stage, user, "Profile", null);
        };
        Consumer<String> toSearch = query -> {
            Runnable restoreCurrent = () -> showScreen(stage, user, screenName, param);
            history.push(restoreCurrent);
            showScreen(stage, user, "Search", query);
        };
        Consumer<Artist> toArtist = artist -> {
            Runnable restoreCurrent = () -> showScreen(stage, user, screenName, param);
            history.push(restoreCurrent);
            showScreen(stage, user, "Artist", artist);
        };
        IntConsumer toSong = songId -> {
            Runnable restoreCurrent = () -> showScreen(stage, user, screenName, param);
            history.push(restoreCurrent);
            showScreen(stage, user, "Song", songId);
        };
        IntConsumer toAlbum = albumId -> {
            Runnable restoreCurrent = () -> showScreen(stage, user, screenName, param);
            history.push(restoreCurrent);
            showScreen(stage, user, "Album", albumId);
        };
        Runnable toLogout = () -> showLogin(stage);

        Nav nav = new Nav(stage, user, canGoBack, toHome, toDiscover, toListenLater,
                toTasteMatch, toProfile, toSearch, toArtist, toSong, toAlbum, toBack, toLogout);

        Runnable onRefresh = () -> showScreen(stage, user, screenName, param);

        Parent root = switch (screenName) {
            case "Home" -> HomeFeedView.build(nav, feedController, artistController);
            case "Discover" -> DiscoverPageView.build(nav, artistController, onRefresh);
            case "ListenLater" -> ListenLaterView.build(nav, musicController, onRefresh);
            case "TasteMatch" -> TasteMatchView.build(nav, authController, onRefresh);
            case "Profile" -> ProfileView.build(nav, authController, feedController, onRefresh);
            case "Search" -> SearchView.build(nav, searchController, artistController, (String) param);
            case "Artist" -> ArtistPageView.build(nav, artistController, musicController, (Artist) param, onRefresh);
            case "Song" -> SongView.build(nav, musicController, artistController, (Integer) param, onRefresh);
            case "Album" -> AlbumView.build(nav, musicController, artistController, (Integer) param, onRefresh);
            default -> HomeFeedView.build(nav, feedController, artistController);
        };

        setScene(stage, root, "Crescendo - " + screenName);
    }

    private void setScene(Stage stage, Parent root, String title) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1080, 720));
        } else {
            stage.getScene().setRoot(root);
        }
        stage.setTitle(title);
        stage.show();
    }
}
