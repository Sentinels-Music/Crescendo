package panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import controllers.ArtistController;
import controllers.TagController;
import model.Album;
import model.Artist;
import model.Song;
import model.StreamingLink;
import model.Tag;


public class ArtistPage extends Application {

    private final ArtistController artistController = new ArtistController();
    private final TagController tagController = new TagController();

    private Stage stage;
    private Scene scene;

   
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        List<Artist> all = artistController.getAllArtists();
        if (all.isEmpty()) {
            render("", placeholder("No artists found",
                    "The Artists table is empty, or the database could not be reached. "
                    + "Check controllers.DatabaseManager's connection settings."));
        }
        else {
            Artist artist = artistController.getArtistById(all.get(0).getArtistId());
            render("", buildArtistContent(artist, null));
        }
        stage.setTitle("Crescendo · Artist");
        stage.show();
    }

    // Draws the given content inside the shell and (re)uses one Scene. 
    private void render(String activeNav, Node content) {
        Runnable goHome = () -> render("Home",
                placeholder("Home", "This screen is implemented by teammate Abdullah Efe Anık."));
        Runnable goDiscover = () -> new DiscoverPage().showIn(stage);
        Runnable goListenLater = () -> render("Listen Later",
                placeholder("Listen Later", "This screen is implemented by teammate Mustafa Ziya Akyol."));

        AnchorPane shell = buildShell(stage, activeNav, content, goHome, goDiscover, goListenLater);

        if (scene == null) {
            scene = new Scene(shell, 933, 695);
            scene.getStylesheets().add(ArtistPage.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
        }
        else {
            scene.setRoot(shell);
        }
    }

    
    //  Shared shell: sidebar + search bar + close button 
    public static AnchorPane buildShell(Stage stage, String activeNav, Node content,
                                        Runnable goHome, Runnable goDiscover, Runnable goListenLater) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(933, 695);
        root.setStyle("-fx-background-color: #F6F1E8;");

        // left sidebar
        AnchorPane sidebar = new AnchorPane();
        sidebar.setPrefWidth(242);
        sidebar.setStyle("-fx-background-color: #171713; -fx-background-radius: 0 10 10 0;");
        AnchorPane.setTopAnchor(sidebar, 0.0);
        AnchorPane.setBottomAnchor(sidebar, 0.0);
        AnchorPane.setLeftAnchor(sidebar, 0.0);

        Label brand = new Label("Crescendo");
        brand.setLayoutX(20);
        brand.setLayoutY(38);
        brand.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #F4D66A;");

        VBox navBox = new VBox(16);
        navBox.setLayoutX(20);
        navBox.setLayoutY(106);
        navBox.setPrefWidth(202);
        navBox.getChildren().addAll(
                sideNavButton("Home", activeNav, goHome),
                sideNavButton("Discover", activeNav, goDiscover),
                sideNavButton("Listen Later", activeNav, goListenLater));

        HBox userCard = new HBox(12);
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setPrefSize(202, 62);
        userCard.setStyle("-fx-background-color: #22211C; -fx-background-radius: 18;"
                + " -fx-padding: 10 12 10 12; -fx-cursor: hand;");
        userCard.setOnMouseClicked(e -> views.SceneNavigator.switchTo(stage, "ProfileView.fxml"));
        AnchorPane.setLeftAnchor(userCard, 20.0);
        AnchorPane.setBottomAnchor(userCard, 24.0);
        Circle avatar = new Circle(21, Color.web("#8B8B83"));
        Label userName = new Label("Example User");
        userName.setStyle("-fx-text-fill: #F6F1E8; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label userTag = new Label("Verified");
        userTag.setStyle("-fx-text-fill: #E2AE3F; -fx-font-size: 12px; -fx-font-weight: bold;");
        userCard.getChildren().addAll(avatar, new VBox(2, userName, userTag));

        sidebar.getChildren().addAll(brand, navBox, userCard);

        // search bar (top-right) 
        HBox search = new HBox(10);
        search.setAlignment(Pos.CENTER_LEFT);
        search.setPrefSize(310, 42);
        search.setStyle("-fx-background-color: white; -fx-background-radius: 18;"
                + " -fx-border-color: #E4D8C2; -fx-border-radius: 18; -fx-padding: 0 14 0 14;");
        AnchorPane.setTopAnchor(search, 32.0);
        AnchorPane.setRightAnchor(search, 72.0);
        Label glass = new Label("⌕");
        glass.setStyle("-fx-text-fill: #737373; -fx-font-size: 16px;");
        TextField searchField = new TextField();
        searchField.setPromptText("Search artists, albums and songs...");
        searchField.setPrefWidth(240);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"
                + " -fx-font-size: 14px; -fx-prompt-text-fill: #8A8A8A;");
        // see controllers.SearchController / panels.searchPanel.
        search.getChildren().addAll(glass, searchField);

        // close button
        Button close = new Button("×");
        close.setPrefSize(32, 32);
        close.setStyle("-fx-background-color: #D94141; -fx-text-fill: white; -fx-font-size: 18px;"
                + " -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 0; -fx-cursor: hand;");
        close.setOnAction(e -> stage.close());
        AnchorPane.setTopAnchor(close, 12.0);
        AnchorPane.setRightAnchor(close, 14.0);

        // content area (scrolls)
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        AnchorPane.setTopAnchor(scroll, 90.0);
        AnchorPane.setLeftAnchor(scroll, 262.0);
        AnchorPane.setRightAnchor(scroll, 24.0);
        AnchorPane.setBottomAnchor(scroll, 20.0);

        root.getChildren().addAll(sidebar, scroll, search, close);
        return root;
    }

    private static Button sideNavButton(String text, String activeNav, Runnable action) {
        Button b = new Button(text);
        b.setPrefWidth(202);
        b.setAlignment(Pos.CENTER_LEFT);
        boolean active = text.equals(activeNav);
        if (active){
            b.setPrefHeight(50);
            b.setStyle("-fx-background-color: #E2AE3F; -fx-text-fill: #11110E; -fx-font-size: 15px;"
                    + " -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 0 0 0 18;");
        } 
        else {
            b.setPrefHeight(46);
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: #EEE6D8; -fx-font-size: 15px;"
                    + " -fx-padding: 0 0 0 18; -fx-cursor: hand;");
        }
        if (action != null) {
            b.setOnAction(e -> action.run());
        }
        return b;
    }

    // A simple centered notice used for screens that teammates will implement, or DB errors. 
    public static VBox placeholder(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("placeholder-title");
        Label sub = new Label(message);
        sub.getStyleClass().add("placeholder-sub");
        sub.setWrapText(true);
        sub.setMaxWidth(560);
        VBox box = new VBox(10, titleLabel, sub);
        box.setPadding(new Insets(40, 0, 0, 0));
        return box;
    }

    
    //  Artist content (a Node, so DiscoverPage can embed it too)
    public VBox buildArtistContent(Artist artist, Runnable onBack) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(4, 8, 20, 4));

        if (onBack != null) {
            Button back = new Button("‹  Back");
            back.getStyleClass().add("back-button");
            back.setOnAction(e -> onBack.run());
            page.getChildren().add(back);
        }

        // header
        Label name = new Label(artist.getName());
        name.getStyleClass().add("artist-name");
        HBox nameRow = new HBox(14, name);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        if (artist.isVerified()) {
            Label v = new Label("★ Verified");
            v.getStyleClass().add("badge-verified");
            nameRow.getChildren().add(v);
        }

        Label subtitle = new Label(artist.getMainGenre() + "  ·  " + artist.getAlbumCount() + " albums");
        subtitle.getStyleClass().add("subtitle");

        // stat cards
        HBox stats = new HBox(16,
                statCard(String.valueOf(artist.getAlbumCount()), "ALBUMS"),
                statCard(String.valueOf(artist.getSongCount()), "SONGS"),
                statCard(String.format("%.1f", artist.calculateAverageRating()), "AVG RATING"));

        // follow button (persists through ArtistController)
        Button follow = new Button();
        Label followers = new Label();
        refreshFollow(follow, followers, artist);
        follow.setOnAction(e -> {
            if (artist.isFollowing()) {
                artist.unfollow();
                artistController.unfollowArtist(artist.getArtistId());
            } else {
                artist.follow();
                artistController.followArtist(artist.getArtistId());
            }
            refreshFollow(follow, followers, artist);
        });
        HBox followRow = new HBox(14, follow, followers);
        followRow.setAlignment(Pos.CENTER_LEFT);

        // genres (add / remove tag  a VerifiedUser privilege)
        Label genresTitle = new Label("G E N R E S");
        genresTitle.getStyleClass().add("section-title");
        FlowPane tagsPane = new FlowPane(8, 8);
        fillTags(tagsPane, artist);

        // discography
        Label discoTitle = new Label("D I S C O G R A P H Y");
        discoTitle.getStyleClass().add("section-title");
        VBox disco = new VBox(10);
        if (artist.getAlbums().isEmpty()) {
            disco.getChildren().add(emptyRow("No albums yet."));
        }
        for (Album album : artist.getAlbums()) {
            disco.getChildren().add(albumRow(album));
        }

        // popular songs
        Label popTitle = new Label("P O P U L A R   S O N G S");
        popTitle.getStyleClass().add("section-title");
        VBox popular = new VBox(10);
        ArrayList<Song> topSongs = artist.getPopularSongs(5);
        if (topSongs.isEmpty()) {
            popular.getChildren().add(emptyRow("No songs yet."));
        }
        for (Song song : topSongs) {
            popular.getChildren().add(songRow(song));
        }

        page.getChildren().addAll(nameRow, subtitle, stats, followRow,
                genresTitle, tagsPane, discoTitle, disco, popTitle, popular);
        return page;
    }

    private static void refreshFollow(Button follow, Label followers, Artist artist) {
        follow.setText(artist.isFollowing() ? "Following" : "+ Follow");
        follow.getStyleClass().setAll(artist.isFollowing() ? "following-button" : "gold-button");
        followers.setText(artist.getFollowerCount() + " followers");
        followers.getStyleClass().setAll("item-sub");
    }

    // Fills the tag area with removable chips plus an "+ Add tag" button.
    private void fillTags(FlowPane pane, Artist artist) {
        pane.getChildren().clear();
        for (Tag tag : new ArrayList<>(artist.getTags())) {
            Label label = new Label(tag.getName());
            label.getStyleClass().add("item-title");
            Button remove = new Button("×");
            remove.getStyleClass().add("remove-tag");
            remove.setOnAction(e -> {
                artist.removeTag(tag);
                tagController.removeTag(artist.getArtistId(), tag.getTagId());   // TagController.removeTag()
                fillTags(pane, artist);
            });
            HBox chip = new HBox(2, label, remove);
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.getStyleClass().add("tag-chip");
            pane.getChildren().add(chip);
        }

        Button addTag = new Button("+ Add tag");
        addTag.getStyleClass().add("ghost-button");
        addTag.setOnAction(e -> {
            String value = askText("Add tag", "Genre / tag name:");
            if (value != null && !value.trim().isEmpty()) {
                Tag saved = tagController.addTag(artist.getArtistId(), value.trim());   // TagController.addTag()
                if (saved != null) {
                    artist.addTag(saved);
                    fillTags(pane, artist);
                }
            }
        });
        pane.getChildren().add(addTag);
    }

    //  Small building blocks
    private static VBox statCard(String number, String label) {
        Label n = new Label(number);
        n.getStyleClass().add("stat-number");
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");
        VBox card = new VBox(4, n, l);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private static Label emptyRow(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("item-sub");
        return label;
    }

    private static HBox albumRow(Album album) {
        Label note = new Label("♪");
        note.setStyle("-fx-text-fill: #E2AE3F; -fx-font-size: 18px;");
        Label title = new Label(album.getTitle());
        title.getStyleClass().add("item-title");
        Label sub = new Label(String.valueOf(album.getReleaseYear()));
        sub.getStyleClass().add("item-sub");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, note, new VBox(2, title, sub), spacer,
                starRow(album.getAverageRating(), true));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        return row;
    }

    private static HBox songRow(Song song) {
        Label note = new Label("♪");
        note.setStyle("-fx-text-fill: #E2AE3F; -fx-font-size: 18px;");
        Label title = new Label(song.getTitle());
        title.getStyleClass().add("item-title");
        Label sub = new Label(song.getFormattedDuration());
        sub.getStyleClass().add("item-sub");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // "Play" opens the song on its streaming platform via StreamingLink.open()
        Button play = new Button("▶ Play");
        play.getStyleClass().add("play-button");
        play.setOnAction(e -> {
            StreamingLink link = song.getStreamingLink();
            if (link != null) {
                System.out.println("Opening \"" + song.getTitle() + "\" on " + link.getPlatform());
                link.open();
            } else {
                System.out.println("No streaming link for \"" + song.getTitle() + "\".");
            }
        });

        HBox row = new HBox(14, note, new VBox(2, title, sub), spacer,
                starRow(song.getAverageRating(), true), play);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        return row;
    }

    //* Builds a row of five stars (filled up to the rating) plus the number. 
    public static HBox starRow(double rating, boolean showNumber) {
        HBox stars = new HBox(1);
        stars.setAlignment(Pos.CENTER_LEFT);
        int filled = (int) Math.round(rating);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add(i <= filled ? "star-filled" : "star-empty");
            stars.getChildren().add(star);
        }
        if (showNumber) {
            Label num = new Label(String.format(" %.1f", rating));
            num.getStyleClass().add("rating-num");
            stars.getChildren().add(num);
        }
        return stars;
    }

    // Small helper around TextInputDialog; returns null if cancelled. 
    public static String askText(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
