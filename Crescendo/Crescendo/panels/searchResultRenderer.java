package Crescendo.panels;

import Crescendo.controllers.SearchResult;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class searchResultRenderer extends ListCell<SearchResult> {
    private final BorderPane card = new BorderPane();
    private final Label titleLabel = new Label();
    private final Label infoLabel = new Label();
    private final Label typeLabel = new Label();
    private final Label ratingLabel = new Label();

    public searchResultRenderer() {
        super();

        VBox leftPanel = new VBox(titleLabel, infoLabel);
        leftPanel.setSpacing(5);
        leftPanel.setAlignment(Pos.CENTER_LEFT);

        VBox rightPanel = new VBox(typeLabel, ratingLabel);
        rightPanel.setSpacing(5);
        rightPanel.setAlignment(Pos.CENTER_RIGHT);

        card.setLeft(leftPanel);
        card.setRight(rightPanel);

        card.setStyle(
                "-fx-background-color: #FFFFFF; -fx-padding: 10 10 10 10; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        titleLabel.setStyle(
                "-fx-font-family: 'SansSerif'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        infoLabel.setStyle("-fx-font-family: 'SansSerif'; -fx-font-size: 14px; -fx-text-fill: #555555;");
        typeLabel.setStyle(
                "-fx-font-family: 'SansSerif'; -fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #808080;");
        ratingLabel.setStyle(
                "-fx-font-family: 'SansSerif'; -fx-font-size: 14px; -fx-text-fill: #D4AF37; -fx-font-weight: bold;");

        card.setOnMouseEntered(e -> {
            if (!isSelected()) {
                card.setStyle(
                        "-fx-background-color: #F8F8F8; -fx-padding: 10 10 10 10; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        });
        card.setOnMouseExited(e -> {
            if (!isSelected()) {
                card.setStyle(
                        "-fx-background-color: #FFFFFF; -fx-padding: 10 10 10 10; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");
            }
        });
    }

    @Override
    protected void updateItem(SearchResult item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            setStyle("-fx-background-color: transparent;");
        } else {
            titleLabel.setText(item.getTitle());
            typeLabel.setText(item.getType().toUpperCase());
            infoLabel.setText(item.getAdditionalInfo());

            if (item.getType().equalsIgnoreCase("User") || item.getAverageRating() == 0.0) {
                ratingLabel.setText("");
                ratingLabel.setVisible(false);
                ratingLabel.setManaged(false);
            } else {
                ratingLabel.setText(
                        getStarString(item.getAverageRating()) + "  " + String.format("%.1f", item.getAverageRating()));
                ratingLabel.setVisible(true);
                ratingLabel.setManaged(true);
            }

            updateCellStyles();
            setGraphic(card);
            setText(null);
        }
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        updateCellStyles();
    }

    private void updateCellStyles() {
        if (getItem() != null) {
            if (isSelected()) {
                card.setStyle(
                        "-fx-background-color: #E8E8E8; -fx-padding: 10 10 10 10; -fx-border-color: #D4AF37; -fx-border-width: 0 0 1 0;");
                setStyle("-fx-background-color: #E8E8E8;");
            } else {
                card.setStyle(
                        "-fx-background-color: #FFFFFF; -fx-padding: 10 10 10 10; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");
                setStyle("-fx-background-color: #FFFFFF;");
            }
        } else {
            setStyle("-fx-background-color: transparent;");
        }
    }

    private String getStarString(double rating) {
        int fullStars = (int) Math.round(rating);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}