package com.crescendo.panels;

import com.crescendo.controller.AuthController;
import com.crescendo.model.User;
import javafx.geometry.Insets;
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
 * Owned by Metehan Karadeniz (User & Authentication) - the Register Page.
 * Same desktop-style split pane as LoginView, plus a Back button to return to Login.
 */
public final class RegisterView {
    private RegisterView() {
    }

    public static Parent build(Stage stage, AuthController authController, Consumer<User> onRegisterSuccess,
                                Runnable onGoLogin) {
        Region brandPane = Widgets.brandPane("Create an account to rate, review and follow.");

        Label heading = new Label("Create your account");
        heading.setTextFill(Color.web(Theme.INK));
        heading.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-font-family: '" + Theme.SANS_FONT + "';");

        Label subheading = new Label("Join Crescendo to rate, review and follow.");
        subheading.setTextFill(Color.web(Theme.MUTED));
        subheading.setStyle("-fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        Widgets.styleField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        Widgets.styleField(passwordField);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        Widgets.styleField(confirmField);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(Theme.DANGER));
        errorLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button createButton = new Button("Create Account");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: #11110E;"
                + " -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10;");
        createButton.setOnAction(e -> {
            if (!passwordField.getText().equals(confirmField.getText())) {
                errorLabel.setText("Passwords don't match.");
                return;
            }
            try {
                User user = authController.register(usernameField.getText(), passwordField.getText());
                onRegisterSuccess.accept(user);
            } catch (RuntimeException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        Button loginLink = new Button("Already have an account? Log in");
        loginLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.GOLD + "; -fx-font-size: 12px;");
        loginLink.setOnAction(e -> onGoLogin.run());

        VBox form = new VBox(14, heading, subheading, usernameField, passwordField, confirmField, errorLabel,
                createButton, loginLink);
        form.setMaxWidth(340);
        form.setAlignment(Pos.CENTER_LEFT);

        VBox formPane = new VBox(form);
        formPane.setAlignment(Pos.CENTER);
        formPane.setStyle("-fx-background-color: " + Theme.BG + ";");
        HBox.setHgrow(formPane, Priority.ALWAYS);

        HBox root = new HBox(brandPane, formPane);
        root.setPrefSize(1080, 720);
        root.setMinSize(900, 620);

        Button backButton = new Button("‹ Back to Login");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.INK + ";"
                + " -fx-font-size: 13px; -fx-font-weight: bold;");
        backButton.setOnAction(e -> onGoLogin.run());
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(20));

        StackPane wrapper = new StackPane(root, backButton, Widgets.closeButton(stage));
        return wrapper;
    }
}
