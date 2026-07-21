package com.crescendo.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.BiConsumer;

/** Generic "click 5 stars + write a review" popup, used to rate an artist, song, or album. */
public final class RateDialog {
    private RateDialog() {
    }

    public static void show(Stage owner, String subjectName, BiConsumer<Integer, String> onSubmit) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Rate " + subjectName);
        dialog.setResizable(false);

        Label title = new Label("Rate " + subjectName);
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        int[] selectedRating = {0};
        Label[] starLabels = new Label[5];
        HBox starsRow = new HBox(8);
        starsRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            int position = i + 1;
            Label star = new Label("★");
            star.setStyle("-fx-font-size: 30px;");
            star.setTextFill(Color.web(Theme.STAR_EMPTY));
            star.setCursor(Cursor.HAND);
            star.setOnMouseClicked(e -> {
                selectedRating[0] = position;
                updateStars(starLabels, position);
            });
            starLabels[i] = star;
            starsRow.getChildren().add(star);
        }

        Label starsHint = new Label("Click a star to rate");
        starsHint.setTextFill(Color.web(Theme.MUTED));
        starsHint.setStyle("-fx-font-size: 11px;");

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your review of " + subjectName + " (optional)...");
        reviewArea.setPrefRowCount(4);
        reviewArea.setWrapText(true);
        reviewArea.setStyle("-fx-font-size: 13px;");

        Button submitButton = new Button("Submit Rating");
        submitButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E;"
                + " -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 18 8 18;");
        submitButton.setOnAction(e -> {
            if (selectedRating[0] == 0) {
                new Alert(Alert.AlertType.WARNING, "Pick a star rating before submitting.").showAndWait();
                return;
            }
            try {
                onSubmit.accept(selectedRating[0], reviewArea.getText());
                dialog.close();
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });

        VBox root = new VBox(14, title, starsRow, starsHint, reviewArea, submitButton);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Theme.BG + ";");
        root.setAlignment(Pos.TOP_LEFT);

        dialog.setScene(new Scene(root, 360, 340));
        dialog.showAndWait();
    }

    private static void updateStars(Label[] labels, int rating) {
        for (int i = 0; i < labels.length; i++) {
            labels[i].setTextFill(Color.web(i < rating ? Theme.GOLD : Theme.STAR_EMPTY));
        }
    }
}
