package views;

import controllers.Session;
import controllers.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

/**
 * Controller for the Login screen. Validates input, authenticates through
 * {@link UserController}, stores the user in {@link Session}, and moves to
 * the Profile page on success.
 */
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final UserController userController = new UserController();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }

        User user = userController.login(username, password);
        if (user == null) {
            showError("Invalid username or password.");
            return;
        }

        Session.setCurrentUser(user);
        SceneNavigator.switchTo(stage(), "ProfileView.fxml");
    }

    @FXML
    private void goToRegister() {
        SceneNavigator.switchTo(stage(), "RegisterView.fxml");
    }

    @FXML
    private void handleForgotPassword() {
        showError("Password reset is not available yet.");
    }

    private void showError(String message) {
        messageLabel.getStyleClass().setAll("error-label");
        messageLabel.setText(message);
    }

    private Stage stage() {
        return (Stage) usernameField.getScene().getWindow();
    }
}
