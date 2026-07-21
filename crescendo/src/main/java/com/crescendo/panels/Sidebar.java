package com.crescendo.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/** The dark left navigation rail shared by every logged-in screen. */
public class Sidebar extends AnchorPane {

    public Sidebar(Nav nav, String activeItem) {
        setPrefWidth(242);
        setMinWidth(242);
        setStyle("-fx-background-color: " + Theme.SIDEBAR_BG + "; -fx-background-radius: 0 10 10 0;");

        Label brand = new Label("Crescendo");
        brand.setTextFill(Color.web(Theme.GOLD_BRIGHT));
        brand.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");
        AnchorPane.setTopAnchor(brand, 30.0);
        AnchorPane.setLeftAnchor(brand, 20.0);
        enableWindowDrag(brand, nav.stage);

        VBox nav1 = new VBox(14,
                navButton("Home", activeItem, nav.toHome),
                navButton("Discover", activeItem, nav.toDiscover),
                navButton("Listen Later", activeItem, nav.toListenLater),
                navButton("Taste Matching", activeItem, nav.toTasteMatch));
        nav1.setPrefWidth(202);
        AnchorPane.setTopAnchor(nav1, 96.0);
        AnchorPane.setLeftAnchor(nav1, 20.0);

        HBox userChip = buildUserChip(nav.currentUser.getUsername(), nav.verified, nav.toProfile);
        AnchorPane.setLeftAnchor(userChip, 20.0);
        AnchorPane.setBottomAnchor(userChip, 24.0);

        getChildren().addAll(brand, nav1, userChip);
    }

    private Button navButton(String label, String activeItem, Runnable action) {
        boolean active = label.equals(activeItem);
        Button button = new Button(label);
        button.setPrefWidth(202);
        button.setPrefHeight(active ? 50 : 46);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(active
                ? "-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E;"
                        + " -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 0 0 0 18;"
                : "-fx-background-color: transparent; -fx-text-fill: " + Theme.SIDEBAR_TEXT
                        + "; -fx-font-size: 15px; -fx-padding: 0 0 0 18;");
        if (action != null && !active) {
            button.setOnAction(e -> action.run());
        } else if (action == null) {
            button.setOpacity(0.5);
            button.setDisable(true);
        }
        return button;
    }

    private HBox buildUserChip(String username, boolean verified, Runnable onProfile) {
        Circle avatar = new Circle(21, Color.web("#8B8B83"));

        VBox names = new VBox(2);
        Label nameLabel = new Label(username);
        nameLabel.setTextFill(Color.web(Theme.BG));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        names.getChildren().add(nameLabel);
        if (verified) {
            Label verifiedLabel = new Label("Verified");
            verifiedLabel.setTextFill(Color.web(Theme.GOLD));
            verifiedLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            names.getChildren().add(verifiedLabel);
        }

        HBox chip = new HBox(12, avatar, names);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPrefWidth(202);
        chip.setPrefHeight(62);
        chip.setPadding(new Insets(10, 12, 10, 12));
        chip.setStyle("-fx-background-color: " + Theme.SIDEBAR_ALT + "; -fx-background-radius: 18; -fx-cursor: hand;");
        chip.setOnMouseClicked(e -> onProfile.run());
        return chip;
    }

    private void enableWindowDrag(Label brand, Stage stage) {
        final double[] offset = new double[2];
        brand.setOnMousePressed(e -> {
            offset[0] = stage.getX() - e.getScreenX();
            offset[1] = stage.getY() - e.getScreenY();
        });
        brand.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() + offset[0]);
            stage.setY(e.getScreenY() + offset[1]);
        });
    }
}
