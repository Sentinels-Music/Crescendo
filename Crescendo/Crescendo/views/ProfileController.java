package views;

import java.util.List;
import java.util.Optional;

import controllers.CrescendoGetterSetters;
import controllers.ReviewSummary;
import controllers.Session;
import controllers.UserController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Profile;
import model.User;

/**
 * Controller for the Profile page. Reads the logged-in user from
 * {@link Session}, fills in the header and stats, and builds the
 * "My Reviews" and "Following" lists from the database via
 * {@link UserController}. Taste-match percentages reuse the team facade
 * {@link CrescendoGetterSetters#getTasteMatch}.
 */
public class ProfileController {

    @FXML
    private Label sidebarName;
    @FXML
    private Label sidebarVerified;
    @FXML
    private Label avatarLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label verifiedBadge;
    @FXML
    private Label bioLabel;
    @FXML
    private Label reviewsCount;
    @FXML
    private Label followingCount;
    @FXML
    private Label followersCount;
    @FXML
    private Button editButton;
    @FXML
    private VBox reviewsBox;
    @FXML
    private VBox followingBox;

    private final UserController userController = new UserController();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            usernameLabel.setText("Not logged in");
            verifiedBadge.setVisible(false);
            editButton.setDisable(true);
            return;
        }
        populateHeader();
        populateReviews();
        populateFollowing();
    }

    // ---- header + stats ----
    private void populateHeader() {
        String name = currentUser.getUsername();
        usernameLabel.setText(name);
        sidebarName.setText(name);
        avatarLabel.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());

        boolean verified = currentUser.isVerified();
        verifiedBadge.setVisible(verified);
        sidebarVerified.setVisible(verified);

        Profile p = currentUser.getProfile();
        if (p != null) {
            bioLabel.setText(p.getBio());
            reviewsCount.setText(String.valueOf(p.getReviewCount()));
            followingCount.setText(String.valueOf(p.getFollowingCount()));
            followersCount.setText(String.valueOf(p.getFollowerCount()));
        }
    }

    // ---- my reviews ----
    private void populateReviews() {
        reviewsBox.getChildren().clear();
        List<ReviewSummary> reviews = userController.getReviewsByUser(currentUser.getUserId());
        if (reviews.isEmpty()) {
            reviewsBox.getChildren().add(mutedLabel("No reviews yet."));
            return;
        }
        for (ReviewSummary r : reviews) {
            reviewsBox.getChildren().add(reviewCard(r));
        }
    }

    private VBox reviewCard(ReviewSummary r) {
        Label title = new Label(r.getItemTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        Label stars = new Label(starString(r.getStars()));
        stars.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 14px;");

        Label comment = new Label(r.getComment() == null ? "" : r.getComment());
        comment.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        comment.setWrapText(true);

        VBox card = new VBox(3, title, stars, comment);
        return card;
    }

    // ---- following (with taste match %) ----
    private void populateFollowing() {
        followingBox.getChildren().clear();
        List<User> followed = userController.getFollowedUsers(currentUser.getUserId());
        if (followed.isEmpty()) {
            followingBox.getChildren().add(mutedLabel("Not following anyone yet."));
            return;
        }
        for (User u : followed) {
            int pct = CrescendoGetterSetters.getTasteMatch(
                    currentUser.getUserId(), u.getUserId());
            followingBox.getChildren().add(followingCard(u, pct));
        }
    }

    private HBox followingCard(User user, int tasteMatchPct) {
        Label name = new Label(user.getUsername());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        Label match = new Label(tasteMatchPct + "% taste match");
        match.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        VBox text = new VBox(2, name, match);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button followBtn = new Button("Following");
        followBtn.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-background-radius: 14; -fx-padding: 5 12 5 12; -fx-cursor: hand;");
        followBtn.setOnAction(e -> toggleFollow(user, followBtn));

        HBox card = new HBox(10, text, spacer, followBtn);
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    private void toggleFollow(User target, Button button) {
        boolean nowFollowing = "Following".equals(button.getText());
        if (nowFollowing) {
            if (userController.unfollowUser(currentUser.getUserId(), target.getUserId())) {
                button.setText("Follow");
            }
        } else {
            if (userController.followUser(currentUser.getUserId(), target.getUserId())) {
                button.setText("Following");
            }
        }
        refreshCounts();
    }

    // ---- edit profile ----
    @FXML
    private void handleEditProfile() {
        if (currentUser == null)
            return;
        String current = currentUser.getProfile() != null ? currentUser.getProfile().getBio() : "";

        TextInputDialog dialog = new TextInputDialog(current);
        dialog.setTitle("Edit profile");
        dialog.setHeaderText("Update your bio");
        dialog.setContentText("Bio:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newBio -> {
            if (userController.updateBio(currentUser.getUserId(), newBio)) {
                currentUser.getProfile().updateBio(newBio);
                bioLabel.setText(newBio);
            }
        });
    }

    private void refreshCounts() {
        User reloaded = userController.loadUser(currentUser.getUserId());
        if (reloaded != null && reloaded.getProfile() != null) {
            currentUser.setProfile(reloaded.getProfile());
            followingCount.setText(String.valueOf(reloaded.getProfile().getFollowingCount()));
            followersCount.setText(String.valueOf(reloaded.getProfile().getFollowerCount()));
        }
    }

    // ---- sidebar navigation ----
    @FXML
    private void goHome() {
        /* wire to Home screen */ }

    @FXML
    private void goDiscover() {
        /* wire to DiscoverPage */ }

    @FXML
    private void goListenLater() {
        /* wire to Listen Later */ }

    // ---- small helpers ----
    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        return l;
    }

    private String starString(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "★" : "☆");
        }
        return sb.toString();
    }
}
