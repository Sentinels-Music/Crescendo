package com.crescendo.panels;

import com.crescendo.controller.ArtistController;
import com.crescendo.model.Artist;
import com.crescendo.model.Tag;
import com.crescendo.model.VerifiedUser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Locale;

/** Owned by Emir Selim Kayhan (Artist, Tag & Streaming) - the Discover Page. */
public final class DiscoverPageView {
    private DiscoverPageView() {
    }

    public static Parent build(Nav nav, ArtistController controller, Runnable onRefresh) {
        Label eyebrow = Widgets.eyebrow("Discover");
        Label heading = new Label("Find Artists");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");

        TextField searchField = new TextField();
        searchField.setPromptText("Search artists by name...");
        searchField.setPrefHeight(46);
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.INK + ";"
                + " -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px; -fx-padding: 0 14 0 14;");

        VBox resultsBox = new VBox(0);
        FlowPane tagFilterRow = new FlowPane(8, 8);

        Runnable[] refreshResults = new Runnable[1];
        String[] activeTagName = new String[]{null};

        refreshResults[0] = () -> renderResults(nav, controller, resultsBox, searchField.getText(), activeTagName[0]);
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshResults[0].run());

        buildTagFilter(controller, tagFilterRow, activeTagName, refreshResults);
        refreshResults[0].run();

        VBox searchSection = new VBox(14, eyebrow, heading, searchField, tagFilterRow);

        VBox resultsSection = new VBox(10, Widgets.eyebrow("Artists"), resultsBox);
        resultsSection.setPadding(new Insets(20, 0, 0, 0));

        VBox content = new VBox(24, searchSection, resultsSection);
        if (nav.verified) {
            content.getChildren().add(buildAddArtistCard(controller, (VerifiedUser) nav.currentUser, tagFilterRow,
                    activeTagName, refreshResults, onRefresh));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return Chrome.build(nav, "Discover", scroll, "Crescendo · Discover");
    }

    private static void buildTagFilter(ArtistController controller, FlowPane tagFilterRow,
                                        String[] activeTagName, Runnable[] refreshResults) {
        tagFilterRow.getChildren().clear();
        tagFilterRow.getChildren().add(filterChip("All", activeTagName[0] == null, () -> {
            activeTagName[0] = null;
            buildTagFilter(controller, tagFilterRow, activeTagName, refreshResults);
            refreshResults[0].run();
        }));
        for (Tag tag : controller.getAllTags()) {
            boolean active = tag.getName().equals(activeTagName[0]);
            tagFilterRow.getChildren().add(filterChip(tag.getName(), active, () -> {
                activeTagName[0] = tag.getName();
                buildTagFilter(controller, tagFilterRow, activeTagName, refreshResults);
                refreshResults[0].run();
            }));
        }
    }

    private static Button filterChip(String label, boolean active, Runnable onSelect) {
        Button chip = new Button(label);
        chip.setStyle(active
                ? "-fx-background-color: " + Theme.INK + "; -fx-text-fill: white;"
                        + " -fx-background-radius: 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14 6 14;"
                : "-fx-background-color: #EDE6D6; -fx-text-fill: " + Theme.MUTED + ";"
                        + " -fx-background-radius: 14; -fx-font-size: 12px; -fx-padding: 6 14 6 14;");
        chip.setOnAction(e -> onSelect.run());
        return chip;
    }

    private static void renderResults(Nav nav, ArtistController controller, VBox resultsBox, String query,
                                       String tagName) {
        resultsBox.getChildren().clear();
        List<Artist> artists = tagName == null ? controller.getAllArtists()
                : controller.getAllArtists().stream()
                        .filter(a -> a.getTags().stream().anyMatch(t -> t.getName().equals(tagName)))
                        .toList();

        String needle = query == null ? "" : query.toLowerCase(Locale.ROOT).strip();
        boolean any = false;
        for (Artist artist : artists) {
            if (!needle.isEmpty() && !artist.getName().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            resultsBox.getChildren().add(artistRow(nav, artist));
            any = true;
        }
        if (!any) {
            resultsBox.getChildren().add(Widgets.labelMuted("No artists match yet."));
        }
    }

    private static Region artistRow(Nav nav, Artist artist) {
        StackPane avatar = Widgets.noteCircle(26);

        Label name = new Label(artist.getName());
        name.setTextFill(Color.web(Theme.INK));
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        String primaryTag = artist.getTags().isEmpty() ? "Uncategorized" : artist.getTags().get(0).getName();
        Label subtitle = new Label(primaryTag + " · " + artist.getAlbums().size() + " albums · "
                + artist.getFollowerCount() + " followers");
        subtitle.setTextFill(Color.web(Theme.MUTED));
        subtitle.setStyle("-fx-font-size: 12px;");

        VBox textBlock = new VBox(2, name, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label avg = new Label(String.format("%.1f", artist.calculateAverageRating()));
        avg.setTextFill(Color.web(Theme.MUTED));
        avg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        HBox ratingBlock = new HBox(6, Widgets.stars(artist.calculateAverageRating()), avg);
        ratingBlock.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(14, avatar, textBlock, spacer, ratingBlock);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 4, 12, 4));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER
                + " transparent; -fx-border-width: 1; -fx-cursor: hand;");
        row.setOnMouseClicked(e -> nav.toArtist.accept(artist));
        return row;
    }

    private static Region buildAddArtistCard(ArtistController controller, VerifiedUser currentUser,
                                              FlowPane tagFilterRow, String[] activeTagName,
                                              Runnable[] refreshResults, Runnable onRefresh) {
        Label title = new Label("+ Add New Artist");
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label caption = new Label("Verified users can add artists that aren't in the catalog yet.");
        caption.setTextFill(Color.web(Theme.MUTED));
        caption.setStyle("-fx-font-size: 11px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Artist name");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Short description");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(descriptionField, Priority.ALWAYS);

        Button addButton = new Button("Add Artist");
        addButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E;"
                + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16;");
        addButton.setOnAction(e -> {
            try {
                controller.addNewArtist(currentUser, nameField.getText(), descriptionField.getText());
                nameField.clear();
                descriptionField.clear();
                buildTagFilter(controller, tagFilterRow, activeTagName, refreshResults);
                refreshResults[0].run();
                onRefresh.run();
            } catch (IllegalArgumentException | SecurityException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });

        HBox form = new HBox(10, nameField, descriptionField, addButton);
        form.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, title, caption, form);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 14;");
        return card;
    }
}
