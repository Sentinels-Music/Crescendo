package com.crescendo.panels;

import com.crescendo.controller.AuthController;
import com.crescendo.controller.FeedController;
import com.crescendo.db.Database;
import com.crescendo.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Locale;

/** Owned by Metehan Karadeniz (User & Authentication) - the Profile Page. */
public final class ProfileView {
    private ProfileView() {
    }

    public static Parent build(Nav nav, AuthController authController, FeedController feedController,
                                Runnable onRefresh) {
        User user = nav.currentUser;

        StackPane avatar = new StackPane();
        Circle circle = new Circle(38, Color.web(Theme.GOLD));
        Label initial = new Label(user.getUsername().substring(0, 1).toUpperCase(Locale.ROOT));
        initial.setTextFill(Color.web("#11110E"));
        initial.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        avatar.getChildren().addAll(circle, initial);

        Label name = new Label(user.getUsername());
        name.setTextFill(Color.web(Theme.INK));
        name.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox nameRow = new HBox(10, name);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        if (nav.verified) {
            Label badge = new Label("★ Verified");
            badge.setTextFill(Color.web(Theme.GOLD));
            badge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
            nameRow.getChildren().add(badge);
        }

        Label bio = new Label(user.getBio() == null || user.getBio().isBlank() ? "No bio yet." : user.getBio());
        bio.setTextFill(Color.web(Theme.MUTED));
        bio.setStyle("-fx-font-size: 13px;");
        bio.setWrapText(true);

        int followingCount = authController.getFollowedUsers(user).size();
        int followerCount = authController.getFollowerCount(user);
        HBox statsRow = new HBox(28,
                profileStat(String.valueOf(user.getReviewCount()), "REVIEWS"),
                profileStat(String.valueOf(followingCount), "FOLLOWING"),
                profileStat(String.valueOf(followerCount), "FOLLOWERS",
                        () -> showFollowersDialog(nav, authController, user, onRefresh)));

        Button editButton = new Button("Edit profile");
        editButton.setStyle("-fx-background-color: " + Theme.INK + "; -fx-text-fill: white;"
                + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 14 8 14;");
        editButton.setOnAction(e -> showEditBioDialog(nav, authController, user, onRefresh));

        Button logoutButton = new Button("Log out");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.DANGER + "; -fx-font-size: 12px;");
        logoutButton.setOnAction(e -> nav.toLogout.run());

        VBox headerText = new VBox(6, nameRow, bio, statsRow);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(20, avatar, headerText, spacer, new VBox(8, editButton, logoutButton));
        header.setAlignment(Pos.CENTER_LEFT);

        VBox reviewsColumn = new VBox(14, Widgets.eyebrow("My Reviews"));
        List<Database.FeedEntry> myReviews = feedController.getReviewsByUser(user);
        if (myReviews.isEmpty()) {
            reviewsColumn.getChildren().add(Widgets.labelMuted("You haven't reviewed anything yet."));
        }
        for (Database.FeedEntry entry : myReviews) {
            reviewsColumn.getChildren().add(myReviewRow(entry));
        }

        VBox content = new VBox(24, header, Widgets.divider(), reviewsColumn);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return Chrome.build(nav, "", scroll, "Crescendo · Profile");
    }

    private static VBox profileStat(String value, String label) {
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(Theme.INK));
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label captionLabel = Widgets.labelMuted(label);
        return new VBox(2, valueLabel, captionLabel);
    }

    /** A stat block that opens something when clicked (used by FOLLOWERS to show who follows you). */
    private static VBox profileStat(String value, String label, Runnable onClick) {
        VBox box = profileStat(value, label);
        box.setStyle("-fx-cursor: hand;");
        box.setOnMouseClicked(e -> onClick.run());
        return box;
    }

    private static Region myReviewRow(Database.FeedEntry entry) {
        Label target = new Label(entry.targetLabel() == null ? "(removed)" : entry.targetLabel());
        target.setTextFill(Color.web(Theme.INK));
        target.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        VBox row = new VBox(4, target, Widgets.stars(entry.starRating()));
        if (entry.comment() != null && !entry.comment().isBlank()) {
            Label comment = Widgets.labelMuted(entry.comment());
            comment.setWrapText(true);
            row.getChildren().add(comment);
        }
        row.setPadding(new Insets(0, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER + " transparent; -fx-border-width: 1;");
        return row;
    }

    private static Region followingRow(AuthController authController, User me, User other,
                                        Runnable onRefresh, boolean following) {
        HBox avatarBox = new HBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefSize(36, 36);
        avatarBox.setStyle("-fx-background-color: " + Theme.SIDEBAR_BG + "; -fx-background-radius: 18;");
        Label initial = new Label(other.getUsername().substring(0, 1).toUpperCase(Locale.ROOT));
        initial.setTextFill(Color.web(Theme.GOLD));
        initial.setStyle("-fx-font-weight: bold;");
        avatarBox.getChildren().add(initial);

        Label name = new Label(other.getUsername());
        name.setTextFill(Color.web(Theme.INK));
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        VBox textBlock = new VBox(2, name);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button toggle = new Button(following ? "Following" : "Follow");
        toggle.setStyle(following
                ? "-fx-background-color: #EDE6D6; -fx-text-fill: " + Theme.MUTED + "; -fx-font-size: 11px; -fx-background-radius: 10; -fx-padding: 4 12 4 12;"
                : "-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 4 12 4 12;");
        toggle.setOnAction(e -> {
            if (following) {
                authController.unfollowUser(me, other);
            } else {
                authController.followUser(me, other);
            }
            onRefresh.run();
        });

        HBox row = new HBox(10, avatarBox, textBlock, spacer, toggle);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 4, 8, 4));
        return row;
    }

    private static void showFollowersDialog(Nav nav, AuthController authController, User user, Runnable onRefresh) {
        Stage dialog = new Stage();
        dialog.initOwner(nav.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Followers");

        List<User> followers = authController.getFollowers(user);

        VBox list = new VBox(4);
        if (followers.isEmpty()) {
            list.getChildren().add(Widgets.labelMuted("No one follows you yet."));
        }
        for (User follower : followers) {
            boolean followingBack = authController.isFollowingUser(user, follower);
            list.getChildren().add(followingRow(authController, user, follower, () -> {
                onRefresh.run();
                showFollowersDialog(nav, authController, user, onRefresh);
            }, followingBack));
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox root = new VBox(12, Widgets.eyebrow("Followers"), scroll);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Theme.BG + ";");
        dialog.setScene(new Scene(root, 340, 400));
        dialog.showAndWait();
    }

    private static void showEditBioDialog(Nav nav, AuthController authController, User user, Runnable onRefresh) {
        Stage dialog = new Stage();
        dialog.initOwner(nav.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Edit Profile");

        TextArea bioArea = new TextArea(user.getBio());
        bioArea.setPromptText("Tell people what you're into...");
        bioArea.setPrefRowCount(4);
        bioArea.setWrapText(true);

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16;");
        saveButton.setOnAction(e -> {
            authController.updateBio(user, bioArea.getText());
            dialog.close();
            onRefresh.run();
        });

        VBox root = new VBox(12, Widgets.eyebrow("Bio"), bioArea, saveButton);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Theme.BG + ";");
        dialog.setScene(new Scene(root, 340, 260));
        dialog.showAndWait();
    }
}
