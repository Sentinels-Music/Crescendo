package views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone launcher for testing the Person 1 screens (Login -> Register
 * -> Profile) on their own. The finished app will have the team's real
 * main class; this just opens the Login screen so you can try the flow.
 *
 * Run with JavaFX on the module path, e.g.:
 *   java --module-path &lt;javafx-sdk&gt;/lib --add-modules javafx.controls,javafx.fxml views.AuthApp
 */
public class AuthApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("LoginView.fxml"));
        stage.setScene(new Scene(root, SceneNavigator.WIDTH, SceneNavigator.HEIGHT));
        stage.setTitle("Crescendo · Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
