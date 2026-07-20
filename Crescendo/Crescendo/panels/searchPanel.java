package Crescendo.panels;

import Crescendo.controllers.SearchController;
import Crescendo.controllers.SearchResult;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class searchPanel extends BorderPane {
    private TextField searchField;
    private ToggleGroup filterGroup;
    private HBox filterBar;
    private ListView<SearchResult> resultsListView;
    private Label placeholderLabel;

    public searchPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setStyle("-fx-background-color: #FFFFFF;");

        sidebarPanel sidebar = new sidebarPanel();
        setLeft(sidebar);

        VBox mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 30 40 20 40; -fx-spacing: 15;");
        mainContent.setAlignment(Pos.TOP_LEFT);

        Label searchTitle = new Label("Find anything");
        searchTitle.setStyle(
                "-fx-font-family: 'SansSerif'; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        searchField = new TextField();
        searchField.setPromptText("Search artists, albums, songs, people...");

        String normalFieldStyle = "-fx-font-family: 'SansSerif'; -fx-font-size: 18px; -fx-padding: 10; -fx-background-color: #FAFAFA; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-background-radius: 5;";
        String focusedFieldStyle = "-fx-font-family: 'SansSerif'; -fx-font-size: 18px; -fx-padding: 10; -fx-background-color: #FAFAFA; -fx-border-color: #D4AF37; -fx-border-radius: 5; -fx-background-radius: 5;";

        searchField.setStyle(normalFieldStyle);
        searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                searchField.setStyle(focusedFieldStyle);
            } else {
                searchField.setStyle(normalFieldStyle);
            }
        });

        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterBar = new HBox();
        filterBar.setSpacing(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        filterGroup = new ToggleGroup();
        String[] filters = { "All", "Artists", "Albums", "Songs", "People" };

        for (String filter : filters) {
            ToggleButton filterButton = new ToggleButton(filter);
            filterButton.setToggleGroup(filterGroup);
            if (filter.equals("All")) {
                filterButton.setSelected(true);
            }
            filterBar.getChildren().add(filterButton);
        }
        applyFilterButtonStyles();

        resultsListView = new ListView<>();
        resultsListView.setStyle(
                "-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-border-color: transparent;");
        resultsListView.setCellFactory(listView -> new searchResultRenderer());

        placeholderLabel = new Label("Type a query and press enter to search.");
        placeholderLabel.setStyle(
                "-fx-font-family: 'SansSerif'; -fx-font-size: 16px; -fx-font-style: italic; -fx-text-fill: #888888;");
        resultsListView.setPlaceholder(placeholderLabel);

        VBox.setVgrow(resultsListView, Priority.ALWAYS);

        mainContent.getChildren().addAll(searchTitle, searchField, filterBar, resultsListView);
        setCenter(mainContent);

        searchField.setOnAction(e -> performSearch());

        filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyFilterButtonStyles();
                performSearch();
            } else if (oldVal != null) {
                oldVal.setSelected(true);
            }
        });
    }

    private void applyFilterButtonStyles() {
        for (javafx.scene.Node node : filterBar.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                if (btn.isSelected()) {
                    btn.setStyle(
                            "-fx-background-color: #D4AF37; -fx-text-fill: #FFFFFF; -fx-font-family: 'SansSerif'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
                } else {
                    btn.setStyle(
                            "-fx-background-color: #EEEEEE; -fx-text-fill: #333333; -fx-font-family: 'SansSerif'; -fx-font-size: 14px; -fx-background-radius: 15; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
                }
            }
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim();

        ToggleButton selectedButton = (ToggleButton) filterGroup.getSelectedToggle();
        String filterType = selectedButton != null ? selectedButton.getText() : "All";

        resultsListView.getItems().clear();

        if (query.isEmpty()) {
            placeholderLabel.setText("Type a query and press enter to search.");
            return;
        }

        placeholderLabel.setText("Searching...");

        Task<List<SearchResult>> searchTask = new Task<>() {
            @Override
            protected List<SearchResult> call() throws Exception {
                SearchController searchController = new SearchController();
                return searchController.searchMusicItem(query, filterType);
            }
        };

        searchTask.setOnSucceeded(e -> {
            List<SearchResult> results = searchTask.getValue();
            if (results.isEmpty()) {
                placeholderLabel.setText("No results found.");
            } else {
                resultsListView.getItems().setAll(results);
            }
        });

        searchTask.setOnFailed(e -> {
            placeholderLabel.setText("Error retrieving results.");
            Throwable ex = searchTask.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        Thread thread = new Thread(searchTask);
        thread.setDaemon(true);
        thread.start();
    }

    public static class Launcher extends Application {
        @Override
        public void start(Stage primaryStage) {
            try {
                searchPanel root = new searchPanel();
                Scene scene = new Scene(root, 1000, 700);
                primaryStage.setTitle("Crescendo - Search");
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}