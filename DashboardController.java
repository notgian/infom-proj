package org.example.eqlab2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    private void load(ActionEvent event, String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/eqlab2/" + fxml));
        getStage(event).setScene(new Scene(root, 400, 650));
    }

    @FXML
    public void goToReservation(ActionEvent event) throws IOException {
        load(event, "reservation-view.fxml");
    }

    @FXML
    public void goToBorrow(ActionEvent event) throws IOException {
        load(event, "borrow-view.fxml");
    }

    @FXML
    public void goToReturn(ActionEvent event) throws IOException {
        load(event, "return-view.fxml");
    }

    @FXML
    public void logout(ActionEvent event) throws IOException {
        load(event, "homepg-view.fxml");
    }
}
