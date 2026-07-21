package com.crescendo.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/** Sidebar + top search bar + footer caption around a screen's content. */
public final class Chrome {
    private Chrome() {
    }

    public static BorderPane build(Nav nav, String activeNav, Node content, String footerText) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Theme.BG + ";");
        root.setPrefSize(1080, 720);
        root.setMinSize(900, 620);

        Sidebar sidebar = new Sidebar(nav, activeNav);
        root.setLeft(sidebar);

        BorderPane centerArea = new BorderPane();
        centerArea.setPadding(new Insets(24, 36, 12, 36));
        centerArea.setTop(buildTopBar(nav));
        BorderPane.setMargin(content, new Insets(20, 0, 0, 0));
        centerArea.setCenter(content);
        centerArea.setBottom(buildFooter(footerText));

        root.setCenter(centerArea);
        return root;
    }

    private static HBox buildTopBar(Nav nav) {
        Button back = new Button("‹ Back");
        back.setVisible(nav.canGoBack);
        back.setManaged(nav.canGoBack);
        back.setCursor(Cursor.HAND);
        back.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.INK + ";"
                + " -fx-font-size: 13px; -fx-font-weight: bold;");
        back.setOnAction(e -> nav.toBack.run());

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 14, 0, 14));
        searchBox.setPrefHeight(42);
        searchBox.setMaxWidth(320);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 18;"
                + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 18;");

        Label magnifier = new Label("⌕");
        magnifier.setTextFill(Color.web("#737373"));
        magnifier.setCursor(Cursor.HAND);

        TextField search = new TextField();
        search.setPromptText("Search artists, albums and songs...");
        search.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(search, Priority.ALWAYS);
        Runnable submit = () -> {
            if (!search.getText().isBlank()) {
                nav.toSearch.accept(search.getText());
            }
        };
        search.setOnAction(e -> submit.run());
        magnifier.setOnMouseClicked(e -> submit.run());

        searchBox.getChildren().addAll(magnifier, search);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("×");
        close.setPrefSize(32, 32);
        close.setStyle("-fx-background-color: " + Theme.DANGER + "; -fx-text-fill: white;"
                + " -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10;");
        close.setOnAction(e -> nav.stage.close());

        HBox bar = new HBox(12, back, spacer, searchBox, close);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private static HBox buildFooter(String footerText) {
        Label footer = new Label(footerText);
        footer.setTextFill(Color.web(Theme.MUTED));
        footer.setStyle("-fx-font-size: 11px;");
        HBox bar = new HBox(footer);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(10, 8, 0, 0));
        return bar;
    }
}
