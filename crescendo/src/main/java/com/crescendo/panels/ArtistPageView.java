package com.crescendo.panels;

import com.crescendo.controller.ArtistController;
import com.crescendo.controller.MusicController;
import com.crescendo.model.Album;
import com.crescendo.model.Artist;
import com.crescendo.model.Review;
import com.crescendo.model.Song;
import com.crescendo.model.Tag;
import com.crescendo.model.VerifiedUser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

/** Owned by Emir Selim Kayhan (Artist, Tag & Streaming) - the Artist Page. */
public final class ArtistPageView {
    private ArtistPageView() {
    }

    public static Parent build(Nav nav, ArtistController controller, MusicController musicController, Artist artist,
                                Runnable onRefresh) {
        VBox content = new VBox(22,
                buildHeader(nav, controller, artist, onRefresh),
                buildTagsSection(controller, artist, onRefresh),
                Widgets.divider(),
                buildBody(nav, controller, musicController, artist, onRefresh),
                Widgets.divider(),
                buildReviewsSection(artist));
        content.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return Chrome.build(nav, "Discover", scroll, "Crescendo · Artist");
    }

    private static Region buildHeader(Nav nav, ArtistController controller, Artist artist, Runnable onRefresh) {
        StackPane avatar = Widgets.noteCircle(50);

        Label name = new Label(artist.getName());
        name.setTextFill(Color.web(Theme.INK));
        name.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        String primaryTag = artist.getTags().isEmpty() ? "Uncategorized" : artist.getTags().get(0).getName();
        Label subtitle = new Label(primaryTag + " · " + artist.getAlbums().size() + " albums · "
                + artist.getFollowerCount() + " followers");
        subtitle.setTextFill(Color.web(Theme.MUTED));
        subtitle.setStyle("-fx-font-size: 13px;");

        VBox titleBlock = new VBox(4, name, subtitle);

        HBox nameRow = new HBox(14, avatar, titleBlock);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean alreadyFollowing = controller.isFollowingArtist(nav.currentUser, artist);
        Button followButton = new Button(alreadyFollowing ? "Following" : "+ Follow");
        followButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E;"
                + " -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16;");
        followButton.setOnAction(e -> {
            try {
                if (alreadyFollowing) {
                    controller.unfollowArtist(nav.currentUser, artist);
                } else {
                    controller.followArtist(nav.currentUser, artist);
                }
                onRefresh.run();
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });

        Button rateButton = new Button("Rate");
        rateButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.INK + ";"
                + " -fx-border-color: " + Theme.INK + "; -fx-border-radius: 10; -fx-background-radius: 10;"
                + " -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        rateButton.setOnAction(e -> RateDialog.show(nav.stage, artist.getName(), (stars, comment) -> {
            controller.rateArtist(artist, nav.currentUser, stars, comment);
            onRefresh.run();
        }));

        VBox actionButtons = new VBox(8, followButton, rateButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        HBox headerRow = new HBox(nameRow, spacer, actionButtons);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(40,
                statBlock(String.valueOf(artist.getAlbums().size()), "ALBUMS"),
                statBlock(String.valueOf(artist.getSongs().size()), "SONGS"),
                statBlock(String.format("%.1f", artist.calculateAverageRating()), "AVG RATING"));
        stats.setPadding(new Insets(14, 0, 0, 0));

        return new VBox(0, headerRow, stats);
    }

    private static VBox statBlock(String value, String label) {
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(Theme.INK));
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label captionLabel = new Label(label);
        captionLabel.setTextFill(Color.web(Theme.MUTED));
        captionLabel.setStyle("-fx-font-size: 10px; -fx-letter-spacing: 0.1em;");
        return new VBox(2, valueLabel, captionLabel);
    }

    private static VBox buildTagsSection(ArtistController controller, Artist artist, Runnable onRefresh) {
        FlowPane tagPane = new FlowPane(8, 8);
        for (Tag tag : artist.getTags()) {
            Button chip = new Button(tag.getName() + "   ×");
            chip.setStyle("-fx-background-color: white; -fx-text-fill: " + Theme.INK + ";"
                    + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 14; -fx-background-radius: 14;"
                    + " -fx-font-size: 12px; -fx-padding: 4 12 4 12;");
            chip.setOnAction(e -> {
                controller.removeTag(artist, tag);
                onRefresh.run();
            });
            tagPane.getChildren().add(chip);
        }

        TextField newTagField = new TextField();
        newTagField.setPromptText("Add a tag");
        newTagField.setPrefWidth(120);
        newTagField.setStyle("-fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: "
                + Theme.BORDER + "; -fx-font-size: 12px;");

        Button addButton = new Button("+ Tag");
        addButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.GOLD + ";"
                + " -fx-font-size: 12px; -fx-font-weight: bold;");
        addButton.setOnAction(e -> {
            if (!newTagField.getText().isBlank()) {
                controller.addTag(artist, newTagField.getText());
                onRefresh.run();
            }
        });

        HBox addRow = new HBox(6, newTagField, addButton);
        addRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, Widgets.eyebrow("Tags"), tagPane, addRow);
    }

    private static Region buildBody(Nav nav, ArtistController controller, MusicController musicController,
                                     Artist artist, Runnable onRefresh) {
        HBox discographyHeader = new HBox(10, Widgets.eyebrow("Discography"));
        discographyHeader.setAlignment(Pos.CENTER_LEFT);
        if (nav.verified) {
            discographyHeader.getChildren().add(addLink("+ Add Album",
                    () -> showAddAlbumDialog(nav, musicController, artist, onRefresh)));
        }

        VBox discographyColumn = new VBox(14, discographyHeader);
        for (Album album : artist.getAlbums()) {
            discographyColumn.getChildren().add(albumRow(nav, album));
        }
        if (artist.getAlbums().isEmpty()) {
            discographyColumn.getChildren().add(emptyHint("No albums yet"));
        }

        HBox songsHeader = new HBox(10, Widgets.eyebrow("Popular Songs"));
        songsHeader.setAlignment(Pos.CENTER_LEFT);
        if (nav.verified) {
            songsHeader.getChildren().add(addLink("+ Add Song",
                    () -> showAddSongDialog(nav, musicController, artist, onRefresh)));
        }

        VBox songsColumn = new VBox(14, songsHeader);
        for (Song song : artist.getSongs()) {
            songsColumn.getChildren().add(songRow(nav, controller, song));
        }
        if (artist.getSongs().isEmpty()) {
            songsColumn.getChildren().add(emptyHint("No songs yet"));
        }

        HBox body = new HBox(48, discographyColumn, songsColumn);
        HBox.setHgrow(discographyColumn, Priority.ALWAYS);
        HBox.setHgrow(songsColumn, Priority.ALWAYS);
        return body;
    }

    private static Button addLink(String label, Runnable onClick) {
        Button button = new Button(label);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.GOLD + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold;");
        button.setOnAction(e -> onClick.run());
        return button;
    }

    private static void showAddAlbumDialog(Nav nav, MusicController musicController, Artist artist, Runnable onRefresh) {
        Stage dialog = new Stage();
        dialog.initOwner(nav.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Add Album");

        Label title = new Label("Add Album to " + artist.getName());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        TextField titleField = new TextField();
        titleField.setPromptText("Album title");

        TextField yearField = new TextField();
        yearField.setPromptText("Release year (e.g. 2024)");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(Theme.DANGER));
        errorLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button saveButton = new Button("Add Album");
        saveButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-font-weight: bold;"
                + " -fx-background-radius: 10; -fx-padding: 8 16 8 16;");
        saveButton.setOnAction(e -> {
            int year;
            try {
                year = Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Enter a valid release year.");
                return;
            }
            try {
                musicController.addAlbum((VerifiedUser) nav.currentUser, artist, titleField.getText(), year);
                dialog.close();
                onRefresh.run();
            } catch (IllegalArgumentException | SecurityException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        VBox root = new VBox(12, title, titleField, yearField, errorLabel, saveButton);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Theme.BG + ";");
        dialog.setScene(new Scene(root, 320, 240));
        dialog.showAndWait();
    }

    private static void showAddSongDialog(Nav nav, MusicController musicController, Artist artist, Runnable onRefresh) {
        Stage dialog = new Stage();
        dialog.initOwner(nav.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Add Song");

        Label title = new Label("Add Song to " + artist.getName());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SERIF_FONT + "';");

        TextField titleField = new TextField();
        titleField.setPromptText("Song title");

        TextField minutesField = new TextField();
        minutesField.setPromptText("min");
        minutesField.setPrefWidth(60);
        TextField secondsField = new TextField();
        secondsField.setPromptText("sec");
        secondsField.setPrefWidth(60);
        HBox durationRow = new HBox(6, minutesField, new Label(":"), secondsField);
        durationRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Album> albumBox = new ComboBox<>();
        albumBox.getItems().add(null);
        albumBox.getItems().addAll(artist.getAlbums());
        albumBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Album album) {
                return album == null ? "No album (standalone single)" : album.getTitle() + " (" + album.getReleaseYear() + ")";
            }

            @Override
            public Album fromString(String string) {
                return null;
            }
        });
        albumBox.getSelectionModel().selectFirst();
        albumBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(Theme.DANGER));
        errorLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button saveButton = new Button("Add Song");
        saveButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-font-weight: bold;"
                + " -fx-background-radius: 10; -fx-padding: 8 16 8 16;");
        saveButton.setOnAction(e -> {
            int minutes;
            int seconds;
            try {
                minutes = minutesField.getText().isBlank() ? 0 : Integer.parseInt(minutesField.getText().trim());
                seconds = secondsField.getText().isBlank() ? 0 : Integer.parseInt(secondsField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Enter a valid duration.");
                return;
            }
            try {
                musicController.addSong((VerifiedUser) nav.currentUser, artist, albumBox.getValue(),
                        titleField.getText(), minutes * 60 + seconds);
                dialog.close();
                onRefresh.run();
            } catch (IllegalArgumentException | SecurityException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        VBox root = new VBox(12, title, titleField, durationRow, albumBox, errorLabel, saveButton);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Theme.BG + ";");
        dialog.setScene(new Scene(root, 340, 300));
        dialog.showAndWait();
    }

    private static Label emptyHint(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(Theme.MUTED));
        label.setStyle("-fx-font-size: 12px;");
        return label;
    }

    private static Region albumRow(Nav nav, Album album) {
        Label title = new Label(album.getTitle());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label year = new Label(String.valueOf(album.getReleaseYear()));
        year.setTextFill(Color.web(Theme.MUTED));
        year.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(title, spacer, year);
        top.setAlignment(Pos.CENTER_LEFT);

        Label avg = new Label(String.format("%.1f", album.calculateAverageRating()));
        avg.setTextFill(Color.web(Theme.MUTED));
        avg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        HBox ratingRow = new HBox(8, Widgets.stars(album.calculateAverageRating()), avg);
        ratingRow.setAlignment(Pos.CENTER_LEFT);

        VBox row = new VBox(4, top, ratingRow);
        row.setPadding(new Insets(0, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER
                + " transparent; -fx-border-width: 1; -fx-cursor: hand;");
        row.setOnMouseClicked(e -> nav.toAlbum.accept(album.getItemId()));
        return row;
    }

    private static Region songRow(Nav nav, ArtistController controller, Song song) {
        StackPane thumb = Widgets.noteSquare(40);

        Label title = new Label(song.getTitle());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label duration = new Label(Widgets.formatDuration(song.getDurationInSeconds()));
        duration.setTextFill(Color.web(Theme.MUTED));
        duration.setStyle("-fx-font-size: 11px;");

        VBox titleBlock = new VBox(2, title, duration);
        titleBlock.setOnMouseClicked(e -> nav.toSong.accept(song.getItemId()));
        titleBlock.setStyle("-fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label avg = new Label(String.format("%.1f", song.calculateAverageRating()));
        avg.setTextFill(Color.web(Theme.GOLD));
        avg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        HBox mainRow = new HBox(10, thumb, titleBlock, spacer, avg);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        HBox linkRow = new HBox(6);
        linkRow.setPadding(new Insets(2, 0, 0, 50));
        for (String platform : song.getStreamingLinks().keySet()) {
            Button linkButton = new Button(platform);
            linkButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.GOLD + ";"
                    + " -fx-border-color: " + Theme.GOLD + "; -fx-border-radius: 10; -fx-background-radius: 10;"
                    + " -fx-font-size: 10px; -fx-padding: 2 10 2 10;");
            linkButton.setOnAction(e -> {
                String url = controller.getStreamingLink(song, platform);
                if (!controller.open(url)) {
                    new Alert(Alert.AlertType.WARNING,
                            "Could not open the " + platform + " link for \"" + song.getTitle() + "\".").showAndWait();
                }
            });
            linkRow.getChildren().add(linkButton);
        }

        VBox row = new VBox(4, mainRow, linkRow);
        row.setPadding(new Insets(0, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER + " transparent; -fx-border-width: 1;");
        return row;
    }

    private static Region buildReviewsSection(Artist artist) {
        Label eyebrow = Widgets.eyebrow("Reviews");

        Label summary = new Label(artist.getReviews().isEmpty() ? "No ratings yet"
                : String.format("%.1f average · %d rating%s", artist.calculateReviewAverage(),
                        artist.getReviews().size(), artist.getReviews().size() == 1 ? "" : "s"));
        summary.setTextFill(Color.web(Theme.MUTED));
        summary.setStyle("-fx-font-size: 12px;");

        HBox headerRow = new HBox(12, eyebrow, summary);
        headerRow.setAlignment(Pos.BASELINE_LEFT);

        VBox list = new VBox(14);
        for (Review review : artist.getReviews()) {
            list.getChildren().add(Widgets.reviewRow(review));
        }

        return new VBox(12, headerRow, list);
    }
}
