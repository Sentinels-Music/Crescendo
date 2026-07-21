package views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import controllers.CrescendoGetterSetters;
import controllers.ReviewController;
import controllers.SearchResult;
import controllers.Session;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Album;
import model.Review;
import model.User;
import panels.DiscoverPage;
import panels.searchResultRenderer;

/**
 * Controller for the Home page.
 *
 * Loads the active user from Session, generates the review feed through
 * ReviewController, handles search, and provides sidebar navigation.
 */
public class HomeController {

    private static final int FIXED_HEADER_COUNT = 2;

    @FXML
    private Label sidebarName;

    @FXML
    private Label sidebarVerified;

    @FXML
    private Label avatarLabel;

    @FXML
    private TextField searchField;

    @FXML
    private VBox feedContainer;

    private final ReviewController reviewController = new ReviewController();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = Session.getCurrentUser();

        populateSidebar();
        populateFeed();
    }

    private void populateSidebar() {
        if (currentUser == null) {
            sidebarName.setText("Not logged in");
            avatarLabel.setText("?");

            sidebarVerified.setVisible(false);
            sidebarVerified.setManaged(false);
            return;
        }

        String username = currentUser.getUsername();

        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        sidebarName.setText(username);
        avatarLabel.setText(username.substring(0, 1).toUpperCase());

        boolean verified = currentUser.isVerified();
        sidebarVerified.setVisible(verified);
        sidebarVerified.setManaged(verified);
    }

    private void populateFeed() {
        removeExistingFeedItems();

        if (currentUser == null) {
            feedContainer.getChildren().add(
                    createEmptyMessage("Log in to see reviews from people you follow."));
            return;
        }

        List<Review> reviews = reviewController.generateFeed(currentUser.getUserId());

        if (reviews.isEmpty()) {
            feedContainer.getChildren().add(
                    createEmptyMessage("No reviews from people you follow yet."));
            return;
        }

        for (int i = 0; i < reviews.size(); i++) {
            feedContainer.getChildren().add(createReviewCard(reviews.get(i)));

            if (i < reviews.size() - 1) {
                feedContainer.getChildren().add(createSeparator());
            }
        }
    }

    private void removeExistingFeedItems() {
        while (feedContainer.getChildren().size() > FIXED_HEADER_COUNT) {
            feedContainer.getChildren().remove(FIXED_HEADER_COUNT);
        }
    }

    private HBox createReviewCard(Review review) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(15, 0, 15, 0));

        StackPane thumbnail = new StackPane();
        thumbnail.setPrefSize(58, 58);
        thumbnail.setMinSize(58, 58);
        thumbnail.setStyle(
                "-fx-background-color: #171713; "
                        + "-fx-background-radius: 3;");

        Label note = new Label("♪");
        note.setStyle(
                "-fx-text-fill: #D4A12A; "
                        + "-fx-font-size: 25px; "
                        + "-fx-font-weight: bold;");
        thumbnail.getChildren().add(note);

        VBox details = new VBox(3);
        HBox.setHgrow(details, Priority.ALWAYS);

        User author = review.getAuthor();

        HBox actionRow = new HBox(4);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Label username = new Label(author.getUsername());
        username.setStyle(
                "-fx-font-size: 13px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-text-fill: #171713;");

        String actionText = review.getTargetItem() instanceof Album
                ? "reviewed an album"
                : "rated a song";

        Label action = new Label(actionText);
        action.setStyle(
                "-fx-font-size: 13px; "
                        + "-fx-text-fill: #77736B;");

        actionRow.getChildren().addAll(username, action);

        if (author.isVerified()) {
            Label verified = new Label("· Verified");
            verified.setStyle(
                    "-fx-font-size: 12px; "
                            + "-fx-font-weight: bold; "
                            + "-fx-text-fill: #B8892A;");
            actionRow.getChildren().add(verified);
        }

        Label title = new Label(review.getTargetItem().getTitle());
        title.setStyle(
                "-fx-font-size: 16px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-text-fill: #171713;");

        HBox ratingRow = new HBox(8);
        ratingRow.setAlignment(Pos.CENTER_LEFT);

        Label stars = new Label(createStarString(review.getStarRating()));
        stars.setStyle(
                "-fx-text-fill: #C18E22; "
                        + "-fx-font-size: 15px; "
                        + "-fx-font-weight: bold;");

        Label rating = new Label(String.format("%.1f", (double) review.getStarRating()));
        rating.setStyle(
                "-fx-text-fill: #6F6B63; "
                        + "-fx-font-size: 13px;");

        ratingRow.getChildren().addAll(stars, rating);

        details.getChildren().addAll(actionRow, title, ratingRow);

        String commentText = review.getComment();

        if (commentText != null && !commentText.trim().isEmpty()) {
            Label comment = new Label("\"" + commentText.trim() + "\"");
            comment.setWrapText(true);
            comment.setMaxWidth(520);
            comment.setStyle(
                    "-fx-font-size: 12px; "
                            + "-fx-text-fill: #66625B;");
            details.getChildren().add(comment);
        }

        Label date = new Label(formatRelativeDate(review.getCreatedAt()));
        date.setStyle(
                "-fx-font-size: 11px; "
                        + "-fx-text-fill: #AAA59B;");
        details.getChildren().add(date);

        card.getChildren().addAll(thumbnail, details);
        return card;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setStyle("-fx-background-color: #E5DED2;");
        return separator;
    }

    private Label createEmptyMessage(String text) {
        Label message = new Label(text);
        message.setWrapText(true);
        message.setPadding(new Insets(18, 0, 0, 0));
        message.setStyle(
                "-fx-font-size: 13px; "
                        + "-fx-text-fill: #8A8A8A;");
        return message;
    }

    private String createStarString(int rating) {
        StringBuilder stars = new StringBuilder();

        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }

        return stars.toString();
    }

    private String formatRelativeDate(Date createdAt) {
        if (createdAt == null) {
            return "";
        }

        long difference = Math.max(0, System.currentTimeMillis() - createdAt.getTime());
        long minutes = difference / 60_000;
        long hours = difference / 3_600_000;
        long days = difference / 86_400_000;

        if (minutes < 1) {
            return "Just now";
        }

        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        return new SimpleDateFormat("MMM d, yyyy").format(createdAt);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            return;
        }

        List<SearchResult> results =
                CrescendoGetterSetters.executeSearch(query, "All");

        ListView<SearchResult> resultList = new ListView<>();
        resultList.getItems().addAll(results);
        resultList.setCellFactory(listView -> new searchResultRenderer());
        resultList.setPlaceholder(new Label("No results found."));
        resultList.setPrefSize(520, 360);

        Label title = new Label("Search results for \"" + query + "\"");
        title.setStyle(
                "-fx-font-size: 19px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-text-fill: #171713;");

        VBox root = new VBox(14, title, resultList);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F6F1E8;");

        Stage resultStage = new Stage();
        resultStage.initOwner(stage());
        resultStage.initModality(Modality.WINDOW_MODAL);
        resultStage.setTitle("Crescendo · Search");
        resultStage.setScene(new Scene(root, 560, 420));
        resultStage.show();
    }

    @FXML
    private void goDiscover() {
        new DiscoverPage().showIn(stage());
    }

    @FXML
    private void goListenLater() {
        SceneNavigator.switchTo(stage(), "ListenLaterPage.fxml");
    }

    @FXML
    private void goProfile() {
        SceneNavigator.switchTo(stage(), "ProfileView.fxml");
    }

    @FXML
    private void closeWindow() {
        stage().close();
    }

    private Stage stage() {
        return (Stage) searchField.getScene().getWindow();
    }
}
