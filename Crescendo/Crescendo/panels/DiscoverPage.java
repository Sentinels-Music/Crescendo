package Crescendo.panels;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Crescendo.controllers.ArtistController;
import Crescendo.model.Artist;
import Crescendo.model.Tag;

public class DiscoverPage extends Application {

    private final ArtistController artistController = new ArtistController();
    private final ArtistPage artistPage = new ArtistPage();

    private Stage stage;
    private Scene scene;
    private List<Artist> catalog;
    private List<Tag> allTags;
    private Tag selectedTag;// null means "All"
    private FlowPane artistGrid;

    
    //Entry points
    @Override
    public void start(Stage stage) {
        showIn(stage);
        stage.setTitle("Crescendo · Discover");
        stage.show();
    }

    //Renders the Discover page into an existing window (used for navigation). 
    public void showIn(Stage stage) {
        this.stage = stage;
        this.scene = null;
        this.catalog = artistController.getAllArtists();
        this.allTags = collectTags(catalog);
        this.selectedTag = null;
        showDiscover();
    }

    
    //Navigation
    private void showDiscover() {
        render("Discover", buildDiscoverContent());
    }

    private void showArtist(Artist artist) {
        // Reload the artist with albums/songs/streaming links before opening its page.
        Artist fullArtist = artistController.getArtistById(artist.getArtistId());
        render("Discover", artistPage.buildArtistContent(fullArtist, this::showDiscover));
    }

    // Draws the given content inside the shared shell and reuses one Scene. 
    private void render(String activeNav, Node content) {
        Runnable goHome = () -> render("Home",
                ArtistPage.placeholder("Home", "This screen is implemented by teammate Abdullah Efe Anık."));
        Runnable goDiscover = this::showDiscover;
        Runnable goListenLater = () -> render("Listen Later",
                ArtistPage.placeholder("Listen Later", "This screen is implemented by teammate Mustafa Ziya Akyol."));

        AnchorPane shell = ArtistPage.buildShell(stage, activeNav, content, goHome, goDiscover, goListenLater);

        if (scene == null) {
            scene = new Scene(shell, 933, 695);
            scene.getStylesheets().add(DiscoverPage.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
        } 
        else {
            scene.setRoot(shell);
        }
    }

    
    // Discover content
    private VBox buildDiscoverContent() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(4, 8, 20, 4));

        // title row + "Add New Artist" (Verified privilege)
        Label title = new Label("Discover");
        title.getStyleClass().add("page-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button addArtist = new Button("+ Add New Artist");
        addArtist.getStyleClass().add("gold-button");
        addArtist.setOnAction(e -> {
            String name = ArtistPage.askText("Add New Artist", "Artist name:");
            if (name != null && !name.trim().isEmpty()) {
                Artist saved = artistController.addNewArtist(name.trim(), "");   // ArtistController.addNewArtist()
                if (saved != null) {
                    catalog.add(saved);
                    allTags = collectTags(catalog);
                    showDiscover();
                }
            }
        });
        HBox titleRow = new HBox(14, title, spacer, addArtist);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Find your next favorite by genre");
        subtitle.getStyleClass().add("subtitle");

        // ---- genre chips (filter) ----
        Label browseTitle = new Label("B R O W S E   B Y   G E N R E");
        browseTitle.getStyleClass().add("section-title");
        FlowPane chips = new FlowPane(10, 10);
        chips.getChildren().add(tagChip(null, "All"));
        for (Tag tag : allTags) {
            chips.getChildren().add(tagChip(tag, tag.getName()));
        }

        // ---- colourful genre cards ----
        FlowPane genreCards = new FlowPane(16, 16);
        String[] colors = {"#E2AE3F", "#C1683A", "#3F7E6B", "#4A6FA5", "#9B5AA5", "#B58A2E"};
        int i = 0;
        for (Tag tag : allTags) {
            genreCards.getChildren().add(genreCard(tag, colors[i % colors.length]));
            i++;
        }

        // ---- trending artists (filtered) ----
        Label trendingTitle = new Label("T R E N D I N G   A R T I S T S");
        trendingTitle.getStyleClass().add("section-title");
        artistGrid = new FlowPane(16, 16);
        refreshArtists();

        page.getChildren().addAll(titleRow, subtitle, browseTitle, chips,
                genreCards, trendingTitle, artistGrid);
        return page;
    }

    /** Clears and refills the artist grid using the currently selected tag. */
    private void refreshArtists() {
        artistGrid.getChildren().clear();
        for (Artist artist : catalog) {
            if (selectedTag == null || artist.hasTag(selectedTag)) {
                artistGrid.getChildren().add(artistCard(artist));
            }
        }
        if (artistGrid.getChildren().isEmpty()) {
            Label empty = new Label("No artists found. Try \"+ Add New Artist\", "
                    + "or check that the database connection works.");
            empty.getStyleClass().add("item-sub");
            empty.setWrapText(true);
            artistGrid.getChildren().add(empty);
        }
    }

    //  Building blocks
    private Button tagChip(Tag tag, String text) {
        Button chip = new Button(text);
        chip.getStyleClass().add("tag-chip");
        boolean active = (tag == null && selectedTag == null)
                || (tag != null && tag.equals(selectedTag));
        if (active) {
            chip.getStyleClass().add("tag-chip-active");
        }
        chip.setOnAction(e -> {
            selectedTag = tag;
            showDiscover(); // rebuild so chip highlight and grid update
        });
        return chip;
    }

    private VBox genreCard(Tag tag, String color) {
        Label name = new Label(tag.getName());
        name.getStyleClass().add("genre-title");
        int count = countArtists(tag);
        Label sub = new Label(count + (count == 1 ? " artist" : " artists"));
        sub.getStyleClass().add("genre-sub");

        VBox card = new VBox(6, name, sub);
        card.setPrefSize(200, 96);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 14;"
                + " -fx-padding: 22; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            selectedTag = tag;
            showDiscover();
        });
        return card;
    }

    private HBox artistCard(Artist artist) {
        Label name = new Label(artist.getName());
        name.getStyleClass().add("item-title");
        Label sub = new Label(artist.getMainGenre() + " · " + artist.getFollowerCount() + " followers");
        sub.getStyleClass().add("item-sub");
        VBox text = new VBox(4, name, sub,
                ArtistPage.starRow(artist.getAverageRating(), true));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button view = new Button("View ›");
        view.getStyleClass().add("play-button");

        HBox card = new HBox(14, text, spacer, view);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll("card", "clickable");
        card.setMinWidth(430);

        card.setOnMouseClicked(e -> showArtist(artist));
        view.setOnAction(e -> showArtist(artist));
        return card;
    }

    
    //  Helpers over the catalog
    private List<Tag> collectTags(List<Artist> artists) {
        List<Tag> tags = new ArrayList<>();
        for (Artist artist : artists) {
            for (Tag tag : artist.getTags()) {
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    private int countArtists(Tag tag) {
        int count = 0;
        for (Artist artist : catalog) {
            if (artist.hasTag(tag)) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
