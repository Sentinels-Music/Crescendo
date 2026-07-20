package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.net.URL;

public class CrescendoController {

    public void goHome(ActionEvent event) {
        navigate((Node) event.getSource(), "HomePage.fxml");
    }

    public void goDiscover(ActionEvent event) {
        navigate((Node) event.getSource(), "DiscoverPage.fxml");
    }

    public void goListenLater(ActionEvent event) {
        navigate((Node) event.getSource(), "ListenLaterPage.fxml");
    }

    public void goProfile(ActionEvent event) {
        navigate((Node) event.getSource(), "ProfilePage.fxml");
    }

    public void goProfile(MouseEvent event) {
        navigate((Node) event.getSource(), "ProfilePage.fxml");
    }

    public void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void navigate(Node source, String page) {
        try {
            URL location = getClass().getResource("/views/" + page);
            Parent root = new FXMLLoader(location).load();
            source.getScene().setRoot(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Crescendo");
            alert.setHeaderText(null);
            alert.setContentText(page + " henüz hazır değil.");
            alert.showAndWait();
        }
    }
}
