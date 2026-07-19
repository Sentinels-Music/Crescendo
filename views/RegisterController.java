package views;

import controllers.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Register screen. Creates a new account through
 * {@link UserController} and sends the user back to Login on success.
 */
public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField nicknameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final UserController userController = new UserController();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
            showError("Please fill in every field.");
            return;
        }
        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            return;
        }

        boolean created = userController.register(username, nickname, password);
        if (created) {
            showInfo("Account created! You can log in now.");
        } else {
            showError("That username is already taken.");
        }
    }

    @FXML
    private void goToLogin() {
        SceneNavigator.switchTo(stage(), "LoginView.fxml");
    }

    private void showError(String message) {
        messageLabel.getStyleClass().setAll("error-label");
        messageLabel.setText(message);
    }

    private void showInfo(String message) {
        messageLabel.getStyleClass().setAll("info-label");
        messageLabel.setText(message);
    }

    private Stage stage() {
        return (Stage) usernameField.getScene().getWindow();
    }
}
