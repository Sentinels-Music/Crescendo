package panels;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class sidebarPanel extends VBox {

    public sidebarPanel() {
        initializeSidebar();
    }

    private void initializeSidebar() {
        setStyle("-fx-background-color: #141414; -fx-padding: 30 20 30 20; -fx-spacing: 15;");
        setPrefWidth(200);

        Label logoLabel = new Label("Crescendo");
        logoLabel.setStyle(
                "-fx-font-family: 'SansSerif'; -fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #D4AF37; -fx-padding: 0 0 20 0;");
        getChildren().add(logoLabel);

        String[] navItems = { "Home", "Discover", "Listen Later" };
        for (String item : navItems) {
            Button navButton = new Button(item);
            String normalStyle = "-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-family: 'SansSerif'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 10 15 10 15; -fx-background-radius: 6; -fx-cursor: hand;";
            String hoverStyle = "-fx-background-color: #2D2D2D; -fx-text-fill: #D4AF37; -fx-font-family: 'SansSerif'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 10 15 10 15; -fx-background-radius: 6; -fx-cursor: hand;";

            navButton.setStyle(normalStyle);
            navButton.setMaxWidth(Double.MAX_VALUE);

            navButton.setOnMouseEntered(e -> navButton.setStyle(hoverStyle));
            navButton.setOnMouseExited(e -> navButton.setStyle(normalStyle));

            getChildren().add(navButton);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        Label profileLabel = new Label("mustafa");
        profileLabel.setStyle(
                "-fx-text-fill: #FFFFFF; -fx-font-family: 'SansSerif'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label verifiedLabel = new Label("Verified");
        verifiedLabel.setStyle(
                "-fx-text-fill: #D4AF37; -fx-font-family: 'SansSerif'; -fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");

        VBox profilePanel = new VBox(profileLabel, verifiedLabel);
        profilePanel.setStyle(
                "-fx-background-color: #242424; -fx-padding: 12 15 12 15; -fx-background-radius: 8; -fx-spacing: 4;");
        profilePanel.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(profilePanel);
    }
}
