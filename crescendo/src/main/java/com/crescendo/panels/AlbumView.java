package com.crescendo.panels;

import com.crescendo.controller.ArtistController;
import com.crescendo.controller.MusicController;
import com.crescendo.model.Album;
import com.crescendo.model.Review;
import com.crescendo.model.Song;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/** Owned by Mustafa Ziya Akyol (Music Items & Listen Later) - the Album Page. */
public final class AlbumView {
    private AlbumView() {
    }

    public static Parent build(Nav nav, MusicController musicController, ArtistController artistController,
                                int albumId, Runnable onRefresh) {
        Album album = musicController.getAlbum(albumId);
        if (album == null) {
            return Chrome.build(nav, "Discover", Widgets.labelMuted("Album not found."), "Crescendo · Album");
        }

        StackPane icon = Widgets.noteSquare(90);

        Label eyebrow = Widgets.eyebrow("Album");
        Label title = new Label(album.getTitle());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        Label artistLink = new Label(album.getArtist().getName());
        artistLink.setTextFill(Color.web(Theme.GOLD));
        artistLink.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        artistLink.setOnMouseClicked(e -> nav.toArtist.accept(artistController.getArtistById(album.getArtist().getArtistId())));

        Label subtitle = new Label(" · " + album.getReleaseYear() + " · " + album.getTrackList().size() + " tracks");
        subtitle.setTextFill(Color.web(Theme.MUTED));
        subtitle.setStyle("-fx-font-size: 13px;");

        HBox subtitleRow = new HBox(artistLink, subtitle);
        VBox titleBlock = new VBox(6, eyebrow, title, subtitleRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox ratingBlock = Widgets.bigRating(album.calculateAverageRating(), album.getReviews().size());

        boolean saved = musicController.isInListenLater(nav.currentUser, "ALBUM", albumId);
        Button listenLaterButton = new Button(saved ? "✓ Saved" : "+ Listen Later");
        listenLaterButton.setStyle("-fx-background-color: " + (saved ? "white" : Theme.GOLD) + ";"
                + " -fx-text-fill: #11110E; -fx-font-size: 12px; -fx-font-weight: bold;"
                + " -fx-border-color: " + Theme.GOLD + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 6 14 6 14;");
        listenLaterButton.setOnAction(e -> {
            if (saved) {
                musicController.removeFromListenLater(nav.currentUser, "ALBUM", albumId);
            } else {
                musicController.addToListenLater(nav.currentUser, "ALBUM", albumId);
            }
            onRefresh.run();
        });

        Button rateButton = new Button("Rate");
        rateButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.INK + ";"
                + " -fx-border-color: " + Theme.INK + "; -fx-border-radius: 10; -fx-background-radius: 10;"
                + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14 6 14;");
        rateButton.setOnAction(e -> RateDialog.show(nav.stage, album.getTitle(), (stars, comment) -> {
            musicController.rateItem("ALBUM", albumId, nav.currentUser, stars, comment);
            onRefresh.run();
        }));

        HBox actionButtons = new HBox(8, rateButton, listenLaterButton);

        HBox header = new HBox(20, icon, titleBlock, spacer, new VBox(10, ratingBlock, actionButtons));
        header.setAlignment(Pos.TOP_LEFT);

        VBox trackList = new VBox(10, Widgets.eyebrow("Tracklist"));
        int i = 1;
        for (Song track : album.getTrackList()) {
            trackList.getChildren().add(trackRow(nav, musicController, i++, track, onRefresh));
        }
        Label hint = Widgets.labelMuted("Tap the stars on any row to rate a song - your rating saves instantly.");

        VBox content = new VBox(24, header, Widgets.divider(), trackList, hint, Widgets.divider(), buildReviews(album.getReviews()));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return Chrome.build(nav, "Discover", scroll, "Crescendo · Album");
    }

    private static Region trackRow(Nav nav, MusicController musicController, int number, Song track, Runnable onRefresh) {
        Label indexLabel = Widgets.labelMuted(String.valueOf(number));
        indexLabel.setPrefWidth(24);

        Label title = new Label(track.getTitle());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        title.setOnMouseClicked(e -> nav.toSong.accept(track.getItemId()));

        Label duration = Widgets.labelMuted(Widgets.formatDuration(track.getDurationInSeconds()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox stars = Widgets.clickableStars(0, rating -> {
            musicController.rateItem("SONG", track.getItemId(), nav.currentUser, rating, "");
            onRefresh.run();
        });

        Label avg = new Label(String.format("%.1f", track.calculateAverageRating()));
        avg.setTextFill(Color.web(Theme.MUTED));
        avg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        avg.setPrefWidth(30);

        HBox row = new HBox(10, indexLabel, title, duration, spacer, stars, avg);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 4, 6, 4));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER + " transparent; -fx-border-width: 1;");
        return row;
    }

    /** Compact album row reused by SongView's "Appears On" section. */
    public static Region miniAlbumRow(Album album) {
        StackPane icon = Widgets.noteSquare(36);
        Label title = new Label(album.getTitle());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label subtitle = Widgets.labelMuted(album.getReleaseYear() + " · " + album.getTrackList().size() + " tracks");
        VBox textBlock = new VBox(2, title, subtitle);
        HBox row = new HBox(12, icon, textBlock);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 10;");
        return row;
    }

    private static Region buildReviews(List<Review> reviews) {
        VBox list = new VBox(14);
        for (Review review : reviews) {
            list.getChildren().add(Widgets.reviewRow(review));
        }
        if (reviews.isEmpty()) {
            list.getChildren().add(Widgets.labelMuted("No reviews yet."));
        }
        return new VBox(10, Widgets.eyebrow("Reviews"), list);
    }
}
