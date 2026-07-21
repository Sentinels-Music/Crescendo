package com.crescendo.panels;

import com.crescendo.controller.ArtistController;
import com.crescendo.controller.MusicController;
import com.crescendo.model.Album;
import com.crescendo.model.Review;
import com.crescendo.model.Song;
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

/** Owned by Mustafa Ziya Akyol (Music Items & Listen Later) - the Song Page. */
public final class SongView {
    private SongView() {
    }

    public static Parent build(Nav nav, MusicController musicController, ArtistController artistController,
                                int songId, Runnable onRefresh) {
        Song song = musicController.getSong(songId);
        if (song == null) {
            return Chrome.build(nav, "Discover", Widgets.labelMuted("Song not found."), "Crescendo · Song");
        }

        StackPane icon = Widgets.noteSquare(90);

        Label eyebrow = Widgets.eyebrow("Song");
        Label title = new Label(song.getTitle());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        Label artistLink = new Label(song.getArtist().getName());
        artistLink.setTextFill(Color.web(Theme.GOLD));
        artistLink.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        artistLink.setOnMouseClicked(e -> nav.toArtist.accept(artistController.getArtistById(song.getArtist().getArtistId())));

        Label subtitle = new Label(" · " + Widgets.formatDuration(song.getDurationInSeconds()));
        subtitle.setTextFill(Color.web(Theme.MUTED));
        subtitle.setStyle("-fx-font-size: 13px;");

        HBox subtitleRow = new HBox(artistLink, subtitle);
        VBox titleBlock = new VBox(6, eyebrow, title, subtitleRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox ratingBlock = Widgets.bigRating(song.calculateAverageRating(), song.getReviews().size());

        boolean saved = musicController.isInListenLater(nav.currentUser, "SONG", songId);
        Button listenLaterButton = new Button(saved ? "✓ Saved" : "+ Listen Later");
        listenLaterButton.setStyle("-fx-background-color: " + (saved ? "white" : Theme.GOLD) + ";"
                + " -fx-text-fill: #11110E; -fx-font-size: 12px; -fx-font-weight: bold;"
                + " -fx-border-color: " + Theme.GOLD + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 6 14 6 14;");
        listenLaterButton.setOnAction(e -> {
            if (saved) {
                musicController.removeFromListenLater(nav.currentUser, "SONG", songId);
            } else {
                musicController.addToListenLater(nav.currentUser, "SONG", songId);
            }
            onRefresh.run();
        });

        HBox header = new HBox(20, icon, titleBlock, spacer, new VBox(10, ratingBlock, listenLaterButton));
        header.setAlignment(Pos.TOP_LEFT);

        Button rateButton = new Button("Rate");
        rateButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.INK + ";"
                + " -fx-border-color: " + Theme.INK + "; -fx-border-radius: 10; -fx-background-radius: 10;"
                + " -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        rateButton.setOnAction(e -> RateDialog.show(nav.stage, song.getTitle(), (stars, comment) -> {
            musicController.rateItem("SONG", songId, nav.currentUser, stars, comment);
            onRefresh.run();
        }));

        VBox yourRating = new VBox(8, Widgets.eyebrow("Your Rating"), rateButton);

        VBox content = new VBox(24, header, Widgets.divider(), yourRating);

        Album containingAlbum = musicController.getContainingAlbum(songId);
        if (containingAlbum != null) {
            content.getChildren().addAll(Widgets.divider(), buildAppearsOn(nav, containingAlbum));
        }

        content.getChildren().addAll(Widgets.divider(), buildReviews(song.getReviews()));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return Chrome.build(nav, "Discover", scroll, "Crescendo · Song");
    }

    private static Region buildAppearsOn(Nav nav, Album album) {
        Region row = AlbumView.miniAlbumRow(album);
        row.setOnMouseClicked(e -> nav.toAlbum.accept(album.getItemId()));
        row.setStyle(row.getStyle() + " -fx-cursor: hand;");
        return new VBox(10, Widgets.eyebrow("Appears On"), row);
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
