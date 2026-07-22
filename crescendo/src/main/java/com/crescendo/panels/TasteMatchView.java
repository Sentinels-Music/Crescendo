package com.crescendo.panels;

import com.crescendo.controller.AuthController;
import com.crescendo.model.User;
import com.crescendo.model.VerifiedUser;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Owned by Emir Selim Kayhan (Artist, Tag & Streaming) as its own screen, backed by Ege
 * Yiğit Yıldırım's taste-matching JOIN/aggregation work (Search, Database & Integration)
 * and Metehan Karadeniz's calculateTasteSimilarity() (User & Authentication) - the Taste
 * Matching Page.
 */
public final class TasteMatchView {
    private TasteMatchView() {
    }

    private record Ranked(User user, int percent, List<String> sharedTags, boolean following) {
    }

    public static Parent build(Nav nav, AuthController authController, Runnable onRefresh) {
        User me = nav.currentUser;

        Label eyebrow = Widgets.eyebrow("Taste Matching");
        Label heading = new Label("People Like You");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");

        Label explainer = new Label("Ranked by the percentage of your tags shared with each user."
                + " Your tags are determined by the artists you follow.");
        explainer.setTextFill(Color.web(Theme.MUTED));
        explainer.setStyle("-fx-font-size: 12px;");
        explainer.setWrapText(true);
        explainer.setMaxWidth(560);

        List<User> others = authController.getAllOtherUsers(me);
        List<User> followed = authController.getFollowedUsers(me);

        List<Ranked> ranked = new ArrayList<>();
        for (User other : others) {
            int percent = authController.computeTasteMatch(me, other);
            List<String> sharedTags = authController.getSharedTags(me, other);
            boolean following = followed.contains(other);
            ranked.add(new Ranked(other, percent, sharedTags, following));
        }
        ranked.sort(new RankedComparator());

        VBox list = new VBox(12);
        if (ranked.isEmpty()) {
            list.getChildren().add(Widgets.labelMuted("No other Crescendo users yet."));
        }
        for (Ranked r : ranked) {
            list.getChildren().add(matchRow(authController, me, r, onRefresh));
        }

        VBox content = new VBox(20, eyebrow, heading, explainer, list);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return Chrome.build(nav, "Taste Matching", scroll, "Crescendo · Taste Matching");
    }

    private static Region matchRow(AuthController authController, User me, Ranked ranked, Runnable onRefresh) {
        User other = ranked.user();

        StackPane avatar = new StackPane();
        Circle circle = new Circle(24, Color.web(Theme.SIDEBAR_BG));
        Label initial = new Label(other.getUsername().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
        initial.setTextFill(Color.web(Theme.GOLD));
        initial.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        avatar.getChildren().addAll(circle, initial);

        HBox nameRow = new HBox(6);
        Label name = new Label(other.getUsername());
        name.setTextFill(Color.web(Theme.INK));
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nameRow.getChildren().add(name);
        if (other instanceof VerifiedUser) {
            Label badge = new Label("★ Verified");
            badge.setTextFill(Color.web(Theme.GOLD));
            badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            nameRow.getChildren().add(badge);
        }

        List<String> sharedTags = ranked.sharedTags();
        Label sharedLabel = Widgets.labelMuted(buildSharedTagsText(sharedTags));
        sharedLabel.setWrapText(true);
        sharedLabel.setMaxWidth(320);

        VBox textBlock = new VBox(3, nameRow, sharedLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox percentBlock = matchBar(ranked.percent());

        boolean following = ranked.following();
        Button toggle = new Button();
        if (following) {
            toggle.setText("Following");
            toggle.setStyle("-fx-background-color: #EDE6D6; -fx-text-fill: " + Theme.MUTED
                    + "; -fx-font-size: 11px; -fx-background-radius: 10; -fx-padding: 5 12 5 12;");
        } else {
            toggle.setText("Follow");
            toggle.setStyle("-fx-background-color: " + Theme.GOLD
                    + "; -fx-text-fill: #11110E; -fx-font-size: 11px; -fx-font-weight: bold;"
                    + " -fx-background-radius: 10; -fx-padding: 5 12 5 12;");
        }
        toggle.setOnAction(new FollowToggleHandler(authController, me, other, following, onRefresh));

        HBox row = new HBox(16, avatar, textBlock, spacer, percentBlock, toggle);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 8, 12, 8));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 12;");
        return row;
    }

    private static String buildSharedTagsText(List<String> sharedTags) {
        if (sharedTags.isEmpty()) {
            return "No shared tags yet";
        }

        List<String> visibleTags = new ArrayList<>();
        int visibleCount = Math.min(3, sharedTags.size());
        for (int i = 0; i < visibleCount; i++) {
            visibleTags.add(sharedTags.get(i));
        }

        String text = "Shared tags: " + String.join(", ", visibleTags);
        if (sharedTags.size() > 3) {
            text += " +" + (sharedTags.size() - 3) + " more";
        }
        return text;
    }

    private static final class RankedComparator implements Comparator<Ranked> {
        @Override
        public int compare(Ranked first, Ranked second) {
            return Integer.compare(second.percent(), first.percent());
        }
    }

    private static final class FollowToggleHandler implements EventHandler<ActionEvent> {
        private final AuthController authController;
        private final User currentUser;
        private final User otherUser;
        private final boolean following;
        private final Runnable onRefresh;

        private FollowToggleHandler(AuthController authController, User currentUser, User otherUser,
                                    boolean following, Runnable onRefresh) {
            this.authController = authController;
            this.currentUser = currentUser;
            this.otherUser = otherUser;
            this.following = following;
            this.onRefresh = onRefresh;
        }

        @Override
        public void handle(ActionEvent event) {
            if (following) {
                authController.unfollowUser(currentUser, otherUser);
            } else {
                authController.followUser(currentUser, otherUser);
            }
            onRefresh.run();
        }
    }

    private static VBox matchBar(int percent) {
        Label percentLabel = new Label(percent + "%");
        percentLabel.setTextFill(Color.web(Theme.INK));
        percentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Region track = new Region();
        track.setPrefSize(90, 6);
        track.setStyle("-fx-background-color: " + Theme.STAR_EMPTY + "; -fx-background-radius: 3;");

        Region fill = new Region();
        fill.setPrefSize(Math.max(2, 90 * percent / 100.0), 6);
        fill.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-background-radius: 3;");

        StackPane bar = new StackPane(track, fill);
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(4, percentLabel, bar);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }
}
