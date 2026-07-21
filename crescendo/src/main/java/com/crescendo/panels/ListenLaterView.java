package com.crescendo.panels;

import com.crescendo.controller.MusicController;
import com.crescendo.db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/** Owned by Mustafa Ziya Akyol (Music Items & Listen Later) - the Listen Later Page. */
public final class ListenLaterView {
    private ListenLaterView() {
    }

    public static Parent build(Nav nav, MusicController musicController, Runnable onRefresh) {
        List<Database.ListenLaterEntry> entries = musicController.getListenLater(nav.currentUser);

        Label eyebrow = Widgets.eyebrow("Your Library");
        Label heading = new Label("Listen Later");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");
        Label subtitle = Widgets.labelMuted(entries.size() + " item" + (entries.size() == 1 ? "" : "s")
                + " saved · queued in the order you added them");

        VBox list = new VBox(0);
        if (entries.isEmpty()) {
            list.getChildren().add(Widgets.labelMuted("Nothing saved yet - use \"+ Listen Later\" on a song or album."));
        }
        for (Database.ListenLaterEntry entry : entries) {
            list.getChildren().add(entryRow(nav, musicController, entry, onRefresh));
        }

        VBox content = new VBox(16, eyebrow, heading, subtitle, list);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return Chrome.build(nav, "Listen Later", scroll, "Crescendo · Listen Later");
    }

    private static Region entryRow(Nav nav, MusicController musicController, Database.ListenLaterEntry entry,
                                    Runnable onRefresh) {
        StackPane icon = Widgets.noteSquare(44);

        Label typeLabel = Widgets.eyebrow(entry.itemType());
        Label title = new Label(entry.title());
        title.setTextFill(Color.web(Theme.INK));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        title.setOnMouseClicked(e -> {
            if (entry.itemType().equals("SONG")) {
                nav.toSong.accept(entry.itemId());
            } else {
                nav.toAlbum.accept(entry.itemId());
            }
        });
        Label subtitle = Widgets.labelMuted(entry.subtitle());

        VBox textBlock = new VBox(2, typeLabel, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label savedLabel = Widgets.labelMuted("saved " + Widgets.relativeTime(entry.savedAt()));
        Button removeButton = new Button("× Remove");
        removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.DANGER + "; -fx-font-size: 11px;");
        removeButton.setOnAction(e -> {
            musicController.removeFromListenLater(nav.currentUser, entry.itemType(), entry.itemId());
            onRefresh.run();
        });

        VBox rightBlock = new VBox(4, savedLabel, removeButton);
        rightBlock.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(14, icon, textBlock, spacer, rightBlock);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 4, 14, 4));
        row.setStyle("-fx-border-color: transparent transparent " + Theme.BORDER + " transparent; -fx-border-width: 1;");
        return row;
    }
}
