package com.crescendo.panels;

import com.crescendo.controller.AuthController;
import com.crescendo.model.User;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Owned by Metehan Karadeniz (User & Authentication) - the Login Page.
 * A desktop-style split pane (dark brand panel + form) filling the whole window, rather
 * than a small floating card, so it reads as a desktop app screen.
 */
public final class LoginView {
    private LoginView() {
    }

    public static Parent build(Stage stage, AuthController authController, Consumer<User> onLoginSuccess,
                                Runnable onGoRegister) {
        Region brandPane = Widgets.brandPane("Log in to keep discovering music through people you trust.");

        Label heading = new Label("Welcome back");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");

        Label subheading = new Label("Log in to your Crescendo account.");
        subheading.setTextFill(Color.web(Theme.MUTED));
        subheading.setStyle("-fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        Widgets.styleField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Widgets.styleField(passwordField);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(Theme.DANGER));
        errorLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button loginButton = new Button("Log In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E;"
                + " -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10;");
        Runnable submit = () -> {
            try {
                User user = authController.login(usernameField.getText(), passwordField.getText());
                onLoginSuccess.accept(user);
            } catch (RuntimeException ex) {
                errorLabel.setText(ex.getMessage());
            }
        };
        loginButton.setOnAction(e -> submit.run());
        usernameField.setOnAction(e -> submit.run());
        passwordField.setOnAction(e -> submit.run());

        Button registerLink = new Button("New here? Create an account");
        registerLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.GOLD + "; -fx-font-size: 12px;");
        registerLink.setOnAction(e -> onGoRegister.run());

        VBox form = new VBox(14, heading, subheading, usernameField, passwordField, errorLabel, loginButton, registerLink);
        form.setMaxWidth(340);
        form.setAlignment(Pos.CENTER_LEFT);

        VBox formPane = new VBox(form);
        formPane.setAlignment(Pos.CENTER);
        formPane.setStyle("-fx-background-color: " + Theme.BG + ";");
        HBox.setHgrow(formPane, Priority.ALWAYS);

        HBox root = new HBox(brandPane, formPane);
        root.setPrefSize(1080, 720);
        root.setMinSize(900, 620);

        StackPane wrapper = new StackPane(root, Widgets.closeButton(stage));
        return wrapper;
    }
}
