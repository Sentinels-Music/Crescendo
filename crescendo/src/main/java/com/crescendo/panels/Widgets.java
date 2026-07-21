package com.crescendo.panels;

import com.crescendo.model.Review;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * Small reusable pieces shared across every panel: star rows, note thumbnails, section
 * eyebrows, muted labels, dividers and the auth-screen close button/field style.
 */
public final class Widgets {
    private Widgets() {
    }

    public static HBox stars(double rating) {
        HBox row = new HBox(2);
        int filled = (int) Math.round(rating);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setTextFill(Color.web(i <= filled ? Theme.GOLD : Theme.STAR_EMPTY));
            star.setStyle("-fx-font-size: 13px;");
            row.getChildren().add(star);
        }
        return row;
    }

    /** Interactive 1-5 star row: clicking a star invokes onRate immediately. */
    public static HBox clickableStars(int currentRating, IntConsumer onRate) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            int position = i;
            Label star = new Label("★");
            star.setTextFill(Color.web(i <= currentRating ? Theme.GOLD : Theme.STAR_EMPTY));
            star.setStyle("-fx-font-size: 20px;");
            star.setCursor(Cursor.HAND);
            star.setOnMouseClicked(e -> onRate.accept(position));
            row.getChildren().add(star);
        }
        return row;
    }

    public static StackPane noteSquare(double size) {
        Rectangle square = new Rectangle(size, size);
        square.setArcWidth(8);
        square.setArcHeight(8);
        square.setFill(Color.web(Theme.SIDEBAR_BG));
        Label note = new Label("♪");
        note.setTextFill(Color.web(Theme.GOLD));
        note.setStyle("-fx-font-size: " + (size * 0.4) + "px;");
        StackPane pane = new StackPane(square, note);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    public static StackPane noteCircle(double radius) {
        Circle circle = new Circle(radius);
        circle.setFill(Color.web(Theme.SIDEBAR_BG));
        Label note = new Label("♪");
        note.setTextFill(Color.web(Theme.GOLD));
        note.setStyle("-fx-font-size: " + (radius * 0.7) + "px;");
        StackPane pane = new StackPane(circle, note);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefSize(radius * 2, radius * 2);
        return pane;
    }

    public static Label eyebrow(String text) {
        Label label = new Label(text.toUpperCase(Locale.ROOT));
        label.setTextFill(Color.web(Theme.GOLD));
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 0.15em;"
                + " -fx-font-family: '" + Theme.SANS_FONT + "';");
        return label;
    }

    public static String relativeTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        long seconds = Math.max(0, (System.currentTimeMillis() - timestamp.getTime()) / 1000);
        if (seconds < 60) {
            return "just now";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        long days = hours / 24;
        if (days < 7) {
            return days == 1 ? "Yesterday" : days + " days ago";
        }
        long weeks = days / 7;
        return weeks + (weeks == 1 ? " week ago" : " weeks ago");
    }

    public static String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    public static Label labelMuted(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(Theme.MUTED));
        label.setStyle("-fx-font-size: 12px;");
        return label;
    }

    public static VBox bigRating(double rating, int count) {
        Label value = new Label(String.format("%.1f", rating) + "/5");
        value.setTextFill(Color.web(Theme.INK));
        value.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");
        HBox starsRow = stars(rating);
        Label countLabel = labelMuted(count + " rating" + (count == 1 ? "" : "s"));
        VBox box = new VBox(2, value, starsRow, countLabel);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    public static Region divider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: " + Theme.INK + ";");
        return divider;
    }

    public static Region reviewRow(Review review) {
        Label reviewer = new Label(review.getReviewerUsername());
        reviewer.setTextFill(Color.web(Theme.INK));
        reviewer.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        HBox reviewerRow = new HBox(6, reviewer, stars(review.getStarRating()));
        reviewerRow.setAlignment(Pos.CENTER_LEFT);

        VBox row = new VBox(4, reviewerRow);
        if (review.getComment() != null && !review.getComment().isBlank()) {
            Label comment = new Label("“" + review.getComment() + "”");
            comment.setTextFill(Color.web(Theme.INK));
            comment.setStyle("-fx-font-size: 12px;");
            comment.setWrapText(true);
            row.getChildren().add(comment);
        }
        row.setPadding(new Insets(0, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER + " transparent; -fx-border-width: 1;");
        return row;
    }

    /** Small red close button used in the top-right corner of every screen (incl. Login/Register). */
    public static Button closeButton(Stage stage) {
        Button close = new Button("×");
        close.setPrefSize(32, 32);
        close.setStyle("-fx-background-color: " + Theme.DANGER + "; -fx-text-fill: white;"
                + " -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10;");
        close.setOnAction(e -> stage.close());
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        StackPane.setMargin(close, new Insets(20));
        return close;
    }

    /** Styles a text field for use on the cream form background (Login/Register). */
    public static void styleField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-text-fill: " + Theme.INK + ";"
                + " -fx-prompt-text-fill: " + Theme.MUTED + "; -fx-font-size: 13px;"
                + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
    }

    /**
     * The dark brand panel used on the left side of Login/Register, filling the full
     * window height so the screen reads as a desktop split-pane rather than a floating
     * mobile-style card.
     */
    public static Region brandPane(String tagline) {
        Label brand = new Label("Crescendo");
        brand.setTextFill(Color.web(Theme.GOLD_BRIGHT));
        brand.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        StackPane bigNote = noteCircle(64);

        Label taglineLabel = new Label(tagline);
        taglineLabel.setTextFill(Color.web(Theme.SIDEBAR_TEXT));
        taglineLabel.setStyle("-fx-font-size: 14px;");
        taglineLabel.setWrapText(true);
        taglineLabel.setMaxWidth(300);

        VBox content = new VBox(24, bigNote, brand, taglineLabel);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox pane = new VBox(content);
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setPadding(new Insets(60));
        pane.setPrefWidth(420);
        pane.setMinWidth(420);
        pane.setMaxWidth(420);
        pane.setMaxHeight(Double.MAX_VALUE);
        pane.setStyle("-fx-background-color: " + Theme.SIDEBAR_BG + ";");
        return pane;
    }
}
