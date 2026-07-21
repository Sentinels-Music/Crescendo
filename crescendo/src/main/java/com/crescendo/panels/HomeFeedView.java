package com.crescendo.panels;

import com.crescendo.controller.ArtistController;
import com.crescendo.controller.FeedController;
import com.crescendo.db.Database;
import com.crescendo.model.Artist;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/** Owned by Abdullah Efe Anık (Review & Social Feed) - the Home Page feed. */
public final class HomeFeedView {
    private HomeFeedView() {
    }

    public static Parent build(Nav nav, FeedController feedController, ArtistController artistController) {
        List<Database.FeedEntry> feed = feedController.getFeed(nav.currentUser);
        boolean followsAnyone = !nav.currentUser.getFollowedUsers().isEmpty();

        Label eyebrow = Widgets.eyebrow("Your Feed");
        Label heading = new Label(followsAnyone || !feed.isEmpty() ? "From people you follow" : "Discover what's happening");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");

        VBox list = new VBox(0);
        if (feed.isEmpty()) {
            Label empty = new Label("No reviews yet - be the first to rate something from Discover.");
            empty.setTextFill(Color.web(Theme.MUTED));
            list.getChildren().add(empty);
        } else {
            for (Database.FeedEntry entry : feed) {
                list.getChildren().add(feedRow(nav, artistController, entry));
            }
        }

        VBox content = new VBox(16, eyebrow, heading, list);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return Chrome.build(nav, "Home", scroll, "Crescendo · Home");
    }

    private static Region feedRow(Nav nav, ArtistController artistController, Database.FeedEntry entry) {
        StackPane icon = Widgets.noteSquare(40);

        String verb = switch (entry.targetType()) {
            case "ARTIST" -> "added or rated an artist";
            case "ALBUM" -> "reviewed an album";
            default -> "rated a song";
        };

        HBox nameLine = new HBox(6);
        Label reviewer = new Label(entry.reviewerUsername());
        reviewer.setTextFill(Color.web(Theme.INK));
        reviewer.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label action = new Label(verb);
        action.setTextFill(Color.web(Theme.MUTED));
        action.setStyle("-fx-font-size: 13px;");
        nameLine.getChildren().addAll(reviewer, action);
        if (entry.verified()) {
            Label badge = new Label("· Verified");
            badge.setTextFill(Color.web(Theme.GOLD));
            badge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            nameLine.getChildren().add(badge);
        }

        Label target = new Label(entry.targetLabel() == null ? "(removed)" : entry.targetLabel());
        target.setTextFill(Color.web(Theme.INK));
        target.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        HBox ratingRow = new HBox(8, Widgets.stars(entry.starRating()),
                Widgets.labelMuted(String.format("%.1f", (double) entry.starRating())));

        VBox textBlock = new VBox(4, nameLine, target, ratingRow);
        if (entry.comment() != null && !entry.comment().isBlank()) {
            Label comment = new Label("“" + entry.comment() + "”");
            comment.setTextFill(Color.web(Theme.INK));
            comment.setStyle("-fx-font-size: 13px;");
            comment.setWrapText(true);
            textBlock.getChildren().add(comment);
        }
        Label time = Widgets.labelMuted(Widgets.relativeTime(entry.createdAt()));
        textBlock.getChildren().add(time);

        HBox row = new HBox(14, icon, textBlock);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(16, 4, 16, 4));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER + " transparent;"
                + " -fx-border-width: 1; -fx-cursor: hand;");
        row.setOnMouseClicked(e -> navigateToTarget(nav, artistController, entry));
        return row;
    }

    private static void navigateToTarget(Nav nav, ArtistController artistController, Database.FeedEntry entry) {
        switch (entry.targetType()) {
            case "SONG" -> nav.toSong.accept(entry.targetId());
            case "ALBUM" -> nav.toAlbum.accept(entry.targetId());
            case "ARTIST" -> {
                Artist artist = artistController.getArtistById(entry.targetId());
                if (artist != null) {
                    nav.toArtist.accept(artist);
                }
            }
            default -> {
            }
        }
    }
}
