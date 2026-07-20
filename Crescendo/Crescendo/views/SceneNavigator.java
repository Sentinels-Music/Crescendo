package Crescendo.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Loads an FXML screen and swaps it into the given stage. Keeps the
 * screen-switching code in one place so the individual controllers stay
 * small.
 */
public final class SceneNavigator {

    public static final double WIDTH = 1000;
    public static final double HEIGHT = 700;

    private SceneNavigator() {
    }

    public static void switchTo(Stage stage, String fxmlFileName) {
        try {
            Parent root = FXMLLoader.load(
                    SceneNavigator.class.getResource(fxmlFileName));
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
