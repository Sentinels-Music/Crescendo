package com.crescendo.panels;

import com.crescendo.controller.ArtistController;
import com.crescendo.controller.SearchController;
import com.crescendo.db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/** Owned by Ege Yiğit Yıldırım (Search, Database & Integration) - the Search Page. */
public final class SearchView {
    private SearchView() {
    }

    public static Parent build(Nav nav, SearchController searchController, ArtistController artistController,
                                String initialQuery) {
        Label eyebrow = Widgets.eyebrow("Search");
        Label heading = new Label("Find anything");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");

        TextField queryField = new TextField(initialQuery == null ? "" : initialQuery);
        queryField.setPromptText("Search artists, albums and songs...");
        queryField.setPrefHeight(46);
        queryField.setMaxWidth(Double.MAX_VALUE);
        queryField.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.INK + ";"
                + " -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px; -fx-padding: 0 14 0 14;");

        String[] activeFilter = {"All"};
        VBox resultsBox = new VBox(0);
        FlowPane filterRow = new FlowPane(8, 8);

        Runnable[] refresh = new Runnable[1];
        refresh[0] = () -> renderResults(nav, searchController, artistController, resultsBox, queryField.getText(), activeFilter[0]);

        for (String type : List.of("All", "Artists", "Albums", "Songs")) {
            filterRow.getChildren().add(filterChip(type, type, activeFilter, filterRow, refresh));
        }

        queryField.textProperty().addListener((obs, oldV, newV) -> refresh[0].run());
        refresh[0].run();

        VBox content = new VBox(16, eyebrow, heading, queryField, filterRow,
                padTop(Widgets.eyebrow("Results"), 8), resultsBox);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return Chrome.build(nav, "Discover", scroll, "Crescendo · Search");
    }

    private static Node padTop(Node node, double top) {
        VBox.setMargin(node, new Insets(top, 0, 0, 0));
        return node;
    }

    private static Button filterChip(String label, String type, String[] activeFilter, FlowPane row, Runnable[] refresh) {
        Button chip = new Button(label);
        chip.setOnAction(e -> {
            activeFilter[0] = type;
            for (Node n : row.getChildren()) {
                if (n instanceof Button b) {
                    boolean active = b.getText().equals(activeFilter[0]);
                    b.setStyle(active
                            ? "-fx-background-color: " + Theme.INK + "; -fx-text-fill: white;"
                                    + " -fx-background-radius: 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14 6 14;"
                            : "-fx-background-color: #EDE6D6; -fx-text-fill: " + Theme.MUTED + ";"
                                    + " -fx-background-radius: 14; -fx-font-size: 12px; -fx-padding: 6 14 6 14;");
                }
            }
            refresh[0].run();
        });
        boolean active = type.equals(activeFilter[0]);
        chip.setStyle(active
                ? "-fx-background-color: " + Theme.INK + "; -fx-text-fill: white;"
                        + " -fx-background-radius: 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14 6 14;"
                : "-fx-background-color: #EDE6D6; -fx-text-fill: " + Theme.MUTED + ";"
                        + " -fx-background-radius: 14; -fx-font-size: 12px; -fx-padding: 6 14 6 14;");
        return chip;
    }

    private static void renderResults(Nav nav, SearchController searchController, ArtistController artistController,
                                       VBox resultsBox, String query, String filter) {
        resultsBox.getChildren().clear();
        if (query == null || query.isBlank()) {
            resultsBox.getChildren().add(Widgets.labelMuted("Type something to search the catalog."));
            return;
        }
        List<Database.SearchResult> results = searchController.searchMusicItem(query);
        boolean any = false;
        for (Database.SearchResult result : results) {
            if (!matchesFilter(filter, result.type())) {
                continue;
            }
            resultsBox.getChildren().add(resultRow(nav, artistController, result));
            any = true;
        }
        if (!any) {
            resultsBox.getChildren().add(Widgets.labelMuted("No matches for \"" + query + "\"."));
        }
    }

    private static boolean matchesFilter(String filter, String resultType) {
        return switch (filter) {
            case "Artists" -> resultType.equals("ARTIST");
            case "Albums" -> resultType.equals("ALBUM");
            case "Songs" -> resultType.equals("SONG");
            default -> true;
        };
    }

    private static Region resultRow(Nav nav, ArtistController artistController, Database.SearchResult result) {
        StackPane icon = result.type().equals("ARTIST") ? Widgets.noteCircle(22) : Widgets.noteSquare(36);

        Label title = new Label(result.title());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label subtitle = new Label(result.subtitle());
        subtitle.setTextFill(Color.web(Theme.MUTED));
        subtitle.setStyle("-fx-font-size: 12px;");

        VBox textBlock = new VBox(2, title, subtitle);

        HBox row = new HBox(14, icon, textBlock);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 4, 10, 4));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER
                + " transparent; -fx-border-width: 1; -fx-cursor: hand;");
        row.setOnMouseClicked(e -> {
            switch (result.type()) {
                case "ARTIST" -> nav.toArtist.accept(artistController.getArtistById(result.id()));
                case "ALBUM" -> nav.toAlbum.accept(result.id());
                default -> nav.toSong.accept(result.id());
            }
        });
        return row;
    }
}
